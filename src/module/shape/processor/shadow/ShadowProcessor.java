package module.shape.processor.shadow;

import core.*;
import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.math.EngineMath;
import core.math.Matrix4;
import core.math.Vector3;
import core.shader.*;
import core.utility.Buffers;
import core.utility.Pools;
import core.utility.Reader;
import module.shape.*;

import java.nio.FloatBuffer;
import java.util.*;

/**
 * Shape processor for rendering shadows using shadow maps filtered with using Percentag Closer Filtering (PCF).
 * Uni-directional shadows are filtered using Poisson disk sampling. Omni-directional shadows are filtered with a fixed
 * 20-sample kernel using redundant vectors for sampling the cube shadow map.
 *
 * @author John Paul Quijano
 */
public class ShadowProcessor extends ShapeProcessor implements TraverserListener {
    public static final int MAX_CASCADES = 6;

    public static final int DEFAULT_NUM_CASCADES = 4;
    public static final float DEFAULT_CASCADE_WEIGHT = 0.75f;

    public static final float SPOT_SHADOW_FILTER_DENSITY_SCALE = 4f;
    public static final float POINT_SHADOW_FILTER_DENSITY_SCALE = 0.000005f;
    public static final float DISTANT_SHADOW_FILTER_DENSITY_SCALE = 16f;

    public static final float SPOT_SHADOW_PENUMBRA_SCALE = 0.001f;
    public static final float POINT_SHADOW_DEPTH_BIAS = 0.0002f;

    private static final Matrix4 biasMatrix = new Matrix4(
        0.5f, 0f, 0f, 0f,
        0f, 0.5f, 0f, 0f,
        0f, 0f, 0.5f, 0f,
        0.5f, 0.5f, 0.5f, 1f
    );

    private Variable numShadows_u;
    private Variable shadowList_u;
    private Variable shadowSourceList_u;
    private Variable biasedMatrix_u;
    private Variable shadowEnabled_u;
    private Variable cascadeSplits_u;
    private Sampler shadowSamplersUni_u;
    private Sampler shadowSamplersOmni_u;
    private Function shadow_f;
    private Function shadowUni_f;
    private Function shadowOmni_f;
    private Function shadowOmniDepth_f;
    private Executable shadowUni_e;
    private Executable shadowOmni_e;

    private Shader shader;
    private Renderer renderer;
    private ShapeRenderer module;
    private Set<Shadow> shadows;
    private Set<Shadow> cascaded;
    private List<Shape>[] casters;
    private FloatBuffer transformBuffer;
    private FloatBuffer biasedTransformBuffer;

    private int numCascades;
    private float cascadeWeight;
    private boolean numCascadesDirty;
    private boolean cascadeWeightDirty;
    private Camera[] cascades;
    private FloatBuffer cascadeSplitsBuffer;

    public ShadowProcessor() {
        casters = new List[6];
        shadows = new HashSet<>();
        transformBuffer = Buffers.createFloatBuffer(16);
        biasedTransformBuffer = Buffers.createFloatBuffer(Shape.MAX_SHADOWS * 16);

        cascades = new Camera[MAX_CASCADES];
        cascaded = new HashSet<>();
        cascadeWeight = DEFAULT_CASCADE_WEIGHT;
        numCascades = DEFAULT_NUM_CASCADES;
        cascadeSplitsBuffer = Buffers.createFloatBuffer(MAX_CASCADES);

        for (int i = 0; i < MAX_CASCADES; i++) {
            cascades[i] = new Camera();
        }

        for (int i = 0; i < 6; i++) {
            casters[i] = new ArrayList<>();
        }
    }

    /**
     * Sets the number of cascades to use. Input is clamped between 2 and MAX_CASCADES, inclusively.
     *
     * @param num - number of cascades
     */
    public void setNumCascades(int num) {
        numCascades = EngineMath.clamp(num, 2, MAX_CASCADES);
        numCascadesDirty = true;
        numCascadesDirty = true;
    }

    /**
     * Gives the number of cascades to use.
     *
     * @return number of cascades to use
     */
    public int getNumCascades() {
        return numCascades;
    }

    /**
     * Sets the modulating factor for calculating cascade splits.
     *
     * @param weight - modulating factor for calculating cascade splits
     */
    public void setCascadeWeight(float weight) {
        cascadeWeight = weight;
        cascadeWeightDirty = true;
    }

