package module.post.processor.volumetric;

import core.GL;
import core.RenderingProcessor;
import core.framebuffer.ColorBuffer;
import core.framebuffer.FrameBuffer;
import core.math.Matrix4;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Buffers;
import core.utility.Pools;
import core.utility.Reader;
import module.post.PostRenderer;
import core.shadow.DistantShadow;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

/**
 * Approximates volumetric light.
 *
 * @author John Paul Quijano
 */
public class VolumetricProcessor extends RenderingProcessor {
    public static final int MAX_SOURCES = 6;

    public static final int DEFAULT_STEPS = 64;
    public static final float DEFAULT_LEVEL = 1f;
    public static final float DEFAULT_FACTOR = 0.25f;

    private static final Matrix4 biasMatrix = new Matrix4(
            0.5f, 0f, 0f, 0f,
            0f, 0.5f, 0f, 0f,
            0f, 0f, 0.5f, 0f,
            0.5f, 0.5f, 0.5f, 1f
    );

    private Shader shader;
    private PostRenderer module;

    private Variable steps_u;
    private Variable level_u;
    private Variable factor_u;
    private Variable lightSources_u;
    private Variable shadowSources_u;
    private Variable numSources_u;
    private Variable shadowMatrices_u;
    private Function accum_f;
    private Function blend_f;
    private Executable accum_e;
    private Executable blend_e;

    private int steps;
    private float level;
    private float factor;
    private boolean stepsDirty;
    private boolean levelDirty;
    private boolean factorDirty;
    private boolean sourcesDirty;
    private FrameBuffer accumBuffer;
    private IntBuffer lightSourcesBuffer;
    private IntBuffer shadowSourcesBuffer;
    private FloatBuffer vpInverseBuffer;
    private FloatBuffer shadowMatricesBuffer;
    private List<DistantShadow> sources;
    private Map<DistantShadow, FloatBuffer> shadowMatrixCache;

    public VolumetricProcessor() {
        steps = DEFAULT_STEPS;
        level = DEFAULT_LEVEL;
        factor = DEFAULT_FACTOR;
        sources = new ArrayList<>();
        shadowMatrixCache = new HashMap<>();
        lightSourcesBuffer = Buffers.createIntBuffer(MAX_SOURCES);
        shadowSourcesBuffer = Buffers.createIntBuffer(MAX_SOURCES);
        vpInverseBuffer = Buffers.createFloatBuffer(16);
        shadowMatricesBuffer = Buffers.createFloatBuffer(16 * MAX_SOURCES);
        accumBuffer = new FrameBuffer(
                GL.DEFAULT_VIEWPORT_WIDTH,
                GL.DEFAULT_VIEWPORT_HEIGHT,
                null,
                new ColorBuffer(ColorBuffer.Type.RGBA, true)
        );

        stepsDirty = true;
        levelDirty = true;
        factorDirty = true;
    }

    /**
     * Adds a volumetric shadow source.
     *
     * @param source - volumetric shadow source
     */
    public void addSource(DistantShadow source) {
        sources.add(source);
        sourcesDirty = true;
    }

    /**
     * Removes the volumetric shadow source at the given index.
     *
     * @param index - index of source to remove
     */
    public void removeSource(int index) {
        sourcesDirty = sources.remove(index) != null;
    }

    /**
     * Removes the give volumetric shadow source.
     *
     * @param source - shadow source to remove
     */
    public void removeSource(DistantShadow source) {
        sourcesDirty = sources.remove(source);
    }

    /**
     * Sets the number of steps used in the ray-marching algorithm for accumulating volumetric samples.
     *
     * @param steps - number of samples used in the ray-marching algorithm
     */
    public void setSteps(int steps) {
        this.steps = steps;
        stepsDirty = true;
    }

    /**
     * Gives the number of steps used in the ray-marching algorithm for accumulating volumetric samples.
     *
     * @return number of samples used in the ray-marching algorithm
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Sets the intensity of the effect.
     *
     * @param level - intensity of the effect
     */
    public void setLevel(float level) {
        this.level = level;
        levelDirty = true;
    }

    /**
     * Gives the intensity of the effect.
     *
     * @return intensity of the effect
     */
    public float getLevel() {
        return level;
    }

    /**
     * Sets the scattering factor used in the Henyey-Greenstein phase function calculation.
     *
     * @param factor - scattering factor
     */
    public void setFactor(float factor) {
        this.factor = factor;
        factorDirty = true;
    }

    /**
     * Gives the scattering factor used in the Henyey-Greenstein phase function calculation.
     *
     * @return scattering factor
     */
    public float getFactor() {
        return factor;
    }

