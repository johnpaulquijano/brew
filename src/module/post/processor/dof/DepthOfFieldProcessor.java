package module.post.processor.dof;

import core.GL;
import core.RenderingProcessor;
import core.framebuffer.ColorBuffer;
import core.framebuffer.FrameBuffer;
import core.math.EngineMath;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Reader;
import module.post.PostRenderer;

/**
 * Camera depth-of-field effect.
 *
 * @author John Paul Quijano
 */
public class DepthOfFieldProcessor extends RenderingProcessor {
    public static final int MIN_DISK_BLUR_SAMPLES = 4;
    public static final int MAX_DISK_BLUR_SAMPLES = 64;

    public static final float DEFAULT_FIELD_RANGE = 6f;
    public static final float DEFAULT_FOCAL_DISTANCE = 12f;
    public static final int DEFAULT_DISK_BLUR_SAMPLES = 32;
    public static final float DEFAULT_DISK_BLUR_RADIUS = 10f;

    private static final int GAUSSIAN_BLUR_LEVEL = 2;
    private static final int GAUSSIAN_BLUR_KERNEL = 9;

    private Shader shader;
    private PostRenderer module;

    private Variable diskBlurRadius_u;
    private Variable diskBlurSamples_u;
    private Variable fieldRange_u;
    private Variable focalDistance_u;
    private Variable diskBlurEnabled_u;
    private Function dof_coc_f;
    private Function dof_blur_f;
    private Executable dof_coc_e;
    private Executable dof_blur_e;

    private int diskBlurSamples;
    private float diskBlurRadius;
    private float fieldRange;
    private float focalDistance;
    private boolean diskBlurEnabled;
    private boolean diskBlurEnabledDirty;
    private boolean diskBlurRadiusDirty;
    private boolean diskBlurSamplesDirty;
    private boolean fieldRangeDirty;
    private boolean focalDistanceDirty;
    private FrameBuffer cocBuffer;

    public DepthOfFieldProcessor() {
        diskBlurEnabled = true;
        diskBlurRadius = DEFAULT_DISK_BLUR_RADIUS;
        diskBlurSamples = DEFAULT_DISK_BLUR_SAMPLES;
        fieldRange = DEFAULT_FIELD_RANGE;
        focalDistance = DEFAULT_FOCAL_DISTANCE;

        cocBuffer = new FrameBuffer(
                GL.DEFAULT_VIEWPORT_WIDTH,
                GL.DEFAULT_VIEWPORT_HEIGHT,
                null,
                new ColorBuffer(ColorBuffer.Type.RGBA, true)
        );

        fieldRangeDirty = true;
        focalDistanceDirty = true;
        diskBlurRadiusDirty = true;
        diskBlurSamplesDirty = true;
        diskBlurEnabledDirty = true;
    }

    /**
     * Sets the distance from the focal point to the near and far planes delimiting the in-focus region.
     *
     * @param range - field range to set
     */
    public void setFieldRange(float range) {
        fieldRange = range;
        fieldRangeDirty = true;
    }

    /**
     * Gives the field range.
     *
     * @return field range
     */
    public float getFieldRange() {
        return fieldRange;
    }

    /**
     * Sets the distance from the camera's near clipping plane to the focal point used in depth-of-field calculations.
     *
     * @param distance - focal distance to set
     */
    public void setFocalDistance(float distance) {
        focalDistance = distance;
        focalDistanceDirty = true;
    }

    /**
     * Gives focal distance.
     *
     * @return focal distance
     */
    public float getFocalDistance() {
        return focalDistance;
    }

    /**
     * If enabled, a Poisson disk blur is applied to out-of-focus regions. This gives edges smooth transition from
     * sharp to blurry.
     *
     * @param enabled - if enabled, a Poisson disk blur is applied to out-of-focus regions
     */
    public void setDiskBlurEnabled(boolean enabled) {
        diskBlurEnabled = enabled;
        diskBlurEnabledDirty = true;
    }

    /**
     * Checks if disk blurring is enabled.
     *
     * @return true if disk blurring is enabled
     */
    public boolean isDiskBlurEnabled() {
        return diskBlurEnabled;
    }

    /**
     * Sets the number of samples used in the Poisson disk blurring algorithm. Input is clamped between MIN_BLUR_SAMPLES
     * and MAX_BLUR_SAMPLES, inclusively.
     *
     * @param samples - number of samples
     */
    public void setDiskBlurSamples(int samples) {
        diskBlurSamples = EngineMath.clamp(samples, MIN_DISK_BLUR_SAMPLES, MAX_DISK_BLUR_SAMPLES);
        diskBlurSamplesDirty = true;
    }