    /**
     * Gives the modulating factor for calculating cascade splits.
     *
     * @return modulating factor for calculating cascade splits
     */
    public boolean isCascadeWeightDirty() {
        return cascadeWeightDirty;
    }

    @Override
    public void apply(Shape shape) {
        applyShadow(shape);
    }

    @Override
    protected void build() {
        module = (ShapeRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize definitions.
         */
        shader.addDefinition("SPOT_SHADOW_PENUMBRA_SCALE", String.valueOf(SPOT_SHADOW_PENUMBRA_SCALE) + "f");
        shader.addDefinition("SPOT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(SPOT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("POINT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(POINT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("DISTANT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(DISTANT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("POINT_SHADOW_DEPTH_BIAS", String.valueOf(POINT_SHADOW_DEPTH_BIAS) + "f");
        shader.addDefinition("SHADOW_NEAR_CLIP", String.valueOf(Shadow.NEAR_CLIP) + "f");

        /**
         * Initialize varyings.
         */
        shader.addVariable(new Variable(Shader.Type.VEC4, "sCoord", null, Shape.MAX_SHADOWS, Shader.Qualifier.VARYING));

        /**
         * Initialize uniforms.
         */
        biasedMatrix_u = new Variable(Shader.Type.MAT4, "biasedShadowMatrix", null, Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(biasedMatrix_u);

        shadowEnabled_u = new Variable(Shader.Type.BOOL, "shadowEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowEnabled_u);

        numShadows_u = new Variable(Shader.Type.INT, "numShadows", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(numShadows_u);

        shadowList_u = new Variable(Shader.Type.INT, "shadowList", null, Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowList_u);

        shadowSourceList_u = new Variable(Shader.Type.INT, "shadowSourceList", null, Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSourceList_u);

        cascadeSplits_u = new Variable(Shader.Type.FLOAT, "cascadeSplits", null, MAX_CASCADES, Shader.Qualifier.UNIFORM);
        shader.addVariable(cascadeSplits_u);

        shadowSamplersUni_u = new Sampler(Shader.Type.SAMPLER2DSHADOW, "shadowSamplersUni", Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSamplersUni_u);

        shadowSamplersOmni_u = new Sampler(Shader.Type.SAMPLERCUBESHADOW, "shadowSamplersOmni", Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSamplersOmni_u);

        shadowUni_f = new Function(
                Shader.Type.FLOAT,
                "shadowUni",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.INT, "index", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "lightToFragDist", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "lightAttenuation", null, 0, Shader.Qualifier.IN)
        );
        shadowUni_f.setSource(Reader.read(getClass().getResource("shader/shadow-uni-ff.glsl")));
        shadowUni_f.setComment("Calculates shadow factor for uni-directional lights.");
        shader.addFunction(shadowUni_f);

        shadowOmniDepth_f = new Function(
                Shader.Type.FLOAT,
                "shadowOmniDepth",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.INT, "index", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "lightToFragDir", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "clipDist", null, 0, Shader.Qualifier.IN)
        );
        shadowOmniDepth_f.setSource(Reader.read(getClass().getResource("shader/shadow-omni-depth-ff.glsl")));
        shadowOmniDepth_f.setComment("Calculates the z-coordinate for sampling a cube shadow texture.");
        shader.addFunction(shadowOmniDepth_f);

        shadowOmni_f = new Function(
                Shader.Type.FLOAT,
                "shadowOmni",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.INT, "index", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "lightToFragNorm", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "lightToFragDir", null, 0, Shader.Qualifier.IN)
        );
        shadowOmni_f.setSource(Reader.read(getClass().getResource("shader/shadow-omni-ff.glsl")));
        shadowOmni_f.setComment("Calculates shadow factor for omni-directional lights.");
        shader.addFunction(shadowOmni_f);

        shadow_f = new Function(
                Shader.Type.FLOAT,
                "shadow",
                Shader.Stage.FRAGMENT
        );
        shadow_f.setSource(Reader.read(getClass().getResource("shader/shadow-ff.glsl")));
        shadow_f.setComment("Renders shadows from uni and omni-directional light sources.");
        shader.addFunction(shadow_f);

        /**
         * Initialize executables.
         */
        shadowUni_e = new Executable("SHADOW_UNI");
        shadowUni_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow-uni-v.glsl")));
        shader.addExecutable(shadowUni_e);

        shadowOmni_e = new Executable("SHADOW_OMNI");
        shadowOmni_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow-omni-v.glsl")));
        shader.addExecutable(shadowOmni_e);

        module.appendShaderSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow-v.glsl")));
        module.appendShaderSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/shadow-f.glsl")));
    }

    @Override
    protected void init() {
    }

    @Override
    protected void render() {
        updateCascades(renderer.getCamera());
        processCasters();

        GL.writeFrameBuffer(renderer.getRenderTarget().getID());
        renderer.resetStates();
    }

    @Override
    protected void clean() {
        for (Shadow shadow : shadows) {
            shadow.clean();
        }

        if (!cascaded.isEmpty()) {
            for (Camera cascade : cascades) {
                cascade.clean();
            }
        }

        shadows.clear();
        cascaded.clear();

        numCascadesDirty = false;
        cascadeWeightDirty = false;
    }

    @Override
    public boolean listen(TraverserEvent event) {
//        if (event.getType() == TraverserEventType.LEAF) {
//            if (event.getSource().getCurrent() instanceof Shape) {
//                Shape shape = (Shape) event.getSource().getCurrent();
//
//                if (shape.isShadowReceiver()) {
//                    for (Shadow shadow : shape.getShadows()) {
//                        if (shadow.isCascaded()) {
//                            cascaded.add(shadow);
//                        }
//
//                        shadows.add(shadow);
//                    }
//                }
//            }
//        }
//
        return false;
    }

    /**
     * Draws shadow casters onto shadow maps.
     */
    private void processCasters() {
//        for (Shadow shadow : shadows) {
//            if (shadow.isEnabled()) {
//                Camera camera = renderer.getCamera();
//
//                if (shadow.isCascaded()) {
//                    camera = cascades[shadow.getCascadeIndex()];
//                }
//
//                shadow.update(renderer, camera);
//                collectCasters(shadow);
//                drawCasters(shadow);
//            } else {
//                shadow.getShadowMap().clearBuffers();
//            }
//        }
    }

    /**
     * Traverses the given scene graph to collect shadow casters.
     *
     * @param shadow - shadow to collect casters for
     *
     * @return array of sets of shapes
     */
    private void collectCasters(Shadow shadow) {
        for (int i = 0; i < shadow.numBuffers(); i++) {
            Spatial current = renderer.getScene();

            casters[i].clear();

            while (current != null) {
                if (current.isLeaf() || !current.hasNext()) {
                    if (current instanceof Shape) {
                        Shape shape = (Shape) current;

                        if (shape.isShadowCaster() && shadow.getCameras()[i].intersects(shape.getWorldBounds())) {
                            casters[i].add(shape);
                        }
                    }

                    current.resetNext();
                    current = current.getParent();
                } else {
                    if (!shadow.getCameras()[i].intersects(current.getWorldBounds())) {
                        current = current.getParent();
                        continue;
                    }

                    current = current.next();
                }
            }
        }
    }

    /**
     * Draws shadow casters onto the shadow map.
     *
     * @param shadow - shadow to draw caster for
     */
    private void drawCasters(Shadow shadow) {
        shader.execute(shadow.getSource().getType() == LightType.POINT ? shadowOmni_e : shadowUni_e);

        for (int i = 0; i < shadow.numBuffers(); i++) {
            if (!casters[i].isEmpty()) {
                shadow.getShadowMap().initDraw(i);

                for (Shape caster : casters[i]) {
                    applyAnimation(caster);
                    processGeometry(caster);
                    calculateCasterTransform(caster, shadow.getCameras()[i]);
                    renderer.setWVPMatrix(transformBuffer);
                    caster.getGeometryDetail(caster.getGeometryLevel()).draw();
                }
            }
        }
    }

    /**
     * Applies animation to shadow casters.
     *
     * @param caster - shape to apply animation
     */
    private void applyAnimation(Shape caster) {
        boolean animate = caster.isAnimationReady();

        if (animate) {
            module.setAnimation(caster.getGeometryDetail(caster.getGeometryLevel()).getAnimation());
        }

        module.setAnimationEnabled(animate);
    }

    /**
     * Shape geometries are built only upon passing frustum intersection test. Some shadow casters that have not been
     * viewed yet should be built or updated.
     *
     * @param caster - shadow caster
     */
    private void processGeometry(Shape caster) {
        ShapeGeometry geometry = caster.getGeometryDetail(caster.getGeometryLevel());

        if (geometry.getID() == 0) {
            renderer.build(geometry);
        } else if (geometry.isDirty()) {
            geometry.update();
        }
    }

    /**
     * Calculates light-space transformation for the given shadow caster.
     *
     * @param caster - shadow caster
     * @param camera - shadow camera
     */
    private void calculateCasterTransform(Shape caster, Camera camera) {
        Matrix4 matrix = Pools.Matrix4.get();
        transformBuffer = matrix.set(caster.getWorldTransformMatrix()).multiply(camera.getViewProjectionMatrix()).toFloatBuffer(transformBuffer);
        Pools.Matrix4.put(matrix);
    }

    /**
     * Calculates biased light-space transformation for the given shadow receiver.
     *
     * @param receiver - shadow receiver
     * @param camera - shadow camera
     */
    private void calculateReceiverTransform(Shape receiver, Camera camera) {
        Matrix4 matrix = Pools.Matrix4.get();
        transformBuffer = matrix.set(receiver.getWorldTransformMatrix()).multiply(camera.getViewProjectionMatrix()).multiply(biasMatrix).toFloatBuffer(transformBuffer);
        Pools.Matrix4.put(matrix);
    }

    /**
     * Sends data related to shadow rendering to the shader.
     *
     * @param shape - shape to apply shadows on
     */
    private void applyShadow(Shape shape) {
        if (shape.isShadowReceiver() && !shape.getShadows().isEmpty()) {
            int samplerUnit = 0;

            biasedTransformBuffer.clear();

            for (Shadow shadow : shape.getShadows()) {
                if (shadow.isEnabled() && shadow.getSource().getType() != LightType.POINT) {
                    GL.bindTexture2D(shadow.getShadowMap().getID(), shadowSamplersUni_u.getUnit(samplerUnit++));
                    calculateReceiverTransform(shape, shadow.getCameras()[0]);
                    biasedTransformBuffer.put(transformBuffer);
                } else {
                    GL.bindTextureCube(shadow.getShadowMap().getID(), shadowSamplersOmni_u.getUnit(samplerUnit++));
                    biasedTransformBuffer.position(biasedTransformBuffer.position() + 16);
                }
            }

            biasedTransformBuffer.flip();

            GL.setScalar(numShadows_u.getID(), shape.getShadows().size());
            GL.setArray1(shadowList_u.getID(), shape.getShadowsBuffer());
//            GL.setArray1(shadowSourceList_u.getID(), shape.getShadowSourcesBuffer());
            GL.setMatrix4(biasedMatrix_u.getID(), false, biasedTransformBuffer);
        }

        GL.setBoolean(shadowEnabled_u.getID(), shape.isShadowReceiver() && !shape.getShadows().isEmpty());
    }

    /**
     * Calculates cascaded frustums from the given camera.
     *
     * @param camera - main viewing camera
     */
    private void updateCascades(Camera camera) {
        if (!cascaded.isEmpty()) {
            for (int i = 0; i < numCascades; i++) {
                Camera cascade = cascades[i];

                if (camera.isFrustumDirty() || numCascadesDirty || cascadeWeightDirty) {
                    float farClip = camera.getFarClipDistance();
                    float nearClip = camera.getNearClipDistance();

                    cascade.set(camera);

                    float uniform = nearClip + (farClip - nearClip) * (float) (i + 1) / numCascades;
                    float logarithmic = nearClip * (EngineMath.pow(farClip / nearClip, (float) (i + 1) / numCascades));
                    float clip = cascadeWeight * uniform + (1f - cascadeWeight) * logarithmic;

                    cascade.setFarClipDistance(clip);

                    if (i > 0) {
                        cascade.setNearClipDistance(cascades[i - 1].getFarClipDistance());
                    }

                    cascadeSplitsBuffer.put(i, clip / camera.getFarClipDistance());
                }

                if (camera.isDirty()) {
                    cascade.setLocation(camera.getLocation());
                    cascade.lookAlong(camera.getDirection(), Vector3.UNIT_Y);
                    cascade.updateViewProjection();
                }
            }
        }

        GL.setArray1(cascadeSplits_u.getID(), cascadeSplitsBuffer);
    }
}
