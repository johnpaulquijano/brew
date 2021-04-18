package core;

import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.framebuffer.*;
import core.math.Vector4;
import core.shader.Sampler;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Buffers;
import core.utility.Colors;
import core.utility.EngineException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages all rendering operations.
 *
 * @author John Paul Quijano
 */
public final class Renderer implements TraverserListener {
    public enum VertexAttribute {
        COORD(0, Shader.Type.VEC3),
        COLOR(1, Shader.Type.VEC4),
        TEXCOORD(2, Shader.Type.VEC2);

        private int identifier;
        private Shader.Type type;

        VertexAttribute(int loc, Shader.Type type) {
            this.identifier = loc;
            this.type = type;
        }

        public int getIdentifier() {
            return identifier;
        }

        public Shader.Type getType() {
            return type;
        }
    }

    public static final int MAX_FRAGMENT_OUTPUTS = 6;
    public static final int NUM_UTILITY_SAMPLERS = 8;

    public static final Vector4 DEFAULT_CLEAR_COLOR = new Vector4(Colors.DARK_GRAY4);

    private Variable wvMatrix_u;
    private Variable vpMatrix_u;
    private Variable wvpMatrix_u;
    private Variable vpMatrixInv_u;
    private Variable cameraLoc_u;
    private Variable cameraDir_u;
    private Variable cameraClip_u;
    private Sampler utility_s;

    private int width;
    private int height;
    private boolean resized;
    private boolean initialized;
    private boolean cameraDirty;
    private boolean frustumCullingEnabled;
    private Camera camera;
    private Shader shader;
    private Spatial scene;
    private Traverser traverser;
    private Vector4 clearColor;
    private FrameBuffer renderTarget;
    private ColorBuffer defaultCB;
    private DepthBuffer defaultDB;
    private FrameBuffer defaultFB;
    private Geometry fullscreenQuad;
    private Set<GraphicsObject> graphicsObjects;
    private List<RenderingModule> renderingModules;
    private List<Spatial> branches;
    private IntBuffer writeBuffer;

    Renderer() {
        width = GL.DEFAULT_VIEWPORT_WIDTH;
        height = GL.DEFAULT_VIEWPORT_HEIGHT;
        clearColor = new Vector4(DEFAULT_CLEAR_COLOR);
        frustumCullingEnabled = true;
        camera = new Camera();
        shader = new Shader();
        traverser = new Traverser();
        fullscreenQuad = new Geometry(Geometry.Type.TRIS, 4, 6);
        defaultDB = new DepthBuffer(DepthBuffer.Type.RENDERBUFFER);
        defaultCB = new ColorBuffer(ColorBuffer.Type.RGBA, false);
        defaultFB = new FrameBuffer(GL.DEFAULT_VIEWPORT_WIDTH, GL.DEFAULT_VIEWPORT_HEIGHT, defaultDB, defaultCB);
        graphicsObjects = new HashSet<>();
        renderingModules = new ArrayList<>();
        branches = new ArrayList();
        writeBuffer = Buffers.createIntBuffer(MAX_FRAGMENT_OUTPUTS);

        renderTarget = defaultFB;

        defaultCB.setWriteEnabled(true);
        defaultCB.setClearEnabled(true);

        traverser.addListener(this);

        initFullscreenQuad();
    }

    /**
     * Gives the shader.
     *
     * @return shader
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * Gives the traverser.
     *
     * @return traverser
     */
    public Traverser getTraverser() {
        return traverser;
    }

    /**
     * Creates an instance of the given graphics object in the graphics context.
     *
     * @param object - graphics object to build
     */
    public void build(GraphicsObject object) {
        object.build();
        graphicsObjects.add(object);
    }

    /**
     * Deallocates the instance of the given graphics object from the graphics context.
     *
     * @param object - graphics object to destroy
     */
    public void destroy(GraphicsObject object) {
        object.destroy();
        graphicsObjects.remove(object);
    }

    /**
     * Sets the main viewing camera.
     */
    public void setCamera(Camera camera) {
        if (camera == null) {
            throw new EngineException("Camera cannot be null.");
        }

        this.camera = camera;
        this.camera.resize(width, height);

        resized = true;
        cameraDirty = true;
    }

