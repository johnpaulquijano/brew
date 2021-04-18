package module.shape.processor.illumination;

import core.*;
import core.math.Matrix4;
import core.shader.*;
import core.shadow.DistantShadow;
import core.shadow.PointShadow;
import core.utility.Buffers;
import core.utility.Pools;
import core.utility.Reader;
import module.shape.*;

import java.nio.FloatBuffer;
import java.util.*;

public class IlluminationProcessor extends ShapeProcessor {
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

    private Variable numLights_u;
    private Variable lightList_u;
    private Sampler normalMap_s;
    private Sampler specularMap_s;
    private Function light_f;

    private Variable numShadows_u;
    private Variable shadowList_u;
    private Variable biasedMatrix_u;
    private Variable shadowEnabled_u;
    private Sampler shadowSamplersUni_u;
    private Sampler shadowSamplersOmni_u;
    private Function shadowUni_f;
    private Function shadowOmni_f;
    private Function shadowOmniDepth_f;
    private Executable shadowUni_e;
    private Executable shadowOmni_e;

    private Shader shader;
    private Renderer renderer;
    private ShapeRenderer module;

    private List<Shape>[] casters;
    private FloatBuffer transformBuffer;
    private FloatBuffer biasedTransformBuffer;

    public IlluminationProcessor() {
        casters = new List[6];
        transformBuffer = Buffers.createFloatBuffer(16);
        biasedTransformBuffer = Buffers.createFloatBuffer(Shape.MAX_SHADOWS * 16);

        for (int i = 0; i < 6; i++) {
            casters[i] = new ArrayList<>();
        }
    }

    @Override
    public void apply(Shape shape) {
        boolean lightingReady = shape.isLightingReady();

        if (lightingReady) {
            applyLight(shape);
            applyShadow(shape);
        }

        module.setLightingEnabled(lightingReady);
    }

