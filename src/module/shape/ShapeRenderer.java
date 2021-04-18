package module.shape;

import core.*;
import core.event.EngineEvent;
import core.event.TraverserEvent;
import core.event.listener.EngineListener;
import core.event.listener.TraverserListener;
import core.event.type.EngineEventType;
import core.event.type.TraverserEventType;
import core.shader.*;
import core.utility.Reader;
import core.animation.Animation;

import java.nio.FloatBuffer;
import java.util.*;

/**
 * Manages the rendering of shapes.
 *
 * @author John Paul Quijano
 */
public class ShapeRenderer extends RenderingModule implements TraverserListener, EngineListener {
    public static final int MAX_CACHED_LIGHTS = 512;
    public static final int MAX_CACHED_JOINTS = 512;
    public static final int MAX_CACHED_SHADOWS = 512;
    public static final int MAX_CACHED_MATERIALS = 512;

    private double deltaTime;
    private double systemTime;
    private boolean lightingEnabled;
    private boolean animationEnabled;

    private Shader shader;

    private Variable nMatrix_u;
    private Variable wMatrix_u;
    private Variable color_u;
    private Variable thickness_u;
    private Variable material_u;
    private Variable lightingEnabled_u;
    private Variable animationEnabled_u;
    private Function animate_f;
    private Function animate_normal_f;
    private Executable shape_e;
    private Executable contour_e;
    private Structure joint_s;
    private UniformBuffer joint_ub;


    private List<Shape> opaquesSorted;
    private List<Shape> opaquesUnsorted;
    private List<Shape> translucents;
    private Set<Light> lights;
    private Set<Shadow> shadows;
    private Set<Material> materials;
    private Set<ShapeGeometry> geometries;
    private Set<Texture> normalMaps;
    private Set<Texture> specularMaps;
    private ShaderCache<Light> lightCache;
    private ShaderCache<Shadow> shadowCache;
    private ShaderCache<Material> materialCache;

    private String appendedVertexSource;
    private String appendedFragmentSource;

    public ShapeRenderer() {
        lights = new HashSet<>();
        shadows = new HashSet<>();
        materials = new HashSet<>();
        geometries = new HashSet<>();
        normalMaps = new HashSet<>();
        specularMaps = new HashSet<>();
        opaquesSorted = new ArrayList<>();
        opaquesUnsorted = new ArrayList<>();
        translucents = new ArrayList<>();
        appendedVertexSource = "";
        appendedFragmentSource = "";
    }

    /**
     * Gives the material cache.
     *
     * @return material cache
     */
    public ShaderCache<Material> getMaterialCache() {
        return materialCache;
    }

    /**
     * Gives the light cache.
     *
     * @return light cache
     */
    public ShaderCache<Light> getLightCache() {
        return lightCache;
    }

    /**
     * Gives the shadow cache.
     *
     * @return shadow cache
     */
    public ShaderCache<Shadow> getShadowCache() {
        return shadowCache;
    }

    /**
     * Gives the list of traversed lights.
     *
     * @return list of traversed lights
     */
    public Set<Light> getLights() {
        return lights;
    }

    /**
     * Gives the list of traversed shadows.
     *
     * @return list of traversed shadows
     */
    public Set<Shadow> getShadows() {
        return shadows;
    }

    /**
     * Gives the set of traversed normal maps.
     *
     * @return set of traversed normal maps
     */
    public Set<Texture> getNormalMaps() {
        return normalMaps;
    }

    /**
     * Gives the set of traversed specular maps.
     *
     * @return set of traversed specular maps
     */
    public Set<Texture> getSpecularMaps() {
        return specularMaps;
    }

    /**
     * Gives the set of traversed materials.
     *
     * @return set of traversed materials
     */
    public Set<Material> getMaterials() {
        return materials;
    }

    /**
     * Gives the set of traversed geometries.
     *
     * @return set of traversed geometries
     */
    public Set<ShapeGeometry> getGeometries() {
        return geometries;
    }

    /**
     * Appends the given shader source code into the shape rendering executable at the given stage.
     *
     * @param stage  - shader stage to append code into
     * @param source - the source code to insert
     */
    public void appendShaderSource(Shader.Stage stage, String source) {
        if (stage == Shader.Stage.VERTEX) {
            appendedVertexSource += "\n" + source + "\n";
        } else if (stage == Shader.Stage.FRAGMENT) {
            appendedFragmentSource += "\n" + source + "\n";
        }
    }

