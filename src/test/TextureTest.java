package test;

import core.*;
import core.event.EngineEvent;
import core.event.MouseEvent;
import core.event.listener.EngineListener;
import core.event.listener.MouseListener;
import core.event.type.EngineEventType;
import core.event.type.MouseEventType;
import core.utility.Navigator;
import core.Material;
import core.Shape;
import core.ShapeGeometry;
import module.shader.ShaderLibrary;
import module.shape.ShapeRenderer;
import core.importer.GeometryImporter;
import core.importer.geometry.DAEGeometryImporter;
import core.light.DistantLight;
import module.shape.processor.illumination.IlluminationProcessor;
import module.shape.processor.texture.TextureProcessor;

public class TextureTest implements EngineListener, MouseListener {
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
    private TextureProcessor textureProcessor;
    private DistantLight distantLight;
    private Navigator navigator;
    private Transform transform;
    private Texture diffuseTexture;
    private Texture normalTexture;
    private Texture specularTexture;
    private ShaderLibrary shaderLibrary;

    public TextureTest() {
        engine = Engine.getInstance();
        renderer = engine.getRenderer();
        shape = new Shape();
        scene = new Spatial();
        material = new Material();
        distantLight = new DistantLight();
        geomImporter = new DAEGeometryImporter(false);
        shapeRenderer = new ShapeRenderer();
        illuminationProcessor = new IlluminationProcessor();
        textureProcessor = new TextureProcessor();
        navigator = new Navigator();
        transform = new Transform();
        diffuseTexture = new Texture(true);
        normalTexture = new Texture(true);
        specularTexture = new Texture(true);
        shaderLibrary = new ShaderLibrary();

        geometry = geomImporter.importFile("src/test/resources/model/gun.dae");

        geometry.generateTangents();
        geometry.setNormalEnabled(true);
        geometry.setTexCoordEnabled(true);
        geometry.setTangentEnabled(true);

        diffuseTexture.setImage(new Image("src/test/resources/texture/gun_diffuse.png", true));
        normalTexture.setImage(new Image("src/test/resources/texture/gun_normal.png", true));
        specularTexture.setImage(new Image("src/test/resources/texture/gun_specular.png", true));

        material.setLightingEnabled(true);
        material.addTexture(diffuseTexture);
        material.setNormalMap(normalTexture);
        material.setSpecularMap(specularTexture);
        material.setNormalMapEnabled(true);
        material.setSpecularMapEnabled(true);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);

        transform.setTranslation(0f, -0.25f, 0f);

        shape.addLight(distantLight);

        scene.addChild(shape);

        navigator.setCamera(renderer.getCamera());

        shapeRenderer.addRenderingProcessor(illuminationProcessor);
        shapeRenderer.addRenderingProcessor(textureProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.getTraverser().addListener(shapeRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);
        renderer.getTraverser().addListener(textureProcessor);
        renderer.getCamera().setLocation(0f, 0f, 20f);

        engine.getDisplay().setSize(640, 640);
        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(this);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new TextureTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
//            System.out.println(event.getSource().getFPS());

//            angle += 0.001f * event.getSource().getDeltaTime();
//            transform.setRotation(Vector3.UNIT_Y, angle);
//            shape.setTransform(transform);
        }

        return true;
    }

    @Override
    public void listen(MouseEvent event) {
        if (event.getType() == MouseEventType.PRESS) {
            System.out.println("SDFSDFSDF");
//            material.setSpecularMapEnabled(!material.isSpecularMapEnabled());
            material.setNormalMapEnabled(!material.isNormalMapEnabled());
//            shape.setMaterialDetail(0, material);
//            System.out.println("SDFSDFSDFSDF");
        }
    }
}
