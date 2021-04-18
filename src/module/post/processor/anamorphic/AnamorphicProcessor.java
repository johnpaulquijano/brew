package module.post.processor.anamorphic;

import core.GL;
import core.RenderingProcessor;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Reader;
import module.post.PostRenderer;

/**
 * Approximates the effect of horizontal streaks of light propagating from bright areas caused by using an anamorphic lens.
 *
 * @author John Paul Quijano
 */
public class AnamorphicProcessor extends RenderingProcessor {
    public static final int DEFAULT_BLUR_KERNEL = 45;
    public static final float DEFAULT_LEVEL = 0.75f;
    public static final float DEFAULT_BRIGHTNESS_THRESHOLD = 1f;

    private Shader shader;
    private PostRenderer module;

    private Variable level_u;
    private Function anamorphic_f;
    private Executable anamorphic_e;

    private int blurKernel;
    private float level;
    private float brightnessThreshold;
    private boolean levelDirty;

    public AnamorphicProcessor() {
        level = DEFAULT_LEVEL;
        blurKernel = DEFAULT_BLUR_KERNEL;
        brightnessThreshold = DEFAULT_BRIGHTNESS_THRESHOLD;

        levelDirty = true;
    }

    /**
     * Sets the intensity of the anamorphic effect.
     *
     * @param level - intensity of the anamorphic lens flare effect
     */
    public void setLevel(float level) {
        this.level = level;
        levelDirty = true;
    }

    /**
     * Gives the intensity of the anamorphic lens flare effect.
     *
     * @return intensity of the anamorphic lens flare effect
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
        level_u = new Variable(Shader.Type.FLOAT, "anamorphicLevel", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(level_u);

        /**
         * Initialize functions.
         */
        anamorphic_f = new Function(
                Shader.Type.VEC4,
                "anamorphic",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.FLOAT, "level", null, 0, Shader.Qualifier.IN)
        );
        anamorphic_f.setSource(Reader.read(getClass().getResource("shader/anamorphic-ff.glsl")));
        anamorphic_f.setComment("Renders anamorphic lens flare effect.");
        shader.addFunction(anamorphic_f);

        /**
         * Initialize executables.
         */
        anamorphic_e = new Executable("ANAMORPHIC");
        anamorphic_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        anamorphic_e.setSource(Shader.Stage.FRAGMENT, "output0 = anamorphic(anamorphicLevel);");
        shader.addExecutable(anamorphic_e);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void render() {
        update();

        module.blurHorizontal(module.gatherBright(module.getSourceBuffer().getColorBuffer(0), brightnessThreshold), blurKernel, PostRenderer.MAX_DOWNSAMPLING_LEVEL);

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(module.getDownsamplingBuffers()[PostRenderer.MAX_DOWNSAMPLING_LEVEL].getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(1));
        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
        GL.writeFrameBuffer(module.getOutputBuffer().getID());

        shader.execute(anamorphic_e);
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
