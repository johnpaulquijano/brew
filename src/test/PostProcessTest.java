package test;

import core.*;
import core.event.EngineEvent;
import core.event.MouseEvent;
import core.event.listener.EngineListener;
import core.event.listener.MouseListener;
import core.event.type.EngineEventType;
import core.event.type.MouseEventType;
import core.framebuffer.ColorBuffer;
import core.framebuffer.ColorBufferMultisampled;
import core.framebuffer.DepthBufferMultisampled;
import core.framebuffer.FrameBuffer;
import core.primitive.Box;
import core.primitive.GeoSphere;
import core.utility.Colors;
import core.utility.Navigator;
import module.post.PostRenderer;
import module.post.processor.anamorphic.AnamorphicProcessor;
import module.post.processor.bloom.BloomProcessor;
import module.post.processor.dof.DepthOfFieldProcessor;
import module.post.processor.fog.FogProcessor;
import module.shader.ShaderLibrary;
import module.shape.*;
import core.animation.Animation;
import core.importer.AnimationImporter;
import core.importer.GeometryImporter;
import core.importer.animation.DAEAnimationImporter;
import core.importer.geometry.DAEGeometryImporter;
import core.light.AmbientLight;
import core.light.DistantLight;
import module.shape.processor.environment.ReflectionProcessor;
import module.shape.processor.illumination.IlluminationProcessor;
import module.shape.processor.shadow.ShadowProcessor;
import module.post.processor.volumetric.VolumetricProcessor;
import core.shadow.DistantShadow;
import core.shadow.PointShadow;
import core.shadow.SpotShadow;
import core.Sky;
import module.sky.SkyRenderer;

public class PostProcessTest implements EngineListener, MouseListener {
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
    private DistantLight distantLight;
    private AmbientLight ambientLight;
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
    private PostRenderer postRenderer;
    private BloomProcessor bloomProcessor;
    private AnamorphicProcessor anamorphicProcessor;
    private FogProcessor fogProcessor;
    private DepthOfFieldProcessor depthOfFieldProcessor;
    private ColorBufferMultisampled colorBuffer;
    private DepthBufferMultisampled depthBuffer;
    private FrameBuffer frameBuffer;
    private VolumetricProcessor volumetricProcessor;
    private SkyRenderer skyRenderer;
    private EnvironmentMap environmentMap;
    private ReflectionProcessor reflectionProcessor;
    private Shape sphere;

