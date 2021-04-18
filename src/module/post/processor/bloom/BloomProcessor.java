package module.post.processor.bloom;

import core.GL;
import core.RenderingProcessor;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Reader;
import module.post.PostRenderer;

/**
 * Approximates the effect of light bouncing off lenses inside a camera or the eye, causing bright areas to extend
 * through objects.
 *
 * @author John Paul Quijano
 */
public class BloomProcessor extends RenderingProcessor {
    public static final int DEFAULT_BLUR_KERNEL = 9;
    public static final float DEFAULT_LEVEL = 0.75f;
    public static final float DEFAULT_BRIGHTNESS_THRESHOLD = 1f;

    private Shader shader;
    private PostRenderer module;

    private Variable level_u;
    private Function bloom_f;
    private Executable bloom_e;

    private int blurKernel;
    private float level;
    private float brightnessThreshold;
    private boolean levelDirty;

    public BloomProcessor() {
        level = DEFAULT_LEVEL;
        blurKernel = DEFAULT_BLUR_KERNEL;
        brightnessThreshold = DEFAULT_BRIGHTNESS_THRESHOLD;

        levelDirty = true;
    }

    /**
     * Sets the intensity of the bloom effect.
     *
     * @param level - intensity of the bloom effect
     */
    public void setLevel(float level) {
        this.level = level;
        levelDirty = true;
    }

    /**
     * Gives the intensity of the bloom effect.
     *
     * @return intensity of the bloom effect
     */
    public float getLevel() {
        return level;
    }

    /**
     * Sets the size of the kernel used in the blurring algorithm.
     *
     * @param kernel - size of the kernel used in the blurring algorithm
     */
    public void setBlurKernel(int kernel) {
        blurKernel = kernel;
    }

    /**
     * Gives the kernel used in the blurring algorithm.
     *
     * @return kernel used in the blurring algorithm
     */
    public int getBlurKernel() {
        return blurKernel;
    }

    /**
     * Sets the brightness threshold used in filtering bright parts of the scene.
     *
     * @param threshold - brightness threshold
     */
    public void setBrightnessThreshold(float threshold) {
        brightnessThreshold = threshold;
    }

    /**
     * Gives the brightness threshold used in filtering bright parts of the scene.
     *
     * @return brightness threshold
     */
    public float getBrightnessThreshold() {
        return brightnessThreshold;
    }

    @Override
    protected void build() {
        module = (PostRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        level_u = new Variable(Shader.Type.FLOAT, "bloomLevel", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(level_u);

        /**
         * Initialize functions.
         */
        bloom_f = new Function(
                Shader.Type.VEC4,
                "bloom",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.FLOAT, "level", null, 0, Shader.Qualifier.IN)
        );
        bloom_f.setSource(Reader.read(getClass().getResource("shader/bloom-ff.glsl")));
        bloom_f.setComment("Renders bloom effect.");
        shader.addFunction(bloom_f);

        /**
         * Initialize executables.
         */
        bloom_e = new Executable("BLOOM");
        bloom_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        bloom_e.setSource(Shader.Stage.FRAGMENT, "output0 = bloom(bloomLevel);");
        shader.addExecutable(bloom_e);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void render() {
        update();

        module.blurGaussian(module.gatherBright(module.getSourceBuffer().getColorBuffer(0), brightnessThreshold), blurKernel, PostRenderer.MAX_DOWNSAMPLING_LEVEL);

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(module.getDownsamplingBuffers()[PostRenderer.MAX_DOWNSAMPLING_LEVEL].getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(1));
        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
        GL.writeFrameBuffer(module.getOutputBuffer().getID());

        shader.execute(bloom_e);
        renderer.drawFullscreenQuad();
    }

    @Override
    protected void clean() {
        levelDirty = false;
    }

    /**
     * Update uniforms.
     */
    private void update() {
        if (levelDirty) {
            GL.setScalar(level_u.getID(), level);
        }
    }
}
