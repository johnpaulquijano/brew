package module.shape.processor.environment;

import core.*;
import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.math.Matrix4;
import core.shader.Function;
import core.shader.Sampler;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.EngineException;
import core.utility.Pools;
import core.utility.Reader;
import module.shape.ShapeProcessor;
import module.shape.ShapeRenderer;
import module.sky.SkyRenderer;

import java.util.*;

public class ReflectionProcessor extends ShapeProcessor implements TraverserListener {
    private Shader shader;
    private Renderer renderer;
    private ShapeRenderer module;
    private SkyRenderer skyRenderer;

    private Sampler environmentMap_s;
    private Function reflection_f;

    private List<Shape> reflectors;
    private List<Sky>[] reflectableSkies;
    private List<Shape>[] reflectableShapes;
    private List<RenderingProcessor> processors;
    private Map<Shape, EnvironmentMap> envMapCache;

    public ReflectionProcessor(SkyRenderer skyRenderer) {
        this.skyRenderer = skyRenderer;

        reflectableSkies = new List[6];
        reflectableShapes = new List[6];
        reflectors = new ArrayList<>();
        processors = new ArrayList<>();
        envMapCache = new HashMap<>();

        for (int i = 0; i < 6; i++) {
            reflectableSkies[i] = new ArrayList<>();
            reflectableShapes[i] = new ArrayList<>();
        }
    }

    /**
     * Sets the given environment map for the given shape.
     *
     * @param shape - owner shape
     * @param map - environment map
     */
    public void setEnvironmentMap(Shape shape, EnvironmentMap map) {
        envMapCache.put(shape, map);
    }

    /**
     * Gives the environment map for the given shape.
     *
     * @param shape - owner of the environment map
     *
     * @return environment map for the given shape
     */
    public EnvironmentMap getEnvironmentMap(Shape shape) {
        return envMapCache.get(shape);
    }

    /**
     * Attaches a rendering processor used in rendering on the environment map.
     *
     * @param processor - rendering processor to add
     */
    public void addRenderingProcessor(RenderingProcessor processor) {
        if (initialized) {
            throw new EngineException("Cannot add a rendering processor after initialization.");
        }

        if (processors.contains(processor)) {
            throw new EngineException("Rendering processor already exists.");
        }

        processors.add(processor);
    }

    /**
     * Gives the rendering processor at the given index.
     *
     * @param index - rendering processor position in the list
     *
     * @return rendering processor at the given index
     */
    public RenderingProcessor getRenderingProcessor(int index) {
        return processors.get(index);
    }

    /**
     * Gives the number of attached rendering processors.
     *
     * @return number rendering processors
     */
    public int numRenderingProcessors() {
        return processors.size();
    }

    @Override
    public void apply(Shape shape) {
        Material material = shape.getMaterialDetail(shape.getMaterialLevel());
        EnvironmentMap environmentMap = envMapCache.get(shape);

        if (environmentMap != null) {
            if (material.isReflectionEnabled() || material.isRefractionEnabled()) {
                GL.bindTextureCube(environmentMap.getID(), environmentMap_s.getUnit(0));
            }
        }
    }

    @Override
    protected void build() {
        module = (ShapeRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        environmentMap_s = new Sampler(Shader.Type.SAMPLERCUBE, "environmentMap", 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(environmentMap_s);

        /**
         * Initialize functions.
         */
        reflection_f = new Function(
                Shader.Type.VEC4,
                "reflection",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.VEC4, "inputColor", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.VEC3, "inputNormal", null, 0, Shader.Qualifier.IN),
                new Sampler(Shader.Type.SAMPLERCUBE, "environmentSampler", 0, Shader.Qualifier.IN)
        );
        reflection_f.setSource(Reader.read(getClass().getResource("shader/reflection-ff.glsl")));
        reflection_f.setComment("Renders environment reflections.");
        shader.addFunction(reflection_f);

        module.appendShaderSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/reflection-f.glsl")));
    }

    @Override
    protected void init() {

    }

    @Override
    protected void render() {
        for (Shape reflector : reflectors) {
            EnvironmentMap map = envMapCache.get(reflector);

            if (reflector.isTransformDirty()) {
                map.setLocation(reflector.getWorldBounds().getCenter());
            }

            if (!map.isBuilt()) {
                renderer.build(map);
            } else if (map.isDirty()) {
                map.update();
            }

            for (int i = 0; i < 6; i++) {
                map.collectReflectables(renderer.getScene(), reflector);
                map.initDraw(i);

                renderReflectables(i, module, map.getReflectableShapes(i), map);
                skyRenderer.renderSkies(map.getReflectableSkies(i));
            }
        }

        GL.writeFrameBuffer(renderer.getRenderTarget().getID());
        renderer.resetStates();
    }

    @Override
    protected void clean() {
        for (Shape reflector : reflectors) {
            envMapCache.get(reflector).clean();
        }

        reflectors.clear();
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.LEAF) {
            if (event.getSource().getCurrent() instanceof Shape) {
                Shape shape = (Shape) event.getSource().getCurrent();
                Material material = shape.getMaterialDetail(shape.getMaterialLevel());

                if (material.isReflectionEnabled() || material.isRefractionEnabled()) {
                    if (!envMapCache.containsKey(shape)) {
                        envMapCache.put(shape, new EnvironmentMap());
                    }

                    reflectors.add(shape);
                }
            }
        }

        return false;
    }

    private void renderReflectables(int index, ShapeRenderer renderer, List<Shape> reflectables, EnvironmentMap map) {
        for (Shape shape : reflectables) {
            Matrix4 matrix = Pools.Matrix4.get();
            matrix.set(shape.getWorldTransformMatrix()).multiply(map.getCamera(index).getViewProjectionMatrix());
            renderer.renderShape(shape, matrix.toFloatBuffer(null), 0, 0, processors);
            Pools.Matrix4.put(matrix);
        }
    }
}
