package test;

import core.Engine;
import core.Renderer;
import core.Spatial;
import core.Transform;
import core.event.EngineEvent;
import core.event.listener.EngineListener;
import core.event.type.EngineEventType;
import core.math.Vector3;
import core.primitive.Box;
import core.utility.Colors;
import core.utility.Navigator;
import module.bounds.BoundsRenderer;
import core.Material;
import core.Shape;
import core.ShapeGeometry;
import module.shader.ShaderLibrary;
import module.shape.ShapeRenderer;
import core.animation.Animation;
import core.importer.AnimationImporter;
import core.importer.GeometryImporter;
import core.importer.animation.DAEAnimationImporter;
import core.importer.geometry.DAEGeometryImporter;
import core.light.AmbientLight;
import core.light.DistantLight;
import module.shape.processor.illumination.IlluminationProcessor;

public class TranslucencyTest implements EngineListener {
    private Engine engine;
    private Renderer renderer;
    private Shape shape;
    private Spatial scene;
    private Material material;
    private ShapeGeometry geometry;
    private Animation animation;
    private GeometryImporter geomImporter;
    private AnimationImporter animImporter;
    private ShapeRenderer shapeRenderer;
    private IlluminationProcessor illuminationProcessor;
    private DistantLight distantLight;
    private AmbientLight ambientLight;
    private Navigator navigator;
    private ShapeGeometry platformGeom;
    private Shape platform;
    private Transform transform;
    private BoundsRenderer boundsRenderer;
    private ShaderLibrary shaderLibrary;

    public TranslucencyTest() {
        engine = Engine.getInstance();
        renderer = engine.getRenderer();
        shape = new Shape();
        scene = new Spatial();
        material = new Material();
        distantLight = new DistantLight();
        ambientLight = new AmbientLight();
        geomImporter = new DAEGeometryImporter(true);
        animImporter = new DAEAnimationImporter(true);
        shapeRenderer = new ShapeRenderer();
        illuminationProcessor = new IlluminationProcessor();
        navigator = new Navigator();
        platformGeom = new ShapeGeometry(new Box(50f, 0.5f, 50f, false));
        platform = new Shape();
        transform = new Transform();
        boundsRenderer = new BoundsRenderer();
        shaderLibrary = new ShaderLibrary();

        animation = animImporter.importFile("src/test/resources/model/human_male.dae");
        geometry = geomImporter.importFile("src/test/resources/model/human_male.dae");

        geometry.setAnimation(animation);
        geometry.setNormalEnabled(true);

        platformGeom.generateNormals();
        platformGeom.setNormalEnabled(true);

        material.setAmbientColor(Colors.DARK_GRAY3);
        material.setOpacity(0.5f);
        material.setFaceCullingEnabled(false);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);

        transform.setTranslation(0f, -0.25f, 0f);

        platform.addGeometryDetail(platformGeom);
        platform.addMaterialDetail(new Material());
        platform.calculateBounds(platformGeom);
        platform.setTransform(transform);
        platform.addLight(distantLight);
        platform.addLight(ambientLight);

        distantLight.setColor(Colors.WHITE3);
        distantLight.setDirection(Vector3.NEG_ONE);

        ambientLight.setColor(Colors.DARK_GRAY3);

        shape.addLight(distantLight);
        shape.addLight(ambientLight);

        Spatial branch = new Spatial();
        branch.addChild(shape);
        branch.addChild(platform);

        scene.addChild(branch);
//        scene.addChild(platform);

        navigator.setCamera(renderer.getCamera());

        shapeRenderer.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.addRenderingModule(boundsRenderer);
        renderer.getTraverser().addListener(shapeRenderer);
        renderer.getTraverser().addListener(boundsRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);
//        renderer.getDefaultRenderTarget().getColorBuffer(0).setClearColor(Colors.BLUE4);

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new TranslucencyTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
            System.out.println(event.getSource().getFPS());
        }

        return true;
    }
}