    @Override
    protected void build() {
        module = (ShapeRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize varyings.
         */
        shader.addVariable(new Variable(Shader.Type.VEC4, "wCoord", null, 0, Shader.Qualifier.VARYING));
        shader.addVariable(new Variable(Shader.Type.VEC3, "normal", null, 0, Shader.Qualifier.VARYING));
        shader.addVariable(new Variable(Shader.Type.MAT3, "tbnMatrix", null, 0, Shader.Qualifier.VARYING));

        shader.addVariable(new Variable(Shader.Type.VEC4, "sCoord", null, Shape.MAX_SHADOWS, Shader.Qualifier.VARYING));

        /**
         * Initialize definitions.
         */
        shader.addDefinition("LIGHT_AMBIENT", Integer.toString(LightType.AMBIENT.getID()));
        shader.addDefinition("LIGHT_DISTANT", Integer.toString(LightType.DISTANT.getID()));
        shader.addDefinition("LIGHT_SPOT", Integer.toString(LightType.SPOT.getID()));
        shader.addDefinition("LIGHT_POINT", Integer.toString(LightType.POINT.getID()));

        shader.addDefinition("SPOT_SHADOW_PENUMBRA_SCALE", String.valueOf(SPOT_SHADOW_PENUMBRA_SCALE) + "f");
        shader.addDefinition("SPOT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(SPOT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("POINT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(POINT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("DISTANT_SHADOW_FILTER_DENSITY_SCALE", String.valueOf(DISTANT_SHADOW_FILTER_DENSITY_SCALE) + "f");
        shader.addDefinition("POINT_SHADOW_DEPTH_BIAS", String.valueOf(POINT_SHADOW_DEPTH_BIAS) + "f");
        shader.addDefinition("SHADOW_NEAR_CLIP", String.valueOf(Shadow.NEAR_CLIP) + "f");

        /**
         * Initialize uniforms.
         */
        numLights_u = new Variable(Shader.Type.INT, "numLights", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(numLights_u);

        lightList_u = new Variable(Shader.Type.INT, "lightList", null, Shape.MAX_LIGHTS, Shader.Qualifier.UNIFORM);
        shader.addVariable(lightList_u);

        normalMap_s = new Sampler(Shader.Type.SAMPLER2D, "normalMap", 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(normalMap_s);

        specularMap_s = new Sampler(Shader.Type.SAMPLER2D, "specularMap", 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(specularMap_s);

        biasedMatrix_u = new Variable(Shader.Type.MAT4, "biasedShadowMatrix", null, Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(biasedMatrix_u);

        shadowEnabled_u = new Variable(Shader.Type.BOOL, "shadowEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowEnabled_u);

        numShadows_u = new Variable(Shader.Type.INT, "numShadows", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(numShadows_u);

        shadowList_u = new Variable(Shader.Type.INT, "shadowList", null, Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowList_u);

        shadowSamplersUni_u = new Sampler(Shader.Type.SAMPLER2DSHADOW, "shadowSamplersUni", Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSamplersUni_u);

        shadowSamplersOmni_u = new Sampler(Shader.Type.SAMPLERCUBESHADOW, "shadowSamplersOmni", Shape.MAX_SHADOWS, Shader.Qualifier.UNIFORM);
        shader.addVariable(shadowSamplersOmni_u);

        /**
         * Initialize functions.
         */
        shadowUni_f = new Function(
                Shader.Type.FLOAT,
                "shadowUni",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.INT, "index", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "lightToFragDist", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "lightAttenuation", null, 0, Shader.Qualifier.IN)
        );
        shadowUni_f.setSource(Reader.read(getClass().getResource("shader/shadow/shadow-uni-ff.glsl")));
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
        shadowOmniDepth_f.setSource(Reader.read(getClass().getResource("shader/shadow/shadow-omni-depth-ff.glsl")));
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
        shadowOmni_f.setSource(Reader.read(getClass().getResource("shader/shadow/shadow-omni-ff.glsl")));
        shadowOmni_f.setComment("Calculates shadow factor for omni-directional lights.");
        shader.addFunction(shadowOmni_f);

        light_f = new Function(Shader.Type.VEC4, "illumination", Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.VEC4, "inputColor", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "inputNormal", null, 0, Shader.Qualifier.IN)
        );
        light_f.setSource(Reader.read(getClass().getResource("shader/illumination-ff.glsl")));
        shader.addFunction(light_f);

        /**
         * Initialize executables.
         */
        shadowUni_e = new Executable("SHADOW_UNI");
        shadowUni_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow/shadow-uni-v.glsl")));
        shader.addExecutable(shadowUni_e);

        shadowOmni_e = new Executable("SHADOW_OMNI");
        shadowOmni_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow/shadow-omni-v.glsl")));
        shader.addExecutable(shadowOmni_e);

        /**
         * Append shader sources to shape rendering module.
         */
        module.appendShaderSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/shadow/shadow-v.glsl")));
        module.appendShaderSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/light/light-v.glsl")));
        module.appendShaderSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/light/light-f.glsl")));
    }

    @Override
    protected void init() {
    }

    @Override
    protected void render() {
        processCasters();

        GL.writeFrameBuffer(renderer.getRenderTarget().getID());
        renderer.resetStates();
    }

    @Override
    protected void clean() {
    }

    private void processCasters() {
        for (Shadow shadow : module.getShadows()) {
            updateFrustum(shadow);
            processShadowMap(shadow);
            collectCasters(shadow);
            drawCasters(shadow);
        }
    }

    /**
     * Updates the given shadow's light view-projection frustum.
     *
     * @param shadow - shadow to update frustum
     */
    private void updateFrustum(Shadow shadow) {
        if (shadow instanceof DistantShadow) {
            ((DistantShadow) shadow).setCamera(renderer.getCamera());
        }

        shadow.updateFrustum();
    }

    /**
     * Builds or updates the given shadow's shadow map.
     *
     * @param shadow - shadow to build shadow map
     */
    private void processShadowMap(Shadow shadow) {
        if (!shadow.getShadowMap().isBuilt()) {
            renderer.build(shadow.getShadowMap());
        } else if (shadow.isResolutionDirty()) {
            shadow.getShadowMap().update();
        }
    }

    /**
     * Traverses the given scene graph to collect shadow casters.
     *
     * @param shadow - shadow to collect casters
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
     * @param shadow - shadow to draw caster
     */
    private void drawCasters(Shadow shadow) {
        shader.execute(shadow instanceof PointShadow ? shadowOmni_e : shadowUni_e);

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
     * Sends light rendering data to the shader.
     *
     * @param shape - shape to apply light on
     */
    private void applyLight(Shape shape) {
        GL.setScalar(numLights_u.getID(), shape.getLights().size());
        GL.setArray1(lightList_u.getID(), shape.getLightsBuffer());

        module.setWorldTransform(shape.getWorldTransformBuffer());
        module.setNormalTransform(shape.getNormalTransformBuffer());

        Material material = shape.getMaterialDetail(shape.getMaterialLevel());
        Texture normalMap = material.getNormalMap();
        Texture specularMap = material.getSpecularMap();

        if (shape.isNormalMappingReady() && normalMap != null) {
            if (material.isNormalMapEnabled()) {
                GL.bindTexture2D(normalMap.getID(), normalMap_s.getUnit(0));
            }
        }

        if (specularMap != null) {
            if (material.isSpecularMapEnabled()) {
                GL.bindTexture2D(specularMap.getID(), specularMap_s.getUnit(0));
            }
        }
    }

    /**
     * Sends shadow rendering data to the shader.
     *
     * @param shape - shape to apply shadows on
     */
    private void applyShadow(Shape shape) {
        if (shape.isShadowReceiver() && !shape.getShadows().isEmpty()) {
            int samplerUnit = 0;

            biasedTransformBuffer.clear();

            for (Shadow shadow : shape.getShadows()) {
                if (shadow.isEnabled() && shadow instanceof PointShadow) {
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
            GL.setMatrix4(biasedMatrix_u.getID(), false, biasedTransformBuffer);
        }

        GL.setBoolean(shadowEnabled_u.getID(), shape.isShadowReceiver() && !shape.getShadows().isEmpty());
    }
}
