package module.post;

import core.GL;
import core.Renderer;
import core.RenderingModule;
import core.framebuffer.ColorBuffer;
import core.framebuffer.DepthBuffer;
import core.framebuffer.FrameBuffer;
import core.math.EngineMath;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Buffers;
import core.utility.Reader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Manages the post processing pipeline.
 *
 * @author John Paul Quijano
 */
public class PostRenderer extends RenderingModule {
    public static final int GAUSSIAN_OFFSETS = 32;
    public static final int MIN_GAUSSIAN_KERNEL = 5;
    public static final int MAX_GAUSSIAN_KERNEL = 125;
    public static final int MIN_DOWNSAMPLING_LEVEL = 0;
    public static final int MAX_DOWNSAMPLING_LEVEL = 3;

    public static final int BLUR_VERTICAL = 0;
    public static final int BLUR_HORIZONTAL = 1;

    private int width;
    private int height;
    private Shader shader;

    private Variable fragmentSize_u;
    private Variable frameBufferSize_u;
    private Variable gaussianOffsets_u;
    private Variable gaussianWeights_u;
    private Variable gaussianKernel_u;
    private Variable gaussianDirection_u;
    private Variable brightThreshold_u;
    private Variable horizontalKernel_u;
    private Function bright_f;
    private Function gaussian_f;
    private Function horizontal_f;
    private Executable bright_e;
    private Executable gaussian_e;
    private Executable horizontal_e;

    private FrameBuffer sourceBuffer;
    private DepthBuffer depthBuffer;
    private ColorBuffer[] colorBuffers;

    private FrameBuffer outputBuffer;
    private FrameBuffer brightBuffer;
    private FrameBuffer[] downsamplers;

    private FloatBuffer[] gaussianOffsets;
    private FloatBuffer[] gaussianWeights;

    public PostRenderer() {
        initSourceBuffer();
        initOutputBuffer();
        initBrightBuffer();
        initDownsamplers();
        initGaussian();
    }

    /**
     * Gives the main rendering frame buffer.
     *
     * @return main rendering frame buffer
     */
    public FrameBuffer getSourceBuffer() {
        return sourceBuffer;
    }

    /**
     * Gives the framebuffer containing gathered bright pixels.
     *
     * @return framebuffer containing gathered bright pixels
     */
    public FrameBuffer getBrightBuffer() {
        return brightBuffer;
    }

    /**
     * Gives the output frame buffer.
     *
     * @return output frame buffer
     */
    public FrameBuffer getOutputBuffer() {
        return outputBuffer;
    }

    /**
     * Gives the down-sampling frame buffers.
     *
     * @return down-sampling frame buffers
     */
    public FrameBuffer[] getDownsamplingBuffers() {
        return downsamplers;
    }

    /**
     * Applies a Gaussian blur on the given source color buffer with the given kernel. Kernel is clamped between
     * MIN_DOWNSAMPLING_LEVEL and MAX_DOWNSAMPLING_LEVEL, inclusively.
     *
     * Level is the number of times to down-sample. The higher the level, the blurrier the result. Level is clamped
     * between MIN_DOWNSAMPLING_LEVEL and MAX_DOWNSAMPLING_LEVEL, inclusively.
     *
     * @param source - color buffer containing the input image
     * @param kernel - square of this value is the number of samples applied
     * @param level - number of times to down-sample
     *
     * @return color buffer containing the blurred image
     */
    public ColorBuffer blurGaussian(ColorBuffer source, int kernel, int level) {
        int blurLevel = EngineMath.clamp(level, MIN_DOWNSAMPLING_LEVEL, MAX_DOWNSAMPLING_LEVEL);
        int blurKernel = EngineMath.clamp(kernel % 2 == 0 ? kernel + 1 : kernel, MIN_GAUSSIAN_KERNEL, MAX_GAUSSIAN_KERNEL);
        int bufferIndex = (blurKernel - MIN_GAUSSIAN_KERNEL) / 2;
        int numKernel = (blurKernel - bufferIndex) / 2;

        GL.setScalar(gaussianKernel_u.getID(), numKernel);
        GL.setArray1(gaussianWeights_u.getID(), gaussianWeights[bufferIndex]);
        GL.setArray1(gaussianOffsets_u.getID(), gaussianOffsets[bufferIndex]);

        shader.execute(gaussian_e);

        bind(downsamplers[0]);
        drawGaussianBlur(source, BLUR_HORIZONTAL);
        drawGaussianBlur(downsamplers[0].getColorBuffer(1), BLUR_VERTICAL);

        for (int i = 1; i <= blurLevel; i++) {
            bind(downsamplers[i]);
            drawGaussianBlur(downsamplers[i-1].getColorBuffer(0), BLUR_HORIZONTAL);
            drawGaussianBlur(downsamplers[i].getColorBuffer(1), BLUR_VERTICAL);
        }

        return downsamplers[blurLevel].getColorBuffer(0);
    }