    /**
     * Sets the shader animation data.
     *
     * @param animation - animation to set
     */
    public void setAnimation(Animation animation) {
        GL.updateUniformBuffer(joint_ub.getID(), 0, animation.update(systemTime, deltaTime).getBuffer());
    }

    /**
     * Sets the animation state in the shader.
     *
     * @param enabled - if true, animation is enabled in the shader
     */
    public void setAnimationEnabled(boolean enabled) {
        GL.setBoolean(animationEnabled_u.getID(), enabled);
        animationEnabled = enabled;
    }

    /**
     * Gives the animation state.
     *
     * @return animation enabled state
     */
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    /**
     * Sets the lighting state in the shader.
     *
     * @param enabled - if true, lighting is enabled in the shader
     */
    public void setLightingEnabled(boolean enabled) {
        GL.setBoolean(lightingEnabled_u.getID(), enabled);
        lightingEnabled = enabled;
    }

    /**
     * Gives the lighting enabled state.
     *
     * @return lighting enabled state
     */
    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    /**
     * Sets the world transformation matrix.
     *
     * @param transform - float buffer containing a 16-component matrix data
     */
    public void setWorldTransform(FloatBuffer transform) {
        GL.setMatrix4(wMatrix_u.getID(), false, transform);
    }

    /**
     * Sets the normal transformation matrix.
     *
     * @param transform - float buffer containing a 9-component matrix data
     */
    public void setNormalTransform(FloatBuffer transform) {
        GL.setMatrix3(nMatrix_u.getID(), false, transform);
    }

    /**
     * Loads the given shape's bones animation data to the shader.
     *
     * @param shape - shape to apply animation for
     */
    private void applyAnimation(Shape shape) {
        boolean animate = shape.isAnimationReady();

        if (animate) {
            setAnimation(shape.getGeometryDetail(shape.getGeometryLevel()).getAnimation());
        }

        setAnimationEnabled(animate);
    }

    /**
     * Loads the given shape's material data to the shader.
     *
     * @param shape - shape to apply material for
     * @param lod - level of detail
     */
    private void applyMaterial(Shape shape, int lod) {
        if (shape.hasMaterial()) {
            Material material = shape.getMaterialDetail(lod);

            GL.setPolygonMode(material.getPolygonMode());
            GL.setFaceCullingEnabled(material.isFaceCullingEnabled());

            if (material.isFaceCullingEnabled()) {
                GL.setCullFace(GL.CullFace.BACK);
            }

            GL.setScalar(material_u.getID(), material.getIndex());
        }
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.LEAF) {
            Camera camera = renderer.getCamera();
            Spatial leaf = event.getSource().getCurrent();

            if (leaf instanceof Shape) {
                Shape shape = (Shape) leaf;

                shape.calculateTransforms(camera);
                shape.calculateLevelOfDetail(camera);

                if (!camera.intersects(shape.getWorldBounds())) {
                    return true;
                }

                if (shape.hasMaterial()) {
                    Material material = shape.getMaterialDetail(shape.getMaterialLevel());

                    if (material.getOpacity() < 1f) {
                        translucents.add(shape);
                    } else {
                        if (shape.isSortEnabled()) {
                            opaquesSorted.add(shape);
                        } else {
                            opaquesUnsorted.add(shape);
                        }
                    }

                    if (material.getNormalMap() != null) {
                        if (material.isNormalMapEnabled()) {
                            normalMaps.add(material.getNormalMap());
                        }
                    }

                    if (material.getSpecularMap() != null) {
                        if (material.isSpecularMapEnabled()) {
                            specularMaps.add(material.getSpecularMap());
                        }
                    }

                    materials.add(material);
                } else {
                    if (shape.isSortEnabled()) {
                        opaquesSorted.add(shape);
                    } else {
                        opaquesUnsorted.add(shape);
                    }
                }

                if (shape.hasGeometry()) {
                    geometries.add(shape.getGeometryDetail(shape.getGeometryLevel()));
                }

                lights.addAll(shape.getLights());
                shadows.addAll(shape.getShadows());
            }
        }