    public PostProcessTest() {
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
        shadowProcessor = new ShadowProcessor();
        navigator = new Navigator();
        platformGeom = new ShapeGeometry(new Box(50f, 0.5f, 50f, false));
        platform = new Shape();
        transform = new Transform();
//        distantShadow = new DistantShadow(distantLight);
        shaderLibrary = new ShaderLibrary();
        sky = new Sky();
        skyTexture = new TextureCube(true);
        postRenderer = new PostRenderer();
        bloomProcessor = new BloomProcessor();
        anamorphicProcessor = new AnamorphicProcessor();
        fogProcessor = new FogProcessor();
        depthOfFieldProcessor = new DepthOfFieldProcessor();
        depthBuffer = new DepthBufferMultisampled(8);
        colorBuffer = new ColorBufferMultisampled(ColorBuffer.Type.RGBA, false, 8);
        frameBuffer = new FrameBuffer(GL.DEFAULT_VIEWPORT_WIDTH, GL.DEFAULT_VIEWPORT_HEIGHT, depthBuffer, colorBuffer);
        volumetricProcessor = new VolumetricProcessor();
        skyRenderer = new SkyRenderer();
        environmentMap = new EnvironmentMap();
        reflectionProcessor = new ReflectionProcessor(skyRenderer);
        sphere = new Shape();

        colorBuffer.setWriteEnabled(true);
        colorBuffer.setClearEnabled(true);

        Image[] images = new Image[6];

        images[0] = new Image("src/test/resources/texture/sky1_right.png", false);
        images[1] = new Image("src/test/resources/texture/sky1_left.png", false);
        images[2] = new Image("src/test/resources/texture/sky1_top.png", false);
        images[3] = new Image("src/test/resources/texture/sky1_bottom.png", false);
        images[4] = new Image("src/test/resources/texture/sky1_front.png", false);
        images[5] = new Image("src/test/resources/texture/sky1_back.png", false);

        skyTexture.setImages(images);
        sky.setTexture(skyTexture);
        sky.setGeometry(new ShapeGeometry(new GeoSphere(3, 100f)));

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
        material.setDiffuseColor(Colors.WHITE3);
        material.setAmbientColor(Colors.BROWN3);
        material.setShininess(0.25f);
//        material.setFaceCullingEnabled(false);
//        material.setShadingLevel(2);
//        material.setContourEnabled(true);
        material.setContourColor(Colors.BLACK3);
//        material.setOpacity(0.65f);
        material.setContourThickness(2f);
        material.setReflectionEnabled(true);

        shape.addGeometryDetail(geometry);
        shape.addMaterialDetail(material);
        shape.calculateBounds(geometry);
        shape.setShadowCaster(true);
        shape.setName("Shape");

        transform.setTranslation(0f, -0.25f, 0f);

        Material platformMaterial = new Material();
        platformMaterial.setContourEnabled(true);

        platform.addGeometryDetail(platformGeom);
        platform.addMaterialDetail(platformMaterial);
        platform.calculateBounds(platformGeom);
        platform.setTransform(transform);
        platform.setShadowReceiver(true);
//        platform.setShadowCaster(true);
//        platform.addLight(pointLight);
        platform.addLight(distantLight);
        platform.addLight(ambientLight);
//        platform.addLight(spotLight);
        platform.setName("Platform");

        transform.setTranslation(4f, 4f, 0f);

        ShapeGeometry sphereGeom = new ShapeGeometry(new GeoSphere(4, 2f));
        sphereGeom.setNormalEnabled(true);
        sphereGeom.generateNormals();

        Material sphereMat = new Material();
        sphereMat.setLightingEnabled(true);
        sphereMat.setReflectionEnabled(true);

        sphere.addGeometryDetail(sphereGeom);
        sphere.addMaterialDetail(sphereMat);
        sphere.setTransform(transform);
        sphere.addLight(distantLight);
//        sphere.addShadow(distantShadow);
        sphere.setShadowCaster(true);
        sphere.setShadowReceiver(true);

        distantLight.setColor(Colors.WHITE3);
        distantLight.setDirection(-1f, -0.75f, -1f);

//        spotLight.setAttenuation(0.5f, 0.075f, 0.0075f);
//        pointLight.setAttenuation(0.5f, 0.05f, 0.005f);

        ambientLight.setColor(Colors.WHITE3);

//        platform.addShadow(distantShadow);

//        shape.addLight(spotLight);
//        shape.addLight(pointLight);
        shape.addLight(distantLight);
        shape.addLight(ambientLight);
//        shape.addShadow(distantShadow);
        shape.setShadowReceiver(true);

//        shape.addShadow(spotShadow);
//        shape.addShadow(pointShadow);

//        platform.addShadow(spotShadow);
//        platform.addShadow(pointShadow);


        scene.addChild(shape);
        scene.addChild(sphere);
        scene.addChild(platform);
        scene.addChild(sky);

        distantShadow.setFilterEnabled(true);
//        distantShadow.setFilterSamples(64);
        distantShadow.setResolution(4092);
//        distantShadow.setOpacity(1f);
//        distantShadow.setFilterDensity(100f);

//        pointShadow.setFilterDensity(1f);
//        pointShadow.setOpacity(0.25f);

        volumetricProcessor.addSource(distantShadow);

        navigator.setCamera(renderer.getCamera());

        renderer.getCamera().setFarClipDistance(100f);

        shapeRenderer.addRenderingProcessor(shadowProcessor);
        shapeRenderer.addRenderingProcessor(illuminationProcessor);
        shapeRenderer.addRenderingProcessor(reflectionProcessor);

        reflectionProcessor.addRenderingProcessor(shadowProcessor);
        reflectionProcessor.addRenderingProcessor(illuminationProcessor);

        renderer.setScene(scene);

        postRenderer.addRenderingProcessor(bloomProcessor);
        postRenderer.addRenderingProcessor(anamorphicProcessor);
//        postRenderer.addRenderingProcessor(fogProcessor);
        postRenderer.addRenderingProcessor(volumetricProcessor);
//        postRenderer.addRenderingProcessor(depthOfFieldProcessor);

        renderer.addRenderingModule(shaderLibrary);
        renderer.addRenderingModule(shapeRenderer);
        renderer.addRenderingModule(skyRenderer);
        renderer.addRenderingModule(postRenderer);

        renderer.getTraverser().addListener(shapeRenderer);
        renderer.getTraverser().addListener(skyRenderer);
//        renderer.getTraverser().addListener(illuminationProcessor);
        renderer.getTraverser().addListener(shadowProcessor);
        renderer.getTraverser().addListener(reflectionProcessor);

//        renderer.setRenderTarget(frameBuffer);
        renderer.setRenderTarget(postRenderer.getSourceBuffer());
//        renderer.setRenderTarget(renderer.getDefaultRenderTarget());
//        renderer.setClearColor(Colors.BLACK4);

//        engine.getDisplay().setSize(engine.getDisplay().getScreenWidth(), engine.getDisplay().getScreenHeight());

        renderer.getCamera().setLocation(0f, 4f, 4f);

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
        new PostProcessTest();
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            renderer.build(frameBuffer);

            System.out.println(event.getSource().getRenderer().getShader().getVertexSource());
            System.out.println(event.getSource().getRenderer().getShader().getFragmentSource());
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            System.out.println(event.getSource().getFPS());
        } else if (event.getType() == EngineEventType.LOOP_END) {
//            frameBuffer.toScreen(0, GL.DEFAULT_VIEWPORT_WIDTH, GL.DEFAULT_VIEWPORT_HEIGHT, false);
        }