    /**
     * Gives the currently set camera.
     *
     * @return currently set camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Checks if the camera has been changed.
     *
     * @return true if the camera has been changed.
     */
    public boolean isCameraDirty() {
        return cameraDirty;
    }

    /**
     * Sets the scenegraph. The given spatial must be the root node.
     *
     * @param scene - root of the scenegraph
     */
    public void setScene(Spatial scene) {
        if (!scene.isRoot()) {
            throw new EngineException("Input is not a root node.");
        }

        this.scene = scene;
    }

    /**
     * Gives the root of the scenegraph.
     *
     * @return root of the scenegraph
     */
    public Spatial getScene() {
        return scene;
    }

    /**
     * Sets the color to which the screen is cleared.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     * @param a - alpha color component
     */
    public void setClearColor(float r, float g, float b, float a) {
        clearColor.set(r, g, b, a);
    }

    /**
     * Sets the color to which this color buffer is cleared if clearing state is enabled.
     *
     * @param color - a four-component color vector
     */
    public void setClearColor(Vector4 color) {
        setClearColor(color.getX(), color.getY(), color.getZ(), color.getW());
    }

    /**
     * Gives the clear color.
     *
     * @return clear color
     */
    public Vector4 getClearColor() {
        return clearColor;
    }

    /**
     * Sets the frame buffer to render onto. Set the render target before performing rendering operations.
     *
     * @param target - frame buffer to render onto
     */
    public void setRenderTarget(FrameBuffer target) {
        renderTarget = target;
    }

    /**
     * Gives the current render target.
     *
     * @return current render target
     */
    public FrameBuffer getRenderTarget() {
        return renderTarget;
    }

    /**
     * Gives the on-screen frame buffer.
     *
     * @return on-screen frame buffer
     */
    public FrameBuffer getDefaultRenderTarget() {
        return defaultFB;
    }

    /**
     * Attaches a rendering module. Rendering modules are executed in the order which they were added.
     *
     * @param module - rendering module to add
     */
    public void addRenderingModule(RenderingModule module) {
        if (initialized) {
            throw new EngineException("Cannot add a rendering module after initialization.");
        }

        if (renderingModules.contains(module)) {
            throw new EngineException("Rendering module already exists.");
        }

        renderingModules.add(module);
        module.addNotify(this);
    }

    /**
     * Gives the rendering module at the given index.
     *
     * @param index - rendering module position in the list
     *
     * @return rendering module at the given index
     */
    public RenderingModule getRenderingModule(int index) {
        return renderingModules.get(index);
    }

    /**
     * Gives the number of rendering modules.
     *
     * @return number rendering modules
     */
    public int numRenderingModules() {
        return renderingModules.size();
    }

    /**
     * Sets the world-view matrix.
     *
     * @param matrix - float buffer containing matrix components
     */
    public void setWVMatrix(FloatBuffer matrix) {
        GL.setMatrix4(wvMatrix_u.getID(), false, matrix);
    }

    /**
     * Sets the view-projection matrix.
     *
     * @param matrix - float buffer containing matrix components
     */
    public void setVPMatrix(FloatBuffer matrix) {
        GL.setMatrix4(vpMatrix_u.getID(), false, matrix);
    }

    /**
     * Sets the inverse view-projection matrix.
     *
     * @param matrix - float buffer containing matrix components
     */
    public void setVPMatrixInverse(FloatBuffer matrix) {
        GL.setMatrix4(vpMatrixInv_u.getID(), false, matrix);
    }

    /**
     * Sets the world-view-projection matrix.
     *
     * @param matrix - float buffer containing matrix components
     */
    public void setWVPMatrix(FloatBuffer matrix) {
        GL.setMatrix4(wvpMatrix_u.getID(), false, matrix);
    }

    /**
     * Gives the sampler unit index at the given buffer index.
     *
     * @param index - sampler buffer index
     */
    public int getUtilSamplerUnit(int index) {
        return utility_s.getUnit(index);
    }