        return false;
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.LOOP_BEGIN) {
            deltaTime = event.getSource().getDeltaTime();
            systemTime = event.getSource().getTime();
        }

        return false;
    }

    /**
     * Renders this shape.
     *
     * @param shape - shape to render
     * @param transform - transformation buffer
     * @param geometryLOD - geometry level-of-detail
     * @param materialLOD - material level-of-detail
     * @param processors - list of shape processors
     */
    public void renderShape(Shape shape, FloatBuffer transform, int geometryLOD, int materialLOD, List<RenderingProcessor> processors) {
        if (shape.hasGeometry()) {
            for (RenderingProcessor processor : processors) {
                if (processor.isEnabled()) {
                    ((ShapeProcessor) processor).apply(shape);
                }
            }

            applyAnimation(shape);
            applyMaterial(shape, materialLOD);

            shader.execute(shape_e);
            renderer.setWVPMatrix(transform);
            shape.getGeometryDetail(geometryLOD).draw();
        }
    }

    /**
     * Draws inflated back-facing geometry to show the given shape's contour and outline.
     *
     * @param shape - shape to draw contour on
     */
    public void renderContour(Shape shape) {
        if (shape.hasGeometry() && shape.hasMaterial()) {
            Material material = shape.getMaterialDetail(shape.getMaterialLevel());

            if (material.isContourEnabled()) {
                shader.execute(contour_e);

                GL.setScalar(thickness_u.getID(), material.getContourThickness() * Material.THICKNESS_SCALE);
                GL.setVector3(color_u.getID(), material.getContourColor().getX(), material.getContourColor().getY(), material.getContourColor().getZ());

                GL.setDepthWriteEnabled(true);
                GL.setFaceCullingEnabled(true);
                GL.setCullFace(GL.CullFace.FRONT);

                shape.getGeometryDetail(shape.getGeometryLevel()).draw();
            }
        }
    }

    @Override
    protected void build() {
        shader = renderer.getShader();

        /**
         * Initialize material cache.
         */
        materialCache = new ShaderCache(
                shader,
                "MATERIAL",
                "materials",
                MAX_CACHED_MATERIALS,
                Material.DATA_SIZE,
                new Variable(Shader.Type.VEC3, "ambient", null, 0),
                new Variable(Shader.Type.FLOAT, "shininess", null, 0),
                new Variable(Shader.Type.VEC3, "diffuse", null, 0),
                new Variable(Shader.Type.FLOAT, "opacity", null, 0),
                new Variable(Shader.Type.VEC3, "specular", null, 0),
                new Variable(Shader.Type.BOOL, "normalFlipped", null, 0),
                new Variable(Shader.Type.VEC3, "emissive", null, 0),
                new Variable(Shader.Type.BOOL, "geomColorUsed", null, 0),
                new Variable(Shader.Type.BOOL, "reflectionEnabled", null, 0),
                new Variable(Shader.Type.BOOL, "refractionEnabled", null, 0),
                new Variable(Shader.Type.FLOAT, "refractionIndex", null, 0),
                new Variable(Shader.Type.BOOL, "specularMapEnabled", null, 0),
                new Variable(Shader.Type.BOOL, "normalMapEnabled", null, 0),
                new Variable(Shader.Type.BOOL, "glowMapEnabled", null, 0),
                new Variable(Shader.Type.FLOAT, "shadingLevel", null, 0),
                new Variable(Shader.Type.BOOL, "contourEnabled", null, 0)
        );

        /**
         * Initialize light cache.
         */
        lightCache = new ShaderCache(
                shader,
                "LIGHT",
                "lights",
                MAX_CACHED_LIGHTS,
                Light.DATA_SIZE,
                new Variable(Shader.Type.VEC3, "color", null, 0),
                new Variable(Shader.Type.BOOL, "enabled", null, 0),
                new Variable(Shader.Type.VEC3, "location", null, 0),
                new Variable(Shader.Type.FLOAT, "type", null, 0),
                new Variable(Shader.Type.VEC3, "direction", null, 0),
                new Variable(Shader.Type.FLOAT, "cutoff", null, 0),
                new Variable(Shader.Type.VEC3, "attenuation", null, 0),
                new Variable(Shader.Type.FLOAT, "exponent", null, 0)
        );

        /**
         * Initialize shadow cache.
         */
        shadowCache = new ShaderCache(
                shader,
                "SHADOW",
                "shadows",
                MAX_CACHED_SHADOWS,
                Shadow.DATA_SIZE,
                new Variable(Shader.Type.FLOAT, "opacity", null, 0),
                new Variable(Shader.Type.FLOAT, "clip", null, 0),
                new Variable(Shader.Type.BOOL, "filtered", null, 0),
                new Variable(Shader.Type.FLOAT, "filterSamples", null, 0),
                new Variable(Shader.Type.FLOAT, "filterDensity", null, 0),
                new Variable(Shader.Type.FLOAT, "fragmentSize", null, 0),
                new Variable(Shader.Type.FLOAT, "cascadeIndex", null, 0)
        );

        /**
         * Initialize joint cache.
         */
        joint_s = new Structure("JOINT", new Variable(Shader.Type.MAT4, "transform", null, 0));
        shader.addStructure(joint_s);

        joint_ub = new UniformBuffer("jointCache", "joints", "JOINT", MAX_CACHED_JOINTS, 16);
        shader.addUniformBuffer(joint_ub);

        /**
         * Initialize definitions.
         */
        shader.addDefinition("JOINTS_PER_VERTEX", String.valueOf(ShapeGeometry.JOINTS_PER_VERTEX));

        /**
         * Initialize inputs.
         */
        shader.addVariable(new Variable(Shader.Type.VEC3, "normal", null, 0, Shader.Qualifier.IN));
        shader.addVariable(new Variable(Shader.Type.VEC3, "tangent", null, 0, Shader.Qualifier.IN));
        shader.addVariable(new Variable(Shader.Type.IVEC4, "joint", null, 0, Shader.Qualifier.IN));
        shader.addVariable(new Variable(Shader.Type.VEC4, "weight", null, 0, Shader.Qualifier.IN));

        /**
         * Initialize uniforms.
         */
        nMatrix_u = new Variable(Shader.Type.MAT3, "nMatrix", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(nMatrix_u);

        wMatrix_u = new Variable(Shader.Type.MAT4, "wMatrix", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(wMatrix_u);

        material_u = new Variable(Shader.Type.INT, "material", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(material_u);

        animationEnabled_u = new Variable(Shader.Type.BOOL, "animationEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(animationEnabled_u);

        lightingEnabled_u = new Variable(Shader.Type.BOOL, "lightEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(lightingEnabled_u);

        color_u = new Variable(Shader.Type.VEC3, "contourColor", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(color_u);

        thickness_u = new Variable(Shader.Type.FLOAT, "contourThickness", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(thickness_u);

        /**
         * Initialize functions.
         */
        animate_f = new Function(
                Shader.Type.VEC4,
                "animate",
                Shader.Stage.VERTEX,
                new Variable(Shader.Type.BOOL, "enabled", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC4, "position", null, 0, Shader.Qualifier.IN)
        );
        animate_f.setSource(Reader.read(getClass().getResource("shader/animate-fv.glsl")));
        animate_f.setComment("Applies joint and weight transformations to a vertex.");
        shader.addFunction(animate_f);

        animate_normal_f = new Function(
                Shader.Type.VEC3,
                "animate_normal",
                Shader.Stage.VERTEX,
                new Variable(Shader.Type.BOOL, "enabled", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "normal", null, 0, Shader.Qualifier.IN)
        );
        animate_normal_f.setSource(Reader.read(getClass().getResource("shader/animate-normal-fv.glsl")));
        animate_normal_f.setComment("Applies joint and weight transformations to a normal or tangent.");
        shader.addFunction(animate_normal_f);

        buildProcessors();

        /**
         * Initialize executables.
         */
        String vertexSource = Reader.read(getClass().getResource("shader/shape-ev.glsl")) + appendedVertexSource + "gl_Position = wvpMatrix * p;";
        String fragmentSource = Reader.read(getClass().getResource("shader/shape-ef.glsl")) + appendedFragmentSource + "output0 = color;";

        shape_e = new Executable("SHAPE");
        shape_e.setSource(Shader.Stage.VERTEX, vertexSource);
        shape_e.setSource(Shader.Stage.FRAGMENT, fragmentSource);
        shader.addExecutable(shape_e);

        contour_e = new Executable("CONTOUR");
        contour_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/contour-ev.glsl")));
        contour_e.setSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/contour-ef.glsl")));
        shader.addExecutable(contour_e);
    }

    @Override
    protected void init() {
        initProcessors();
    }

    @Override
    protected void render() {
        processGeometry();
        processMaterial();
        processLights();
        processShadows();
        processNormalMaps();
        processSpecularMaps();
        runProcessors();
        renderOpaquesSorted(opaquesSorted, processors);
        renderOpaquesUnsorted(opaquesUnsorted, processors);
        renderTranslucents(translucents, processors);

        renderer.resetStates();
    }

    @Override
    protected void clean() {
        for (Shape shape : opaquesSorted) {
            shape.clean();
        }

        for (Shape shape : opaquesUnsorted) {
            shape.clean();
        }

        for (Shape shape : translucents) {
            shape.clean();
        }

        for (Geometry geometry : geometries) {
            geometry.clean();
        }

        for (Material material : materials) {
            material.clean();
        }

        for (Light light : lights) {
            light.clean();
        }

        for (Shadow shadow : shadows) {
            shadow.clean();
        }

        for (Texture texture : normalMaps) {
            texture.clean();
        }

        for (Texture texture : specularMaps) {
            texture.clean();
        }

        cleanProcessors();

        lights.clear();
        shadows.clear();
        materials.clear();
        geometries.clear();
        normalMaps.clear();
        specularMaps.clear();
        opaquesSorted.clear();
        opaquesUnsorted.clear();
        translucents.clear();
    }

    /**
     * Render sorted opaque shapes.
     *
     * @param sorted - list of sorted shapes
     * @param processors - list of shape processors
     */
    private void renderOpaquesSorted(List<Shape> sorted, List<RenderingProcessor> processors) {
        Collections.sort(sorted);

        for (Shape shape : sorted) {
            renderShape(shape, shape.getWorldViewProjectionTransformBuffer(), shape.getGeometryLevel(), shape.getMaterialLevel(), processors);
            renderContour(shape);
        }
    }

    /**
     * Render unsorted opaque shapes.
     *
     * @param unsorted - list of unsorted shapes
     * @param processors - list of shape processors
     */
    private void renderOpaquesUnsorted(List<Shape> unsorted, List<RenderingProcessor> processors) {
        for (Shape shape : unsorted) {
            renderShape(shape, shape.getWorldViewProjectionTransformBuffer(), shape.getGeometryLevel(), shape.getMaterialLevel(), processors);
            renderContour(shape);
        }
    }

    /**
     * Renders translucent shapes.
     *
     * @param translucents - list of translucent shapes
     * @param processors - list of shape processors
     */
    private void renderTranslucents(List<Shape> translucents, List<RenderingProcessor> processors) {
        if (!translucents.isEmpty()) {
            Collections.sort(translucents, Comparator.reverseOrder());

            for (Shape shape : translucents) {
                Camera camera = renderer.getCamera();
                ShapeGeometry geometry = shape.getGeometryDetail(shape.getGeometryLevel());

                if (geometry.getFaces().isEmpty()) {
                    geometry.generateFaces();
                }

                if (camera.isLocationDirty() || shape.isTransformDirty() || shape.isAnimationReady()) {
                    geometry.sortFaces(camera);
                    geometry.update();
                }
            }

            GL.setBlendEnabled(true);
            GL.setBlendFunction(0, GL.BlendFunction.SRC_ALPHA, GL.BlendFunction.ONE_MINUS_SRC_ALPHA);

            for (Shape shape : translucents) {
                renderShape(shape, shape.getWorldViewProjectionTransformBuffer(), shape.getGeometryLevel(), shape.getMaterialLevel(), processors);
                renderContour(shape);
            }
        }
    }

    /**
     * Creates or updates geometries.
     */
    private void processGeometry() {
        for (ShapeGeometry geometry : geometries) {
            if (!geometry.isBuilt()) {
                renderer.build(geometry);
            } else if (geometry.isDirty()) {
                geometry.update();
            }
        }
    }

    /**
     * Caches materials or updates cache data.
     */
    private void processMaterial() {
        for (Material material : materials) {
            if (!materialCache.isCached(material)) {
                materialCache.cache(material);
            } else if (material.isDirty()) {
                materialCache.update(material);
            }
        }
    }

    /**
     * Caches lights or updates cache data.
     */
    private void processLights() {
        for (Light light : lights) {
            if (!lightCache.isCached(light)) {
                lightCache.cache(light);
            } else if (light.isDirty()) {
                lightCache.update(light);
            }
        }
    }

    /**
     * Caches shadows or updates cache data.
     */
    private void processShadows() {
        for (Shadow shadow : shadows) {
            if (!shadowCache.isCached(shadow)) {
                shadowCache.cache(shadow);
            } else if (shadow.isDirty()) {
                shadowCache.update(shadow);
            }
        }
    }

    /**
     * Builds or updates normal maps.
     */
    private void processNormalMaps() {
        for (Texture normalMap : normalMaps) {
            if (!normalMap.isBuilt()) {
                renderer.build(normalMap);
            } else if (normalMap.isDirty()) {
                normalMap.update();
            }
        }
    }

    /**
     * Builds or updates specular maps.
     */
    private void processSpecularMaps() {
        for (Texture specularMap : specularMaps) {
            if (!specularMap.isBuilt()) {
                renderer.build(specularMap);
            } else if (specularMap.isDirty()) {
                specularMap.update();
            }
        }
    }
}
