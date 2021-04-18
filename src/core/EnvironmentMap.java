package core;

import core.math.EngineMath;
import core.math.Vector3;
import core.utility.EngineException;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A cube texture containing a view of the environment from a location.
 *
 * @author John Paul Quijano
 */
public class EnvironmentMap extends GraphicsObject {
    public static final int DEFAULT_SIZE = 512;
    public static final float DEFAULT_CLIP = Camera.DEFAULT_FAR_CLIP;

    private int[] framebuffers;
    private int[] depthbuffers;
    private int size;
    private float clip;
    private boolean dirty;
    private boolean cameraDirty;
    private boolean textureDirty;
    private Vector3 location;
    private Camera[] cameras;
    protected List<Sky>[] reflectableSkies;
    protected List<Shape>[] reflectableShapes;


    public EnvironmentMap() {
        size = DEFAULT_SIZE;
        clip = DEFAULT_CLIP;
        framebuffers = new int[6];
        depthbuffers = new int[6];
        cameras = new Camera[6];
        reflectableSkies = new List[6];
        reflectableShapes = new List[6];
        location = new Vector3();

        for (int i = 0; i < 6; i++) {
            cameras[i] = new Camera();
            cameras[i].setPerspectiveFrustum(EngineMath.HALF_PI, 1f, 0.1f, clip);

            reflectableSkies[i] = new ArrayList<>();
            reflectableShapes[i] = new ArrayList<>();
        }

        cameras[0].lookAlong(Vector3.UNIT_X, Vector3.UNIT_Y);
        cameras[1].lookAlong(Vector3.NEG_UNIT_X, Vector3.UNIT_Y);
        cameras[2].lookAlong(Vector3.UNIT_Y, Vector3.UNIT_Z);
        cameras[3].lookAlong(Vector3.NEG_UNIT_Y, Vector3.UNIT_Z);
        cameras[4].lookAlong(Vector3.UNIT_Z, Vector3.UNIT_Y);
        cameras[5].lookAlong(Vector3.NEG_UNIT_Z, Vector3.UNIT_Y);

        cameraDirty = true;
    }

    /**
     * Sets the far clipping distance of the cameras.
     *
     * @param clip - far clipping distance of the cameras
     */
    public void setClip(float clip) {
        this.clip = clip;

        for (Camera camera : cameras) {
            camera.setFarClipDistance(this.clip);
        }

        dirty = true;
        cameraDirty = true;
    }

    /**
     * Gives the far clipping distance of the cameras.
     *
     * @return far clipping distance of the cameras
     */
    public float getClip() {
        return clip;
    }

    /**
     * Sets the cube texture dimensions.
     *
     * @param size - cube texture dimensions
     */
    public void setSize(int size) {
        this.size = size;
        dirty = true;
        textureDirty = true;
    }

    /**
     * Gives the cube texture dimensions.
     *
     * @return cube texture dimensions
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets this environment map's location.
     *
     * @param x - first vector component
     * @param y - second vector component
     * @param z - third vector component
     */
    public void setLocation(float x, float y, float z) {
        location.set(x, y, z);

        for (Camera camera : cameras) {
            camera.setLocation(location);
        }

        dirty = true;
        cameraDirty = true;
    }