    @Override
    protected void build() {
        module = (PostRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        level_u = new Variable(Shader.Type.FLOAT, "volumetricLevel", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(level_u);

        factor_u = new Variable(Shader.Type.FLOAT, "volumetricFactor", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(factor_u);

        steps_u = new Variable(Shader.Type.INT, "volumetricSteps", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(steps_u);

        lightSources_u = new Variable(Shader.Type.INT, "volumetricLightSources", null, MAX_SOURCES, Shader.Qualifier.UNIFORM);
        shader.addVariable(lightSources_u);

        shadowSources_u = new Variable(Shader.Type.INT, "volumetricShadowSources", null, MAX_SOURCES, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSources_u);

        numSources_u = new Variable(Shader.Type.INT, "numVolumetricSources", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(numSources_u);

        shadowMatrices_u = new Variable(Shader.Type.MAT4, "volumetricShadowMatrices", null, MAX_SOURCES, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowMatrices_u);


        /**
         * Initialize functions.
         */
        accum_f = new Function(
                Shader.Type.VEC4,
                "volumetric_accum",
                Shader.Stage.FRAGMENT
        );
        accum_f.setSource(Reader.read(getClass().getResource("shader/volumetric-accum-ff.glsl")));
        accum_f.setComment("Accumulates volumetric samples.");
        shader.addFunction(accum_f);

        blend_f = new Function(
                Shader.Type.VEC4,
                "volumetric_blend",
                Shader.Stage.FRAGMENT
        );
        blend_f.setSource(Reader.read(getClass().getResource("shader/volumetric-blend-ff.glsl")));
        blend_f.setComment("Blends accumulated volumetric samples with the scene.");
        shader.addFunction(blend_f);

        /**
         * Initialize executables.
         */
        accum_e = new Executable("VOLUMETRIC_ACCUM");
        accum_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        accum_e.setSource(Shader.Stage.FRAGMENT, "output0 = volumetric_accum();");
        shader.addExecutable(accum_e);

        blend_e = new Executable("VOLUMETRIC_BLEND");
        blend_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        blend_e.setSource(Shader.Stage.FRAGMENT, "output0 = volumetric_blend();");
        shader.addExecutable(blend_e);
    }

    @Override
    protected void init() {
        renderer.build(accumBuffer);
    }

    @Override
    protected void render() {
        update();

        GL.bindTexture2D(module.getSourceBuffer().getDepthBuffer().getID(), renderer.getUtilSamplerUnit(0));

        for (int i = 0; i < sources.size(); i++) {
            GL.bindTexture2D(sources.get(i).getShadowMap().getID(), renderer.getUtilSamplerUnit(i + 1));
        }

        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
        GL.writeFrameBuffer(accumBuffer.getID());

        shader.execute(accum_e);
        renderer.drawFullscreenQuad();

        ColorBuffer accumBlur = module.blurGaussian(accumBuffer.getColorBuffer(0), 9, 0);

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(accumBlur.getID(), renderer.getUtilSamplerUnit(1));
        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
        GL.writeFrameBuffer(module.getOutputBuffer().getID());

        shader.execute(blend_e);
        renderer.drawFullscreenQuad();
    }

    @Override
    protected void clean() {
        stepsDirty = false;
        levelDirty = false;
        factorDirty = false;
        sourcesDirty = false;
    }

    private void update() {
        Matrix4 matrix = Pools.Matrix4.get();

        if (renderer.isViewportResized()) {
            accumBuffer.resize(renderer.getViewportWidth(), renderer.getViewportHeight());
        }

        if (stepsDirty) {
            GL.setScalar(steps_u.getID(), steps);
        }

        if (levelDirty) {
            GL.setScalar(level_u.getID(), level);
        }

        if (factorDirty) {
            GL.setScalar(factor_u.getID(), factor);
        }

        if (sourcesDirty) {
            lightSourcesBuffer.clear();
            shadowSourcesBuffer.clear();

            for (DistantShadow source : sources) {
                lightSourcesBuffer.put(source.getSource().getIndex());
                shadowSourcesBuffer.put(source.getIndex());
            }

            lightSourcesBuffer.flip();
            shadowSourcesBuffer.flip();

            GL.setArray1(lightSources_u.getID(), lightSourcesBuffer);
            GL.setArray1(shadowSources_u.getID(), shadowSourcesBuffer);
            GL.setScalar(numSources_u.getID(), sources.size());
        }

        if (renderer.getCamera().isDirty()) {
            matrix.set(renderer.getCamera().getViewProjectionMatrix()).invert().toFloatBuffer(vpInverseBuffer);
            renderer.setVPMatrixInverse(vpInverseBuffer);
        }

        shadowMatricesBuffer.clear();

        for (DistantShadow source : sources) {
            FloatBuffer buffer = shadowMatrixCache.get(source);

            if (source.getCameras()[0].isDirty()) {
                buffer = matrix.set(source.getCameras()[0].getViewProjectionMatrix()).multiply(biasMatrix).toFloatBuffer(buffer);
                shadowMatrixCache.put(source, buffer);
            }

            shadowMatricesBuffer.put(buffer);
        }

        shadowMatricesBuffer.flip();
        GL.setMatrix4(shadowMatrices_u.getID(), false, shadowMatricesBuffer);

        Pools.Matrix4.put(matrix);
    }
}
