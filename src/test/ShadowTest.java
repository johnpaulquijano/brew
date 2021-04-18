package test;

import core.*;
import core.event.EngineEvent;
import core.event.MouseEvent;
import core.event.listener.EngineListener;
import core.event.listener.MouseListener;
import core.event.type.EngineEventType;
import core.event.type.MouseEventType;
import core.primitive.Box;
import core.primitive.Sphere;
import core.utility.Colors;
import core.utility.Navigator;
import module.shader.ShaderLibrary;
import module.shape.*;
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
import core.shadow.DistantShadow;
import core.shadow.PointShadow;
import core.shadow.SpotShadow;

public class ShadowTest implements EngineListener, MouseListener {
    private Engine engine;
    private Renderer renderer;
    private Shape shape;
    private Spatial scene;
    private Material material;
    private ShapeGeometry geometry;
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
    private Shape wallLeft;
    private Shape wallRight;
    private Shape wallBack;
    private Shape wallRoof;
    private ShapeGeometry wallLeftGeom;
    private ShapeGeometry wallRightGeom;
    private ShapeGeometry wallBackGeom;
    private ShapeGeometry wallRoofGeom;
    private DistantShadow distantShadow;

    public ShadowTest() {
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
        platformGeom = new ShapeGeometry(new Box(20f, 0.5f, 20f, false));
        wallLeftGeom = new ShapeGeometry(new Box(0.5f, 20f, 20f, false));
        wallRightGeom = new ShapeGeometry(new Box(0.5f, 20f, 20f, false));
        wallBackGeom = new ShapeGeometry(new Box(20f, 20f, 0.5f, false));
        wallRoofGeom = new ShapeGeometry(new Box(20f, 0.5f, 20f, false));
        platform = new Shape();
        wallLeft = new Shape();
        wallRight = new Shape();
        wallBack = new Shape();
        wallRoof = new Shape();
        transform = new Transform();
        shaderLibrary = new ShaderLibrary();

        geometry = geomImporter.importFile("src/test/resources/model/human_male.dae");

        geometry.setNormalEnabled(true);

        platformGeom.generateNormals();
        platformGeom.setNormalEnabled(true);

        wallLeftGeom.generateNormals();
        wallLeftGeom.setNormalEnabled(true);

        wallRightGeom.generateNormals();
        wallRightGeom.setNormalEnabled(true);

        wallBackGeom.generateNormals();
        wallBackGeom.setNormalEnabled(true);

        wallRoofGeom.generateNormals();
        wallRoofGeom.setNormalEnabled(true);

        material.setLightingEnabled(true);
        material.setAmbientColor(Colors.DARK_GRAY3);
//        material.setShininess(0.25f);
//        material.setOpacity(0.5f);
//        material.setShadingLevel(2);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);
        shape.setShadowCaster(true);

        transform.setTranslation(0f, -0.25f, 0f);

        platform.addGeometryDetail(platformGeom);
        platform.addMaterialDetail(new Material());
        platform.calculateBounds(platformGeom);
        platform.setTransform(transform);
        platform.setShadowCaster(true);
        platform.setShadowReceiver(true);
        platform.addLight(pointLight);
        platform.addLight(distantLight);
        platform.addLight(ambientLight);
        platform.addLight(spotLight);

        transform.setTranslation(-10f, 10f, 0f);

        wallLeft.addGeometryDetail(wallLeftGeom);
        wallLeft.addMaterialDetail(new Material());
        wallLeft.calculateBounds(wallLeftGeom);
        wallLeft.setTransform(transform);
        wallLeft.setShadowCaster(true);
        wallLeft.setShadowReceiver(true);
        wallLeft.addLight(pointLight);
        wallLeft.addLight(distantLight);
        wallLeft.addLight(ambientLight);
        wallLeft.addLight(spotLight);

        transform.setTranslation(10f, 10f, 0f);

        wallRight.addGeometryDetail(wallRightGeom);
        wallRight.addMaterialDetail(new Material());
        wallRight.calculateBounds(wallRightGeom);
        wallRight.setTransform(transform);
        wallRight.setShadowCaster(true);
        wallRight.setShadowReceiver(true);
        wallRight.addLight(pointLight);
        wallRight.addLight(distantLight);
        wallRight.addLight(ambientLight);
        wallRight.addLight(spotLight);
        wallRight.setName("RIGHT");

        transform.setTranslation(0f, 10f, -10f);

        wallBack.addGeometryDetail(wallBackGeom);
        wallBack.addMaterialDetail(new Material());
        wallBack.calculateBounds(wallBackGeom);
        wallBack.setTransform(transform);
        wallBack.setShadowCaster(true);
        wallBack.setShadowReceiver(true);
        wallBack.addLight(pointLight);
        wallBack.addLight(distantLight);
        wallBack.addLight(ambientLight);
        wallBack.addLight(spotLight);

        transform.setTranslation(0f, 20f, 0f);

        wallRoof.addGeometryDetail(wallRoofGeom);
        wallRoof.addMaterialDetail(new Material());
        wallRoof.calculateBounds(wallRoofGeom);
        wallRoof.setTransform(transform);
        wallRoof.setShadowCaster(true);
        wallRoof.setShadowReceiver(true);
        wallRoof.addLight(pointLight);
        wallRoof.addLight(distantLight);
        wallRoof.addLight(ambientLight);
        wallRoof.addLight(spotLight);
        wallRoof.setName("ROOF");

        distantLight.setColor(Colors.GRAY3);
        distantLight.setDirection(0f, -1f, -1f);
        distantLight.setShadow(distantShadow);

        spotLight.setColor(Colors.MAGENTA3);
        spotLight.setLocation(18f, 8f, 7f);
        spotLight.lookAt(0f, 1.5f, 0f);
        spotLight.setAttenuation(0.75f, 0.075f, 0.0075f);

        pointLight.setLocation(-4f, 7f, 4f);
        pointLight.setColor(Colors.CYAN3);
        pointLight.setAttenuation(1f, 0.1f, 0.01f);

        ambientLight.setColor(Colors.WHITE3);

        shape.addLight(spotLight);
        shape.addLight(pointLight);
        shape.addLight(distantLight);
        shape.addLight(ambientLight);
        shape.setShadowReceiver(true);

        Shape sphere = new Shape();
        ShapeGeometry sphereGeom = new ShapeGeometry(new Sphere());
        Material sphereMat = new Material();

        sphereGeom.generateNormals();
        sphereGeom.setNormalEnabled(true);
        sphereMat.setLightingEnabled(true);

        transform.setTranslation(-5f, 10f, 0f);

        sphere.addLight(pointLight);
        sphere.addGeometryDetail(sphereGeom);
        sphere.addMaterialDetail(sphereMat);
        sphere.setTransform(transform);
        sphere.setShadowCaster(true);
        sphere.setShadowReceiver(true);

        scene.addChild(shape);
        scene.addChild(platform);
        scene.addChild(wallLeft);
        scene.addChild(wallRight);
        scene.addChild(wallBack);
        scene.addChild(wallRoof);
        scene.addChild(sphere);

        navigator.setCamera(renderer.getCamera());

        renderer.getCamera().setFarClipDistance(100f);

        shapeRenderer.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);
        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.getTraverser().addListener(shapeRenderer);

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(this);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new ShadowTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
//            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
//            System.out.println(event.getSource().getFPS());
        } else if (event.getType() == EngineEventType.LOOP_END) {
//            event.getSource().getRenderer().setRenderTarget(event.getSource().getRenderer().getDefaultRenderTarget());
            System.out.println(event.getSource().getFPS());
        }

        return true;
    }

    @Override
    public void listen(MouseEvent event) {
        if (event.getType() == MouseEventType.CLICK) {
            pointLight.setAttenuation(0.5f, 0.05f, 0.005f);
//            pointShadow.setEnabled(!pointShadow.isEnabled());
//            distantShadow.setEnabled(!distantShadow.isEnabled());
//            spotShadow.setEnabled(!spotShadow.isEnabled());
//            distantShadow.setResolution(distantShadow.getResolution() == 1024 ? 2040 : 1024);
//            distantShadow.setFilterEnabled(!distantShadow.isFilterEnabled());
//            distantShadow.setFilterDensity(distantShadow.getFilterDensity() == 100f ? 0f : 100f);
//            distantShadow.setOpacity(distantShadow.getOpacity() == 1f ? 0.25f : 1f);
//            distantShadow.setFilterSamples(distantShadow.getFilterSamples() == 64 ? 16 : 64);
        }
    }
}
