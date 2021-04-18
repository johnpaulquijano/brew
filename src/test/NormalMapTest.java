package test;

import core.*;
import core.event.EngineEvent;
import core.event.listener.EngineListener;
import core.event.type.EngineEventType;
import core.utility.Navigator;
import core.Material;
import core.Shape;
import core.ShapeGeometry;
import module.shader.ShaderLibrary;
import module.shape.ShapeRenderer;
import core.importer.GeometryImporter;
import core.importer.geometry.OBJGeometryImporter;
import core.light.DistantLight;
import module.shape.processor.illumination.IlluminationProcessor;

public class NormalMapTest implements EngineListener {
    private float angle;
    private Engine engine;
    private Renderer renderer;
    private Shape shape;
    private Spatial scene;
    private Material material;
    private ShapeGeometry geometry;
    private GeometryImporter geomImporter;
    private ShapeRenderer shapeRenderer;
    private IlluminationProcessor illuminationProcessor;
    private DistantLight distantLight;
    private Navigator navigator;
    private Transform transform;
    private Texture normalMap;
    private ShaderLibrary shaderLibrary;

    public NormalMapTest() {
        engine = Engine.getInstance();
        renderer = engine.getRenderer();
        shape = new Shape();
        scene = new Spatial();
        material = new Material();
        distantLight = new DistantLight();
        geomImporter = new OBJGeometryImporter();
        shapeRenderer = new ShapeRenderer();
        illuminationProcessor = new IlluminationProcessor();
        navigator = new Navigator();
        transform = new Transform();
        normalMap = new Texture(true);
        shaderLibrary = new ShaderLibrary();
        geometry = geomImporter.importFile("src/test/resources/model/earth.obj");

        geometry.generateTangents();
        geometry.setNormalEnabled(true);
        geometry.setTexCoordEnabled(true);
        geometry.setTangentEnabled(true);

        normalMap.setImage(new Image("src/test/resources/texture/earth_normal.png", true));

        material.setLightingEnabled(true);
        material.setNormalMap(normalMap);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);
        shape.addLight(distantLight);

        transform.setTranslation(0f, -0.25f, 0f);

        scene.addChild(shape);

        navigator.setCamera(renderer.getCamera());

        shapeRenderer.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.getTraverser().addListener(shapeRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);
        renderer.getCamera().setLocation(0f, 0f, 20f);

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new NormalMapTest();
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