        return true;
    }

    @Override
    public void listen(MouseEvent event) {
        if (event.getType() == MouseEventType.CLICK) {
//            volumetricProcessor.setLevel(1.25f);
//            animation.setPaused(!animation.isPaused());
//            volumetricProcessor.setSteps(1000);
//            material.setContourEnabled(!material.isContourEnabled());
//            contourProcessor.setThickness(0.5f);
//            depthOfFieldProcessor.setDiskBlurEnabled(!depthOfFieldProcessor.isDiskBlurEnabled());
//            fogProcessor.setEnabled(!fogProcessor.isEnabled());
//            anamorphicProcessor.setEnabled(!anamorphicProcessor.isEnabled());
//            bloomProcessor.setEnabled(!bloomProcessor.isEnabled());
//            postRenderer.setEnabled(!postRenderer.isEnabled());
//            shapeRenderer.setEnabled(!shapeRenderer.isEnabled());
//            shadowProcessor.setEnabled(!shadowProcessor.isEnabled());
//            illuminationProcessor.setEnabled(!illuminationProcessor.isEnabled());
//            distantShadow.setEnabled(!distantShadow.isEnabled());
//            distantShadow.setResolution(distantShadow.getResolution() == 1024 ? 2040 : 1024);
//            distantShadow.setFilterEnabled(!distantShadow.isFilterEnabled());
//            distantShadow.setFilterDensity(distantShadow.getFilterDensity() == 100f ? 0f : 100f);
//            distantShadow.setOpacity(distantShadow.getOpacity() == 1f ? 0.25f : 1f);
//            distantShadow.setFilterSamples(distantShadow.getFilterSamples() == 64 ? 16 : 64);
        }
    }
}
