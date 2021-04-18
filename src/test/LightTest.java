package test;

import core.Engine;
import core.Renderer;
import core.Spatial;
import core.Transform;
import core.event.EngineEvent;
import core.event.listener.EngineListener;
import core.event.type.EngineEventType;
import core.primitive.Box;
import core.utility.Colors;
import core.utility.Navigator;
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
import core.light.PointLight;
import core.light.SpotLight;
import module.shape.processor.illumination.IlluminationProcessor;

public class LightTest implements EngineListener {
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
    private PointLight pointLight;
    private DistantLight distantLight;
    private AmbientLight ambientLight;
    private SpotLight spotLight;
    private Navigator navigator;
    private ShapeGeometry platformGeom;
    private Shape platform;
    private Transform transform;
    private ShaderLibrary shaderLibrary;

    public LightTest() {
        engine = Engine.getInstance();
        renderer = engine.getRenderer();
        shape = new Shape();
        scene = new Spatial();
        material = new Material();
        pointLight = new PointLight();
        distantLight = new DistantLight();
        spotLight = new SpotLight();
        ambientLight = new AmbientLight();
        geomImporter = new DAEGeometryImporter(true);
        animImporter = new DAEAnimationImporter(true);
        shapeRenderer = new ShapeRenderer();
        illuminationProcessor = new IlluminationProcessor();
        navigator = new Navigator();
        platformGeom = new ShapeGeometry(new Box(50f, 0.5f, 50f, false));
        platform = new Shape();
        transform = new Transform();
        shaderLibrary = new ShaderLibrary();

        animation = animImporter.importFile("src/test/resources/model/human_male.dae");
        geometry = geomImporter.importFile("src/test/resources/model/human_male.dae");

        geometry.setAnimation(animation);
        geometry.setNormalEnabled(true);

        platformGeom.generateNormals();
        platformGeom.setNormalEnabled(true);

        material.setLightingEnabled(true);
        material.setAmbientColor(Colors.GRAY3);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);
        shape.setName("Shape");

        transform.setTranslation(0f, -0.25f, 0f);

        platform.addGeometryDetail(platformGeom);
        platform.addMaterialDetail(material);
        platform.calculateBounds(platformGeom);
        platform.setTransform(transform);
        platform.addLight(spotLight);
        platform.addLight(pointLight);
        platform.addLight(distantLight);
        platform.addLight(ambientLight);
        platform.setName("Platform");

        distantLight.setColor(Colors.GRAY3);

        spotLight.setColor(Colors.BLUE3);
        spotLight.setLocation(-6f, 10f, 4f);
        spotLight.setDirection(1f, -1f, -1f);

        pointLight.setLocation(0f, 0f, 4f);
        pointLight.setColor(Colors.GREEN3);

        ambientLight.setColor(Colors.ORANGE3);

        shape.addLight(spotLight);
        shape.addLight(pointLight);
        shape.addLight(distantLight);
        shape.addLight(ambientLight);

        scene.addChild(shape);
        scene.addChild(platform);

        navigator.setCamera(renderer.getCamera());

        shapeRenderer.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.getTraverser().addListener(shapeRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new LightTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
//            System.out.println(event.getSource().getFPS());
        }

        return true;
    }
}