    /**
     * If enabled, frustum culling is performed. Frustum culling is enabled by default.
     *
     * @param enabled - if true, frustum culling is enabled
     */
    public void setFrustumCullingEnabled(boolean enabled) {
        frustumCullingEnabled = enabled;
    }

    /**
     * Checks if frustum culling is enabled.
     *
     * @return true if frustum culling is enabled
     */
    public boolean isFrustumCullingEnabled() {
        return frustumCullingEnabled;
    }

    /**
     * Checks if this renderer has been initialized.
     *
     * @return true if this renderer has been initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if the viewport has been resized since the last frame.
     *
     * @return true if viewport has been resized
     */
    public boolean isViewportResized() {
        return resized;
    }

    /**
     * Gives the width of the viewport.
     *
     * @return width of the viewport
     */
    public int getViewportWidth() {
        return width;
    }

    /**
     * Gives the height of the viewport.
     *
     * @return height of the viewport
     */
    public int getViewportHeight() {
        return height;
    }

    /**
     * Renders a quad that covers the entire screen useful for render-to-texture operations.
     */
    public void drawFullscreenQuad() {
        GL.setPolygonMode(GL.PolygonMode.FILL);
        fullscreenQuad.draw();
    }

    /**
     * Resets rendering states. Take note that this overrides framebuffer states.
     */
    public void resetStates() {
        GL.setLineWidth(GL.DEFAULT_LINE_WIDTH);
        GL.setPointSize(GL.DEFAULT_POINT_SIZE);
        GL.setPolygonMode(GL.DEFAULT_POLYGON_MODE);
        GL.setCullFace(GL.DEFAULT_CULL_FACE);
        GL.setFaceCullingEnabled(GL.DEFAULT_FACE_CULLING_STATE);
        GL.setBlendEnabled(GL.DEFAULT_BLEND_STATE);
        GL.setDepthTestEnabled(GL.DEFAULT_DEPTH_TEST_STATE);
        GL.setDepthClampEnabled(GL.DEFAULT_DEPTH_CLAMP_STATE);
        GL.setDepthWriteEnabled(GL.DEFAULT_DEPTH_WRITE_STATE);
        GL.setColorWriteEnabled(GL.DEFAULT_COLOR_WRITE_STATE);
        GL.setSeamlessCubeTextureEnabled(GL.DEFAULT_SEAMLESS_CUBE_TEXTURE_STATE);
        GL.setPolygonOffsetFillEnabled(GL.DEFAULT_POLYGON_OFFSET_FILL_STATE);
        GL.setPolygonOffsetLineEnabled(GL.DEFAULT_POLYGON_OFFSET_LINE_STATE);
        GL.setPolygonOffsetPointEnabled(GL.DEFAULT_POLYGON_OFFSET_POINT_STATE);
        GL.setPolygonOffset(GL.DEFAULT_POLYGON_OFFSET_FACTOR, GL.DEFAULT_POLYGON_OFFSET_UNITS);
        GL.setViewport(0, 0, width, height);
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.BRANCH_DONE) {
            Spatial branch = event.getSource().getCurrent();
            branch.calculateHierarchicalBounds();
            branches.add(branch);
        } else if (event.getType() == TraverserEventType.BRANCH_NEXT) {
            Spatial branch = event.getSource().getCurrent();

            if (branch.isReset()) {
                branch.calculateWorldTransform();

                if (frustumCullingEnabled && !branch.transformDirty && !branch.descendantTransformDirty && !camera.intersects(branch.worldBoundingBox)) {
                    return true;
                }
            }
        } else if (event.getType() == TraverserEventType.LEAF) {
            Spatial leaf = event.getSource().getCurrent();

            leaf.calculateWorldTransform();
            leaf.calculateWorldBounds();
        }