    /**
     * Sets this environment map's location.
     *
     * @param location - position vector to set
     */
    public void setLocation(Vector3 location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Gives this environment map's location.
     *
     * @return environment map's location
     */
    public Vector3 getLocation() {
        return location;
    }

    /**
     * Gives the camera at the given index.
     *
     * @param index - camera index
     *
     * @return camera at the given index
     */
    public Camera getCamera(int index) {
        return cameras[index];
    }

    /**
     * Gives the list of reflectable skies.
     *
     * @param index - buffer index
     *
     * @return list of reflectable skies
     */
    public List<Sky> getReflectableSkies(int index) {
        return reflectableSkies[index];
    }

    /**
     * Gives the list of reflectable shapes.
     *
     * @param index - buffer index
     *
     * @return list of reflectable skies
     */
    public List<Shape> getReflectableShapes(int index) {
        return reflectableShapes[index];
    }

    /**
     * Resets textureDirty flags.
     */
    public void clean() {
        if (cameraDirty) {
            for (Camera camera : cameras) {
                camera.clean();
            }
        }

        dirty = false;
        cameraDirty = false;
        textureDirty = false;
    }

    /**
     * Updates this environment map.
     */
    public void update() {
        if (dirty) {
            if (cameraDirty) {
                for (Camera camera : cameras) {
                    camera.updateViewProjection();
                }
            }

            if (textureDirty) {
                for (int i = 0; i < 6; i++) {
                    GL.updateTextureCubeData(i, id, size, size, false, null);
                    GL.updateDepthRenderBuffer(depthbuffers[i], size, size);
                }
            }
        }
    }

    /**
     * Gives the modification flag.
     *
     * @return true if this environment map has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    @Override
    protected void build() {
        id = GL.createTextureCube(GL.WrapMode.CLAMP_TO_EDGE, GL.WrapMode.CLAMP_TO_EDGE, GL.WrapMode.CLAMP_TO_EDGE, GL.Filter.LINEAR, GL.Filter.LINEAR_MIPMAP_LINEAR);

        GL.generateTextureCubeMipmap(id, 0, 10);

        for (int i = 0; i < framebuffers.length; i++) {
            GL.setTextureCubeData(i, id, size, size, false, (FloatBuffer) null);
        }

        for (int i = 0; i < framebuffers.length; i++) {
            framebuffers[i] = GL.createFrameBuffer();
            depthbuffers[i] = GL.createDepthRenderBuffer(size, size);

            GL.writeFrameBuffer(framebuffers[i]);
            GL.attachCubeColorTexture(id, i);
            GL.attachDepthRenderBuffer(depthbuffers[i]);

            if (!GL.isFrameBufferComplete()) {
                throw new EngineException(GL.getFrameBufferStatus().getDescription());
            }
        }
    }

    @Override
    protected void destroy() {
        for (int i = 0; i < framebuffers.length; i++) {
            GL.freeFrameBuffer(framebuffers[i]);
            framebuffers[i] = 0;

            GL.freeRenderBuffer(depthbuffers[i]);
            depthbuffers[i] = 0;
        }

        GL.freeTexture(id);

        id = 0;
        clean();
    }

    /**
     * Prepares the frame buffer at the given index for drawing.
     *
     * @param index - index of the frame buffer
     */
    public void initDraw(int index) {
        GL.writeFrameBuffer(framebuffers[index]);
        GL.setViewport(0, 0, size, size);
        GL.clearBuffer(GL.Buffer.COLOR.getValue() | GL.Buffer.DEPTH.getValue());
    }

    /**
     * Collects objects that can be reflected from the scene.
     *
     * @param scene - scene graph
     * @param exclude - shape to exclude
     */
    public void collectReflectables(Spatial scene, Shape exclude) {
        for (int i = 0; i < 6; i++) {
            reflectableSkies[i].clear();
            reflectableShapes[i].clear();

            Spatial current = scene;

            while (current != null) {
                if (current.isLeaf() || !current.hasNext()) {
                    if (current instanceof Shape) {
                        Shape shape = (Shape) current;

                        if (!shape.equals(exclude) && cameras[i].intersects(shape.getWorldBounds())) {
                            reflectableShapes[i].add(shape);
                        }
                    } else if (current instanceof Sky) {
                        reflectableSkies[i].add((Sky) current);
                    }

                    current.resetNext();
                    current = current.getParent();
                } else {
                    if (!cameras[i].intersects(current.getWorldBounds())) {
                        current = current.getParent();
                        continue;
                    }

                    current = current.next();
                }
            }
        }
    }
}
