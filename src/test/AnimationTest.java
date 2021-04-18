package test;

import core.*;
import core.event.EngineEvent;
import core.event.MouseEvent;
import core.event.listener.EngineListener;
import core.event.listener.MouseListener;
import core.event.type.EngineEventType;
import core.event.type.MouseEventType;
import core.primitive.Box;
import core.primitive.GeoSphere;
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
import module.shape.processor.shadow.ShadowProcessor;
import core.shadow.DistantShadow;
import core.shadow.PointShadow;
import core.shadow.SpotShadow;
import core.Sky;

public class AnimationTest implements EngineListener, MouseListener {
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
    private ShadowProcessor shadowProcessor;
    private PointLight pointLight;
    private DistantLight distantLight;
    private AmbientLight ambientLight;
    private SpotLight spotLight;
    private Navigator navigator;
    private ShapeGeometry platformGeom;
    private Shape platform;
    private Transform transform;
    private DistantShadow distantShadow;
    private SpotShadow spotShadow;
    private PointShadow pointShadow;
    private ShaderLibrary shaderLibrary;
    private Sky sky;
    private TextureCube skyTexture;

    public AnimationTest() {
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
        shadowProcessor = new ShadowProcessor();
        navigator = new Navigator();
        platformGeom = new ShapeGeometry(new Box(50f, 0.5f, 50f, false));
        platform = new Shape();
        transform = new Transform();
//        distantShadow = new DistantShadow(distantLight);
//        spotShadow = new SpotShadow(spotLight);
//        pointShadow = new PointShadow(pointLight);
        shaderLibrary = new ShaderLibrary();
        sky = new Sky();
        skyTexture = new TextureCube(true);

        Image[] images = new Image[6];

        images[0] = new Image("src/test/resources/texture/sky1_right.png", false);
        images[1] = new Image("src/test/resources/texture/sky1_left.png", false);
        images[2] = new Image("src/test/resources/texture/sky1_top.png", false);
        images[3] = new Image("src/test/resources/texture/sky1_bottom.png", false);
        images[4] = new Image("src/test/resources/texture/sky1_front.png", false);
        images[5] = new Image("src/test/resources/texture/sky1_back.png", false);

        skyTexture.setImages(images);
        sky.setTexture(skyTexture);
        sky.setGeometry(new GeoSphere(3, 100f));

        animation = animImporter.importFile("src/test/resources/model/human_male.dae");
        geometry = geomImporter.importFile("src/test/resources/model/human_male.dae");

        geometry.setAnimation(animation);
        geometry.setNormalEnabled(true);
        geometry.setJointEnabled(true);

        animation.bake(60);
        animation.setType(Animation.Type.BAKED);

        platformGeom.generateNormals();
        platformGeom.setNormalEnabled(true);

        material.setLightingEnabled(true);
        material.setAmbientColor(Colors.BROWN3);
//        material.setShininess(0.25f);
//        material.setFaceCullingEnabled(false);
//        material.setShadingLevel(2);
//        material.setOpacity(0.5f);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);
        shape.setShadowCaster(true);
        shape.setName("Shape");

        transform.setTranslation(0f, -0.25f, 0f);

        platform.addGeometryDetail(platformGeom);
        platform.addMaterialDetail(new Material());
        platform.calculateBounds(platformGeom);
        platform.setTransform(transform);
        platform.setShadowReceiver(true);
//        platform.addLight(pointLight);
        platform.addLight(distantLight);
        platform.addLight(ambientLight);
//        platform.addLight(spotLight);
        platform.setName("Platform");

        distantLight.setColor(Colors.GRAY3);
        distantLight.setDirection(-1f, -1f, -1f);

        spotLight.setColor(Colors.ORANGE3);
        spotLight.setLocation(10f, 10f, 10f);
        spotLight.setDirection(-1f, -1f, -1f);
//        spotLight.setAttenuation(0.5f, 0.075f, 0.0075f);

        pointLight.setLocation(-4f, 7f, 4f);
        pointLight.setColor(Colors.CYAN3);
//        pointLight.setAttenuation(0.5f, 0.05f, 0.005f);

        ambientLight.setColor(Colors.WHITE3);

//        shape.addLight(spotLight);
//        shape.addLight(pointLight);
        shape.addLight(distantLight);
        shape.addLight(ambientLight);
        shape.setShadowReceiver(true);

//        shape.addShadow(spotShadow);
//        shape.addShadow(pointShadow);
//        shape.addShadow(distantShadow);
//        platform.addShadow(spotShadow);
//        platform.addShadow(pointShadow);
//        platform.addShadow(distantShadow);

        scene.addChild(shape);
        scene.addChild(platform);
        scene.addChild(sky);

//        distantShadow.setFilterEnabled(true);
//        distantShadow.setFilterSamples(64);
//        distantShadow.setResolution(4092);
//        distantShadow.setOpacity(1f);
//        distantShadow.setFilterDensity(100f);

//        spotShadow.setFilterEnabled(true);

//        pointShadow.setFilterEnabled(true);
//        pointShadow.setFilterDensity(1f);
//        pointShadow.setOpacity(0.25f);

        navigator.setCamera(renderer.getCamera());

        renderer.getCamera().setFarClipDistance(100f);

//        shapeRenderer.addRenderingProcessor(shadowProcessor);
        shapeRenderer.addRenderingProcessor(illuminationProcessor);


        renderer.setScene(scene);

        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);

        renderer.getTraverser().addListener(shapeRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);
//        renderer.getTraverser().addListener(shadowProcessor);

        renderer.setRenderTarget(renderer.getDefaultRenderTarget());
//        renderer.getDefaultRenderTarget().getColorBuffer(0).setClearColor(Colors.BLUE4);

//        engine.getDisplay().setSize(engine.getDisplay().getScreenWidth(), engine.getDisplay().getScreenHeight());

        engine.setCalculateFPSEnabled(true);
        engine.addListener(this);
        engine.addListener(shapeRenderer);
        engine.addListener(navigator);
        engine.getInput().getMouse().addListener(this);
        engine.getInput().getMouse().addListener(navigator);
        engine.getInput().getMouse().setGrabbed(true);
        engine.start();
    }

    public static void main(String[] args) {
        new AnimationTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            System.out.println(event.getSource().getFPS());
        }

        return true;
    }

    @Override
    public void listen(MouseEvent event) {
        if (event.getType() == MouseEventType.CLICK) {
            animation.setPaused(!animation.isPaused());
//            distantShadow.setEnabled(!distantShadow.isEnabled());
//            distantShadow.setResolution(distantShadow.getResolution() == 1024 ? 2040 : 1024);
//            distantShadow.setFilterEnabled(!distantShadow.isFilterEnabled());
//            distantShadow.setFilterDensity(distantShadow.getFilterDensity() == 100f ? 0f : 100f);
//            distantShadow.setOpacity(distantShadow.getOpacity() == 1f ? 0.25f : 1f);
//            distantShadow.setFilterSamples(distantShadow.getFilterSamples() == 64 ? 16 : 64);
        }
    }
}
