package test;

import core.Engine;
import core.Renderer;
import core.Spatial;
import core.Transform;
import core.event.EngineEvent;
import core.event.listener.EngineListener;
import core.event.type.EngineEventType;
import core.math.EngineMath;
import core.math.Vector3;
import core.primitive.Torus;
import core.utility.Navigator;
import module.bounds.BoundsRenderer;
import module.shader.ShaderLibrary;
import core.Material;
import core.Shape;
import core.ShapeGeometry;
import module.shape.ShapeRenderer;
import core.light.DistantLight;
import module.shape.processor.illumination.IlluminationProcessor;

public class TraversalTest implements EngineListener {
    private float angle;
    private Engine engine;
    private Renderer renderer;
    private Spatial scene;
    private ShapeRenderer shapeRenderer;
    private BoundsRenderer boundsRenderer;
    private IlluminationProcessor illuminationProcessor;
    private Navigator navigator;
    private DistantLight light;
    private ShaderLibrary shaderLibrary;

    public TraversalTest() {
        engine = Engine.getInstance();
        renderer = engine.getRenderer();
        scene = new Spatial();
        shapeRenderer = new ShapeRenderer();
        boundsRenderer = new BoundsRenderer();
        illuminationProcessor = new IlluminationProcessor();
        navigator = new Navigator();
        light = new DistantLight();
        shaderLibrary = new ShaderLibrary();

        navigator.setCamera(renderer.getCamera());

        for (int i = 0; i < 10; i++) {
            Spatial group = new Spatial();

            for (int j = 0; j < 20; j++) {
                Shape shape = new Shape();
                Material material = new Material();
                Transform transform = new Transform();
                ShapeGeometry geometry = new ShapeGeometry(new Torus());
                float x = EngineMath.RANDOM.nextInt(40);
                float y = EngineMath.RANDOM.nextInt(40);
                float z = EngineMath.RANDOM.nextInt(40);
                float r = EngineMath.RANDOM.nextInt(256) / (float) 256;
                float g = EngineMath.RANDOM.nextInt(256) / (float) 256;
                float b = EngineMath.RANDOM.nextInt(256) / (float) 256;

                material.setLightingEnabled(true);
                material.setDiffuseColor(r, g, b);

                geometry.generateNormals();
                geometry.setNormalEnabled(true);

                transform.setTranslation(x, y, z);
                transform.setRotation(r, g, b);

                shape.setTransform(transform);
                shape.addMaterialDetail(material);
                shape.addGeometryDetail(geometry);
                shape.calculateBounds(geometry);
                shape.addLight(light);

                group.addChild(shape);
            }

            Transform transform = new Transform();
            float x = EngineMath.RANDOM.nextInt(100);
            float y = EngineMath.RANDOM.nextInt(100);
            float z = EngineMath.RANDOM.nextInt(100);

            transform.setTranslation(x, y, z);
            transform.setRotation(Vector3.ONE.normalize(), EngineMath.RANDOM.nextFloat() * EngineMath.TWO_PI);
            group.setTransform(transform);

            scene.addChild(group);
        }

        shapeRenderer.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.addRenderingModule(boundsRenderer);
        renderer.getTraverser().addListener(shapeRenderer);
        renderer.getTraverser().addListener(boundsRenderer);
//        renderer.setClearColor(Colors.DARK_GRAY4);

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getDisplay().setSize(800, 800);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new TraversalTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            Transform transform = scene.getChild(0).getLocalTransform();

            angle += 0.0005 * event.getSource().getDeltaTime();
            transform.setRotation(Vector3.ONE.normalize(), angle);
            scene.getChild(0).setTransform(transform);

            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
            System.out.println(event.getSource().getFPS());
        }

        return true;
    }
}