    /**
     * Applies a horizontal blur on the given source color buffer with the given kernel. Kernel is clamped between
     * MIN_DOWNSAMPLING_LEVEL and MAX_DOWNSAMPLING_LEVEL, inclusively.
     *
     * Level is the number of times to down-sample. The higher the level, the blurrier the result. Level is clamped
     * between MIN_DOWNSAMPLING_LEVEL and MAX_DOWNSAMPLING_LEVEL, inclusively.
     *
     * @param source - color buffer containing the input image
     * @param kernel - square of this value is the number of samples applied
     * @param level - number of times to down-sample
     *
     * @return color buffer containing the blurred image
     */
    public ColorBuffer blurHorizontal(ColorBuffer source, int kernel, int level) {
        int blurLevel = EngineMath.clamp(level, MIN_DOWNSAMPLING_LEVEL, MAX_DOWNSAMPLING_LEVEL);
        int blurKernel = EngineMath.clamp(kernel % 2 == 0 ? kernel + 1 : kernel, MIN_GAUSSIAN_KERNEL, MAX_GAUSSIAN_KERNEL);
        int bufferIndex = (blurKernel - MIN_GAUSSIAN_KERNEL) / 2;
        int numKernel = (blurKernel - bufferIndex) / 2;

        GL.setScalar(horizontalKernel_u.getID(), numKernel);
        GL.setArray1(gaussianWeights_u.getID(), gaussianWeights[bufferIndex]);
        GL.setArray1(gaussianOffsets_u.getID(), gaussianOffsets[bufferIndex]);

        shader.execute(horizontal_e);

        bind(downsamplers[0]);
        drawHorizontalBlur(source);

        for (int i = 1; i <= blurLevel; i++) {
            bind(downsamplers[i]);
            drawHorizontalBlur(downsamplers[i-1].getColorBuffer(0));
        }

        return downsamplers[blurLevel].getColorBuffer(0);
    }

    /**
     * Collects pixels that have higher luminance than the given threshold.
     *
     * @param source - color buffer containing the input image
     * @param threshold - luminance threshold
     *
     * @return color buffer containing the bright-filtered image
     */
    public ColorBuffer gatherBright(ColorBuffer source, float threshold) {
        GL.writeFrameBuffer(brightBuffer.getID());
        GL.writeAttachment(GL.Attachment.COLOR_0);
        GL.setScalar(brightThreshold_u.getID(), threshold);
        GL.bindTexture2D(source.getID(), renderer.getUtilSamplerUnit(0));

        shader.execute(bright_e);
        renderer.drawFullscreenQuad();

        return brightBuffer.getColorBuffer(0);
    }