    /**
     * Gives the number of samples to use in the disk blurring algorithm.
     *
     * @return number of samples to use in the disk blurring algorithm
     */
    public int getDiskBlurSamples() {
        return diskBlurSamples;
    }

    /**
     * Sets the radius of the Poisson disk used in the blurring algorithm.
     *
     * @param radius - disk radius
     */
    public void setDiskBlurRadius(float radius) {
        diskBlurRadius = radius;
        diskBlurRadiusDirty = true;
    }

    /**
     * Gives the radius of the disk blurring kernel.
     *
     * @return adius of the disk blurring kernel
     */
    public float getDiskBlurRadius() {
        return diskBlurRadius;
    }

    @Override
    protected void build() {
        module = (PostRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        fieldRange_u = new Variable(Shader.Type.FLOAT, "dofFieldRange", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(fieldRange_u);

        focalDistance_u = new Variable(Shader.Type.FLOAT, "dofFocalDistance", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(focalDistance_u);

        diskBlurEnabled_u = new Variable(Shader.Type.BOOL, "dofDiskBlurEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(diskBlurEnabled_u);

        diskBlurRadius_u = new Variable(Shader.Type.FLOAT, "dofDiskBlurRadius", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(diskBlurRadius_u);

        diskBlurSamples_u = new Variable(Shader.Type.INT, "dofDiskBlurSamples", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(diskBlurSamples_u);


        /**
         * Initialize functions.
         */
        dof_coc_f = new Function(
                Shader.Type.VEC4,
                "dof_coc",
                Shader.Stage.FRAGMENT
        );
        dof_coc_f.setSource(Reader.read(getClass().getResource("shader/dof-coc-ff.glsl")));
        dof_coc_f.setComment("Approximates depth-of-field circle of confusion factor.");
        shader.addFunction(dof_coc_f);

        dof_blur_f = new Function(
                Shader.Type.VEC4,
                "dof_blur",
                Shader.Stage.FRAGMENT
        );
        dof_blur_f.setSource(Reader.read(getClass().getResource("shader/dof-blur-ff.glsl")));
        dof_blur_f.setComment("Blurs the input image based on the circle-of-confusion factor.");
        shader.addFunction(dof_blur_f);

        /**
         * Initialize executables.
         */
        dof_coc_e = new Executable("DOF_COC");
        dof_coc_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        dof_coc_e.setSource(Shader.Stage.FRAGMENT, "output0 = dof_coc();");
        shader.addExecutable(dof_coc_e);

        dof_blur_e = new Executable("DOF_BLUR");
        dof_blur_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        dof_blur_e.setSource(Shader.Stage.FRAGMENT, "output0 = dof_blur();");
        shader.addExecutable(dof_blur_e);
    }

    @Override
    protected void init() {
        renderer.build(cocBuffer);
    }

    @Override
    protected void render() {
        update();

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(module.getSourceBuffer().getDepthBuffer().getID(), renderer.getUtilSamplerUnit(1));
        GL.writeFrameBuffer(cocBuffer.getID());

        shader.execute(dof_coc_e);
        renderer.drawFullscreenQuad();

        ColorBuffer blur = module.blurGaussian(cocBuffer.getColorBuffer(0), GAUSSIAN_BLUR_KERNEL, GAUSSIAN_BLUR_LEVEL);

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(blur.getID(), renderer.getUtilSamplerUnit(1));
        GL.writeFrameBuffer(module.getOutputBuffer().getID());
        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());

        shader.execute(dof_blur_e);
        renderer.drawFullscreenQuad();
    }

    @Override
    protected void clean() {
        fieldRangeDirty = false;
        focalDistanceDirty = false;
        diskBlurRadiusDirty = false;
        diskBlurSamplesDirty = false;
        diskBlurEnabledDirty = false;
    }

    /**
     * Update framebuffer dimensions and shader uniforms.
     */
    private void update() {
        if (renderer.isViewportResized()) {
            cocBuffer.resize(renderer.getViewportWidth(), renderer.getViewportHeight());
        }

        if (fieldRangeDirty) {
            GL.setScalar(fieldRange_u.getID(), fieldRange);
        }

        if (focalDistanceDirty) {
            GL.setScalar(focalDistance_u.getID(), focalDistance);
        }

        if (diskBlurEnabledDirty) {
            GL.setBoolean(diskBlurEnabled_u.getID(), diskBlurEnabled);
        }

        if (diskBlurRadiusDirty) {
            GL.setScalar(diskBlurRadius_u.getID(), diskBlurRadius);
        }

        if (diskBlurSamplesDirty) {
            GL.setScalar(diskBlurSamples_u.getID(), diskBlurSamples);
        }
    }
}