        return false;
    }

    /**
     * Initializes rendering. This is called by the engine just before entering the application loop.
     */
    void init() {
        initShader();

        for (RenderingModule module : renderingModules) {
            module.build();
        }

        shader.build();

        for (RenderingModule module : renderingModules) {
            module.init();
            module.initialized = true;
        }

        initialized = true;
    }

    /**
     * Runs the rendering modules.
     */
    void render() {
        resetStates();
        updateCamera();

        traverser.traverse(scene);

        bind(renderTarget);

        for (RenderingModule module : renderingModules) {
            if (module.isEnabled()) {
                module.render();
            }
        }

        for (RenderingModule module : renderingModules) {
            if (module.isEnabled()) {
                module.clean();
            }
        }

        resetStates();
        clean();
    }

    /**
     * Called by the engine when the display is resized.
     *
     * @param width - display width
     * @param height - display height
     */
    void resize(int width, int height) {
        this.width = width;
        this.height = height;

        camera.resize(width, height);
        defaultFB.resize(width, height);

        GL.setViewport(0, 0, width, height);

        resized = true;
    }

    /**
     * Recreates all graphics objects. This is called by the engine after the display has been destroyed and recreated.
     */
    void restore() {
        init();

        for (GraphicsObject object : graphicsObjects) {
            build(object);
        }
    }

    /**
     * Destroys graphics resources. This is called by the engine after exiting the application loop.
     */
    void free() {
        shader.free();

        for (GraphicsObject object : graphicsObjects) {
            object.destroy();
        }

        for (RenderingModule module : renderingModules) {
            module.initialized = false;
        }

        graphicsObjects.clear();

        initialized = false;
    }

    /**
     * Binds the given frame buffer for writing.
     *
     * @param fb - frame buffer to bind
     */
    private void bind(FrameBuffer fb) {
        GL.writeFrameBuffer(fb.getID());

        writeBuffer.clear();

        for (int i = 0; i < fb.numColorBuffers(); i++) {
            ColorBuffer cb = fb.getColorBuffer(i);
            int attachment = GL.Attachment.COLOR_0.getValue() + i;

            GL.writeAttachment(attachment);

            if (cb.isClearEnabled()) {
                GL.clearBuffer(GL.Buffer.COLOR.getValue());
            }

            if (cb.isWriteEnabled()) {
                writeBuffer.put(i, attachment);
            } else {
                writeBuffer.put(i, GL.Attachment.NONE.getValue());
            }
        }

        GL.clearBuffer(GL.Buffer.DEPTH);
        GL.writeAttachments(writeBuffer);
    }

    /**
     * Initializes global shader attributes.
     */
    private void initShader() {
        wvMatrix_u = new Variable(Shader.Type.MAT4, "wvMatrix", null, 0, Shader.Qualifier.UNIFORM);
        vpMatrix_u = new Variable(Shader.Type.MAT4, "vpMatrix", null, 0, Shader.Qualifier.UNIFORM);
        wvpMatrix_u = new Variable(Shader.Type.MAT4, "wvpMatrix", null, 0, Shader.Qualifier.UNIFORM);
        vpMatrixInv_u = new Variable(Shader.Type.MAT4, "vpMatrixInv", null, 0, Shader.Qualifier.UNIFORM);
        cameraLoc_u = new Variable(Shader.Type.VEC3, "cameraLoc", null, 0, Shader.Qualifier.UNIFORM);
        cameraDir_u = new Variable(Shader.Type.VEC3, "cameraDir", null, 0, Shader.Qualifier.UNIFORM);
        cameraClip_u = new Variable(Shader.Type.VEC2, "cameraClip", null, 0, Shader.Qualifier.UNIFORM);

        shader.addVariable(wvMatrix_u);
        shader.addVariable(vpMatrix_u);
        shader.addVariable(wvpMatrix_u);
        shader.addVariable(vpMatrixInv_u);
        shader.addVariable(cameraLoc_u);
        shader.addVariable(cameraDir_u);
        shader.addVariable(cameraClip_u);

        shader.addVariable(new Variable(VertexAttribute.COORD.getType(), "coord", null, 0, Shader.Qualifier.IN));
        shader.addVariable(new Variable(VertexAttribute.COLOR.getType(), "color", null, 0, Shader.Qualifier.IN));
        shader.addVariable(new Variable(VertexAttribute.TEXCOORD.getType(), "texCoord", null, 0, Shader.Qualifier.IN));

        shader.addVariable(new Variable(Shader.Type.VEC4, "output0", null, 0, Shader.Qualifier.OUT));
        shader.addVariable(new Variable(Shader.Type.VEC4, "output1", null, 0, Shader.Qualifier.OUT));
        shader.addVariable(new Variable(Shader.Type.VEC4, "output2", null, 0, Shader.Qualifier.OUT));
        shader.addVariable(new Variable(Shader.Type.VEC4, "output3", null, 0, Shader.Qualifier.OUT));

        shader.addVariable(new Variable(Shader.Type.VEC4, "coord", null, 0, Shader.Qualifier.VARYING));
        shader.addVariable(new Variable(Shader.Type.VEC4, "color", null, 0, Shader.Qualifier.VARYING));
        shader.addVariable(new Variable(Shader.Type.VEC2, "texCoord", null, 0, Shader.Qualifier.VARYING));

        utility_s = new Sampler(Shader.Type.SAMPLER2D, "utilSamplers", NUM_UTILITY_SAMPLERS, Shader.Qualifier.UNIFORM);
        shader.addVariable(utility_s);

        shader.addDefinition("TEXTURE_BLEND_REPLACE", String.valueOf(Texture.Blend.REPLACE.getValue()));
        shader.addDefinition("TEXTURE_BLEND_MODULATE", String.valueOf(Texture.Blend.MODULATE.getValue()));
        shader.addDefinition("TEXTURE_BLEND_ACCUMULATE", String.valueOf(Texture.Blend.ACCUMULATE.getValue()));
        shader.addDefinition("TEXTURE_BLEND_INTERPOLATE", String.valueOf(Texture.Blend.INTERPOLATE.getValue()));
    }

    /**
     * Updates the camera every frame.
     */
    private void updateCamera() {
        camera.updateViewProjection();

        if (cameraDirty) {
            GL.setVector2(cameraClip_u.getID(), camera.getNearClipDistance(), camera.getFarClipDistance());
            GL.setVector3(cameraLoc_u.getID(), camera.getLocation().getX(), camera.getLocation().getY(), camera.getLocation().getZ());
            GL.setVector3(cameraDir_u.getID(), camera.getDirection().getX(), camera.getDirection().getY(), camera.getDirection().getZ());
        } else {
            if (camera.isFrustumDirty()) {
                GL.setVector2(cameraClip_u.getID(), camera.getNearClipDistance(), camera.getFarClipDistance());
            }

            if (camera.isLocationDirty()) {
                GL.setVector3(cameraLoc_u.getID(), camera.getLocation().getX(), camera.getLocation().getY(), camera.getLocation().getZ());
            }

            if (camera.isDirectionDirty()) {
                GL.setVector3(cameraDir_u.getID(), camera.getDirection().getX(), camera.getDirection().getY(), camera.getDirection().getZ());
            }
        }
    }

    /**
     * Resets dirty flags.
     */
    private void clean() {
        for (Spatial branch : branches) {
            branch.clean();
        }

        branches.clear();
        camera.clean();

        resized = false;
        cameraDirty = false;
    }

    /**
     * Builds the fullscreen quad geometry.
     */
    private void initFullscreenQuad() {
        fullscreenQuad.setIndex(0, 0);
        fullscreenQuad.setIndex(1, 1);
        fullscreenQuad.setIndex(2, 2);
        fullscreenQuad.setIndex(3, 2);
        fullscreenQuad.setIndex(4, 3);
        fullscreenQuad.setIndex(5, 0);

        fullscreenQuad.setCoordinate(0, -1f, -1f, -1f);
        fullscreenQuad.setCoordinate(1, 1f, -1f, -1f);
        fullscreenQuad.setCoordinate(2, 1f, 1f, -1f);
        fullscreenQuad.setCoordinate(3, -1f, 1f, -1f);

        fullscreenQuad.setTextureCoordinate(0, 0f, 0f);
        fullscreenQuad.setTextureCoordinate(1, 1f, 0f);
        fullscreenQuad.setTextureCoordinate(2, 1f, 1f);
        fullscreenQuad.setTextureCoordinate(3, 0f, 1f);

        fullscreenQuad.setTexCoordEnabled(true);

        build(fullscreenQuad);
    }
}