    @Override
    protected void build() {
        shader = renderer.getShader();

        /**
         * Initialize definitions.
         */
        shader.addDefinition("BLUR_VERTICAL", String.valueOf(BLUR_VERTICAL));
        shader.addDefinition("BLUR_HORIZONTAL", String.valueOf(BLUR_HORIZONTAL));

        /**
         * Initialize uniforms.
         */
        fragmentSize_u = new Variable(Shader.Type.VEC2, "fragmentSize", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(fragmentSize_u);

        frameBufferSize_u = new Variable(Shader.Type.VEC2, "frameBufferSize", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(frameBufferSize_u);

        gaussianWeights_u = new Variable(Shader.Type.FLOAT, "gaussianWeights", null, GAUSSIAN_OFFSETS, Shader.Qualifier.UNIFORM);
        shader.addVariable(gaussianWeights_u);

        gaussianOffsets_u = new Variable(Shader.Type.FLOAT, "gaussianOffsets", null, GAUSSIAN_OFFSETS, Shader.Qualifier.UNIFORM);
        shader.addVariable(gaussianOffsets_u);

        gaussianKernel_u = new Variable(Shader.Type.INT, "gaussianKernel", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(gaussianKernel_u);

        gaussianDirection_u = new Variable(Shader.Type.INT, "gaussianDirection", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(gaussianDirection_u);

        horizontalKernel_u = new Variable(Shader.Type.INT, "horizontalKernel", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(horizontalKernel_u);

        brightThreshold_u = new Variable(Shader.Type.FLOAT, "brightThreshold", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(brightThreshold_u);

        /**
         * Initialize functions.
         */
        bright_f = new Function(
                Shader.Type.VEC4,
                "gather_bright",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.SAMPLER2D, "sampler", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC2, "texCoord", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "threshold", null, 0, Shader.Qualifier.IN)
        );
        bright_f.setSource(Reader.read(getClass().getResource("shader/bright-ff.glsl")));
        bright_f.setComment("Gathers bright pixels.");
        shader.addFunction(bright_f);

        gaussian_f = new Function(
                Shader.Type.VEC4,
                "blur_gaussian",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.SAMPLER2D, "sampler", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC2, "texCoord", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC2, "fragSize", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.INT, "kernel", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.INT, "direction", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "offsets", null, GAUSSIAN_OFFSETS, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "weights", null, GAUSSIAN_OFFSETS, Shader.Qualifier.IN)
        );
        gaussian_f.setSource(Reader.read(getClass().getResource("shader/gaussian-ff.glsl")));
        gaussian_f.setComment("Separable Gaussian blur.");
        shader.addFunction(gaussian_f);

        horizontal_f = new Function(
                Shader.Type.VEC4,
                "blur_horizontal",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.SAMPLER2D, "sampler", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC2, "texCoord", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC2, "fragSize", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.INT, "kernel", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "offsets", null, GAUSSIAN_OFFSETS, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "weights", null, GAUSSIAN_OFFSETS, Shader.Qualifier.IN)
        );
        horizontal_f.setSource(Reader.read(getClass().getResource("shader/horizontal-ff.glsl")));
        horizontal_f.setComment("Horizontal blur.");
        shader.addFunction(horizontal_f);

        /**
         * Initialize executables.
         */
        gaussian_e = new Executable("GAUSSIAN");
        gaussian_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        gaussian_e.setSource(Shader.Stage.FRAGMENT, "output0 = blur_gaussian(utilSamplers[0], fragment.texCoord, fragmentSize, gaussianKernel, gaussianDirection, gaussianOffsets, gaussianWeights);");
        shader.addExecutable(gaussian_e);

        horizontal_e = new Executable("HORIZONTAL");
        horizontal_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        horizontal_e.setSource(Shader.Stage.FRAGMENT, "output0 = blur_horizontal(utilSamplers[0], fragment.texCoord, fragmentSize, horizontalKernel, gaussianOffsets, gaussianWeights);");
        shader.addExecutable(horizontal_e);

        bright_e = new Executable("BRIGHT");
        bright_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        bright_e.setSource(Shader.Stage.FRAGMENT, "output0 = gather_bright(utilSamplers[0], fragment.texCoord, brightThreshold);");
        shader.addExecutable(bright_e);

        buildProcessors();
    }

    @Override
    protected void init() {
        renderer.build(sourceBuffer);
        renderer.build(brightBuffer);
        renderer.build(outputBuffer);

        for (FrameBuffer downsampler : downsamplers) {
            renderer.build(downsampler);
        }

        initProcessors();
    }

    @Override
    protected void render() {
        update();

        GL.setBlendEnabled(false);
        GL.setDepthTestEnabled(false);

        outputBuffer.copyColor(sourceBuffer, 0, 0, false);

        runProcessors();

        outputBuffer.toScreen(0, width, height, false);
        renderer.resetStates();
    }

    @Override
    protected void clean() {
        cleanProcessors();
    }

    /**
     * Update frame buffers.
     */
    private void update() {
        if (renderer.isViewportResized()) {
            width = renderer.getViewportWidth();
            height = renderer.getViewportHeight();

            sourceBuffer.resize(width, height);
            brightBuffer.resize(width, height);
            outputBuffer.resize(width, height);

            for (int i = 0; i < downsamplers.length; i++) {
                int div = (int) EngineMath.pow(2f, i);
                int dsWidth = width / div;
                int dsHeight = height / div;

                downsamplers[i].resize(dsWidth, dsHeight);
            }
        }
    }

    /**
     * Build main rendering frame buffer.
     */
    private void initSourceBuffer() {
        depthBuffer = new DepthBuffer(DepthBuffer.Type.TEXTURE);
        colorBuffers = new ColorBuffer[Renderer.MAX_FRAGMENT_OUTPUTS];

        for (int i = 0; i < colorBuffers.length; i++) {
            colorBuffers[i] = new ColorBuffer(ColorBuffer.Type.RGBA, true);
        }

        colorBuffers[0].setWriteEnabled(true);
        colorBuffers[0].setClearEnabled(true);

        sourceBuffer = new FrameBuffer(GL.DEFAULT_VIEWPORT_WIDTH, GL.DEFAULT_VIEWPORT_HEIGHT, depthBuffer, colorBuffers);
    }

    /**
     * Build bright buffer.
     */
    private void initBrightBuffer() {
        brightBuffer = new FrameBuffer(
                GL.DEFAULT_VIEWPORT_WIDTH,
                GL.DEFAULT_VIEWPORT_HEIGHT,
                null,
                new ColorBuffer(ColorBuffer.Type.RGBA, true)
        );
    }

    /**
     * Build output buffer.
     */
    private void initOutputBuffer() {
        outputBuffer = new FrameBuffer(
                GL.DEFAULT_VIEWPORT_WIDTH,
                GL.DEFAULT_VIEWPORT_HEIGHT,
                null,
                new ColorBuffer(ColorBuffer.Type.RGBA, true)
        );
    }

    /**
     * Build downsampling framebuffers.
     */
    private void initDownsamplers() {
        downsamplers = new FrameBuffer[4];

        for (int i = 0; i < downsamplers.length; i++) {
            int div = (int) EngineMath.pow(2f, i);
            int dsWidth = GL.DEFAULT_VIEWPORT_WIDTH / div;
            int dsHeight = GL.DEFAULT_VIEWPORT_HEIGHT / div;

            downsamplers[i] = new FrameBuffer(
                    dsWidth,
                    dsHeight,
                    null,
                    new ColorBuffer(ColorBuffer.Type.RGBA, true),
                    new ColorBuffer(ColorBuffer.Type.RGBA, true)
            );
        }
    }

    /**
     * Load Gaussian offsets and weights.
     */
    private void initGaussian() {
        gaussianOffsets = new FloatBuffer[61];
        gaussianWeights = new FloatBuffer[61];

        for (int i = 0; i < 61; i++) {
            gaussianOffsets[i] = Buffers.createFloatBuffer(GAUSSIAN_OFFSETS);
            gaussianWeights[i] = Buffers.createFloatBuffer(GAUSSIAN_OFFSETS);
        }

        loadResource("resources/gaussian-offsets", gaussianOffsets);
        loadResource("resources/gaussian-weights", gaussianWeights);
    }

    /**
     * Reads a line of data from the given file and stores it in a float buffer.
     *
     * @param path - path to file
     * @param storages - storage float buffer array
     */
    private void loadResource(String path, FloatBuffer[] storages) {
        String[] lines = Reader.read(getClass().getResource(path)).split("\n");

        for (int i = 0; i < lines.length; i++) {
            FloatBuffer storage = storages[i];
            String[] offsets = lines[i].split(" ");

            for (String offset : offsets) {
                storage.put(Float.valueOf(offset.trim()));
            }

            storage.flip();
        }
    }

    /**
     * Reads data from the given file and stores it in a integer buffer.
     *
     * @param path - path to file
     * @param storage - storage integer buffer
     */
    private void loadResource(String path, IntBuffer storage) {
        String[] lines = Reader.read(getClass().getResource(path)).split("\n");

        for (int i = 0; i < lines.length; i++) {
            String[] offsets = lines[i].split(" ");

            for (String offset : offsets) {
                storage.put(Integer.valueOf(offset.trim()));
            }
        }

        storage.flip();
    }

    /**
     * Sets the given frame buffer for writing.
     *
     * @param fb - frame buffer to bind
     */
    private void bind(FrameBuffer fb) {
        GL.writeFrameBuffer(fb.getID());
        GL.setVector2(fragmentSize_u.getID(), fb.getFragmentWidth(), fb.getFragmentHeight());
        GL.setVector2(frameBufferSize_u.getID(), (float) fb.getWidth(), (float) fb.getWidth());
        GL.setViewport(0, 0, fb.getWidth(), fb.getHeight());
    }

    /**
     * Draws a fullscreen quad textured with the given source color buffer.
     *
     * @param source - color buffer containing image to blur
     * @param direction - blur direction
     */
    private void drawGaussianBlur(ColorBuffer source, int direction) {
        GL.writeAttachment(GL.Attachment.COLOR_0.getValue() + direction);
        GL.bindTexture2D(source.getID(), renderer.getUtilSamplerUnit(0));
        GL.setScalar(gaussianDirection_u.getID(), direction);
        renderer.drawFullscreenQuad();
    }

    /**
     * Draws a fullscreen quad textured with the given source color buffer.
     *
     * @param source - color buffer containing image to blur
     */
    private void drawHorizontalBlur(ColorBuffer source) {
        GL.writeAttachment(GL.Attachment.COLOR_0.getValue());
        GL.bindTexture2D(source.getID(), renderer.getUtilSamplerUnit(0));
        renderer.drawFullscreenQuad();
    }
}
