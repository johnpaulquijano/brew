package core;

import core.math.*;
import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Pools;

import java.nio.FloatBuffer;

/**
 * Manages view and perspective transformations.
 *
 * @author John Paul Quijano
 */
public class Camera {
    public enum Frustum {
        LEFT(0), RIGHT(1), BOTTOM(2), TOP(3), FAR(4), NEAR(5);

        private int value;

        Frustum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Projection {
        PERSPECTIVE, ORTHOGRAPHIC
    }

    public static final float DEFAULT_NEAR_CLIP = 0.1f;
    public static final float DEFAULT_FAR_CLIP = 500f;
    public static final float DEFAULT_FOV = EngineMath.toRadians(35f);
    public static final Projection DEFAULT_PROJECTION = Projection.PERSPECTIVE;
    public static final Vector4[] NDC = new Vector4[8];

    static {
        for (int i = 0; i < 8; i++) {
            NDC[i] = new Vector4();
        }

        NDC[0].set(-1f, -1f, -1f, 1f);
        NDC[1].set(1f, -1f, -1f, 1f);
        NDC[2].set(1f, 1f, -1f, 1f);
        NDC[3].set(-1f, 1f, -1f, 1f);
        NDC[4].set(-1f, -1f, 1f, 1f);
        NDC[5].set(1f, -1f, 1f, 1f);
        NDC[6].set(1f, 1f, 1f, 1f);
        NDC[7].set(-1f, 1f, 1f, 1f);
    }

    private float fNear;
    private float fFar;
    private float fLeft;
    private float fRight;
    private float fTop;
    private float fBottom;
    private float cLeft0;
    private float cLeft1;
    private float cRight0;
    private float cRight1;
    private float cBottom0;
    private float cBottom1;
    private float cTop0;
    private float cTop1;
    private float aspect;
    private float fieldOfView;
    private float width;
    private float height;
    private float nWidth;
    private float nHeight;
    private boolean frameDirty;
    private boolean frustumDirty;
    private boolean updateView;
    private boolean updateProjection;
    private boolean dirty;
    private boolean locDirty;
    private boolean dirDirty;
    private boolean fieldRangeDirty;
    private boolean focalDistanceDirty;
    private Matrix4 viewMatrix;
    private Matrix4 projMatrix;
    private Matrix4 viewProjMatrix;
    private Vector3 up;
    private Vector3 left;
    private Vector3 direction;
    private Vector3 location;
    private Plane[] frustum;
    private Projection projection;
    private FloatBuffer viewBuffer;
    private FloatBuffer projBuffer;
    private FloatBuffer viewProjBuffer;

    /**
     * Creates a camera with default attributes.
     */
    public Camera() {
        frustum = new Plane[6];
        viewMatrix = new Matrix4();
        projMatrix = new Matrix4();
        viewProjMatrix = new Matrix4();
        up = new Vector3(Vector3.UNIT_Y);
        left = new Vector3(Vector3.NEG_UNIT_X);
        direction = new Vector3(Vector3.NEG_UNIT_Z);
        location = new Vector3(0f, 0f, 4f);
        viewBuffer = Buffers.createFloatBuffer(16);
        projBuffer = Buffers.createFloatBuffer(16);
        viewProjBuffer = Buffers.createFloatBuffer(16);

        for (int i = 0; i < 6; i++) {
            frustum[i] = new Plane();
        }

        fNear = DEFAULT_NEAR_CLIP;
        fFar = DEFAULT_FAR_CLIP;
        fieldOfView = DEFAULT_FOV;
        projection = DEFAULT_PROJECTION;

        width = 1f;
        height = 1f;
        aspect = 1f;
        nHeight = EngineMath.tan(fieldOfView * 0.5f) * fNear;
        nWidth = nHeight * aspect;
        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;

        dirty = true;
        locDirty = true;
        dirDirty = true;
        frameDirty = true;
        frustumDirty = true;
        fieldRangeDirty = true;
        focalDistanceDirty = true;
    }
    /**
     * Creates a perspective camera with the given parameters.
     *
     * @param fov - field of view
     * @param aspect - aspect ratio
     * @param near - near clipping plane
     * @param far - far clipping plane
     */
    public Camera(float fov, float aspect, float near, float far) {
        frustum = new Plane[6];
        viewMatrix = new Matrix4();
        projMatrix = new Matrix4();
        viewProjMatrix = new Matrix4();
        up = new Vector3(Vector3.UNIT_Y);
        left = new Vector3(Vector3.NEG_UNIT_X);
        direction = new Vector3(Vector3.NEG_UNIT_Z);
        location = new Vector3(0f, 0f, 4f);
        projection = Projection.PERSPECTIVE;
        viewProjBuffer = Buffers.createFloatBuffer(16);

        for (int i = 0; i < 6; i++) {
            frustum[i] = new Plane();
        }

        width = 1f;
        height = 1f;
        fNear = near;
        fFar = far;
        this.aspect = aspect;
        fieldOfView = fov;
        nHeight = EngineMath.tan(fov * 0.5f) * fNear;
        nWidth = nHeight * aspect;
        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;

        dirty = true;
        locDirty = true;
        dirDirty = true;
        frameDirty = true;
        frustumDirty = true;
        fieldRangeDirty = true;
        focalDistanceDirty = true;
    }

    /**
     * Sets all attributes of this camera to the given template.
     *
     * @param template - camera to copy attributes from
     *
     * @return this camera
     */
    public Camera set(Camera template) {
        if (template == null) {
            return this;
        }

        fNear = template.fNear;
        fFar = template.fFar;
        fLeft = template.fLeft;
        fRight = template.fRight;
        fTop = template.fTop;
        fBottom = template.fBottom;

        fieldOfView = template.fieldOfView;
        width = template.width;
        height = template.height;
        aspect = template.aspect;
        projection = template.projection;

        up.set(template.up);
        left.set(template.left);
        location.set(template.location);
        direction.set(template.direction);

        viewProjBuffer = Buffers.createFloatBuffer(16);

        dirty = true;
        locDirty = true;
        dirDirty = true;
        frameDirty = true;
        frustumDirty = true;
        fieldRangeDirty = true;
        focalDistanceDirty = true;

        return this;
    }

    /**
     * Sets the projection to either ORTHOGRAPHIC or PERSPECTIVE.
     *
     * @param projection - projection to set
     */
    public void setProjection(Projection projection) {
        this.projection = projection;
        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives this camera's projection.
     *
     * @return projection
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Sets the half-angle of the area this camera covers.
     *
     * @param fov - field of view to set
     */
    public void setFieldOfView(float fov) {
        this.fieldOfView = fov;

        nHeight = EngineMath.tan(fov * 0.5f) * fNear;
        nWidth = nHeight * aspect;

        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;

        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives this camera's field of view.
     *
     * @return field of view
     */
    public float getFieldOfView() {
        return fieldOfView;
    }

    /**
     * Ratio of width to height of the area covered by this camera.
     */
    public void setAspectRatio(float aspect) {
        this.aspect = aspect;

        nWidth = nHeight * aspect;

        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;

        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives this camera's aspect ratio.
     *
     * @return aspect ratio
     */
    public float getAspectRatio() {
        return aspect;
    }

    /**
     * Calculates then sets this camera's aspect ratio from the given width and height.
     *
     * @param width - display width
     * @param height - display height
     */
    public void resize(float width, float height) {
        this.width = width;
        this.height = height;
        setAspectRatio(width / height);
    }

    /**
     * Sets the far clipping distance.
     *
     * @param distance - far clipping distance to set
     */
    public void setFarClipDistance(float distance) {
        fFar = distance;
        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives this camera's far clipping distance.
     *
     * @return far clipping distance
     */
    public float getFarClipDistance() {
        return fFar;
    }

    /**
     * Sets the near clipping distance.
     *
     * @param distance - near clipping distance to set
     */
    public void setNearClipDistance(float distance) {
        fNear = distance;

        nHeight = EngineMath.tan(fieldOfView * 0.5f) * distance;
        nWidth = nHeight * aspect;

        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;

        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives this camera's near clipping distance.
     *
     * @return near clipping distance
     */
    public float getNearClipDistance() {
        return fNear;
    }

    /**
     * Calculates a perspective view frustum based on the given parameters.
     *
     * @param fov - field of view
     * @param aspect - aspect ratio
     * @param near - near clipping plane
     * @param far - far clipping plane
     */
    public void setPerspectiveFrustum(float fov, float aspect, float near, float far) {
        this.fieldOfView = fov;
        this.aspect = aspect;

        nHeight = EngineMath.tan(fov * 0.5f) * near;
        nWidth = nHeight * aspect;

        fLeft = -nWidth;
        fRight = nWidth;
        fBottom = -nHeight;
        fTop = nHeight;
        fNear = near;
        fFar = far;

        frustumDirty = true;
        dirty = true;
    }

    /**
     * Calculates a view frustum based on the given clipping distances.
     */
    public void setOrthographicFrustum(float left, float right, float bottom, float top, float near, float far) {
        fLeft = left;
        fRight = right;
        fBottom = bottom;
        fTop = top;
        fNear = near;
        fFar = far;

        frustumDirty = true;
        dirty = true;
    }

    /**
     * Gives the frustum plane at the given side.
     *
     * @param side - frustum plane position
     *
     * @return frustum plane at the given side
     */
    public Plane getFrustumPlane(Frustum side) {
        return frustum[side.value];
    }

    /**
     * Sets this camera's vertical reference frame.
     *
     * @param x - first vector component
     * @param y - second vector component
     * @param z - third vector component
     */
    public void setUp(float x, float y, float z) {
        up.set(x, y, z);
        frameDirty = true;
        dirty = true;
    }

    /**
     * Sets this camera's vertical reference frame.
     *
     * @param up - vertical reference vector to set
     */
    public void setUp(Vector3 up) {
        setUp(up.getX(), up.getY(), up.getZ());
    }

    /**
     * Gives this camera's vertical reference frame vector.
     *
     * @return vertical reference frame vector
     */
    public Vector3 getUp() {
        return up;
    }

    /**
     * Sets this camera's horizontal reference frame.
     *
     * @param x - first vector component
     * @param y - second vector component
     * @param z - third vector component
     */
    public void setLeft(float x, float y, float z) {
        left.set(x, y, z);
        frameDirty = true;
        dirty = true;
    }

    /**
     * Sets this camera's horizontal reference frame.
     *
     * @param left - horizontal reference vector to set
     */
    public void setLeft(Vector3 left) {
        setLeft(left.getX(), left.getY(), left.getZ());
    }

    /**
     * Gives this camera's horizontal reference frame vector.
     *
     * @return horizontal reference frame vector
     */
    public Vector3 getLeft() {
        return left;
    }

    /**
     * Sets this camera's world-space location.
     *
     * @param x - first vector component
     * @param y - second vector component
     * @param z - third vector component
     */
    public void setLocation(float x, float y, float z) {
        location.set(x, y, z);

        dirty = true;
        locDirty = true;
        frameDirty = true;
    }

    /**
     * Sets this camera's world-space location.
     *
     * @param location - camera position vector to set
     */
    public void setLocation(Vector3 location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Gives this camera's world-space location.
     *
     * @return camera's world-space location
     */
    public Vector3 getLocation() {
        return location;
    }

    /**
     * Orients this camera to the direction defined by this camera's location, the given coordinates, and the given up-vector.
     *
     * @param x - first location vector component
     * @param y - second location vector component
     * @param z - third location vector component
     * @param upVec - normalized vertical reference vector
     */
    public void lookAt(float x, float y, float z, Vector3 upVec) {
        Vector3 newDir = Pools.Vector3.get().set(x, y, z).subtract(location).normalize();

        if (newDir.equals(direction)) {
            Pools.Vector3.put(newDir);
            return;
        }

        direction.set(newDir);

        if (upVec.equals(Vector3.ZERO)) {
            up.set(Vector3.UNIT_Y);
        } else {
            up.set(upVec);
        }

        left.set(up).cross(direction).normalize();

        if (left.equals(Vector3.ZERO)) {
            if (direction.getX() != 0f) {
                left.set(direction.getY(), -direction.getX(), 0f);
            } else {
                left.set(0f, direction.getZ(), -direction.getY());
            }
        }

        up.set(direction).cross(left).normalize();

        dirty = true;
        dirDirty = true;
        frameDirty = true;

        Pools.Vector3.put(newDir);
    }

    /**
     * Orients this camera to the direction defined by this camera's location, the given coordinates, and the given up-vector.
     *
     * @param pos - camera location vector
     * @param upVec - normalized vertical reference vector
     */
    public void lookAt(Vector3 pos, Vector3 upVec) {
        lookAt(pos.getX(), pos.getY(), pos.getZ(), upVec);
    }

    /**
     * Orients this camera to the given direction coordinates and up-vector.
     *
     * @param x - first direction vector component
     * @param y - second direction vector component
     * @param z - third direction vector component
     * @param upVec - normalized vertical reference vector
     */
    public void lookAlong(float x, float y, float z, Vector3 upVec) {
        Vector3 newDir = Pools.Vector3.get().set(x, y, z).normalize();

        if (newDir.equals(direction)) {
            Pools.Vector3.put(newDir);
            return;
        }

        direction.set(newDir);

        if (up.equals(Vector3.ZERO)) {
            up.set(Vector3.UNIT_Y);
        } else {
            up.set(upVec);
        }

        left.set(up).cross(direction).normalize();

        if (left.equals(Vector3.ZERO)) {
            if (direction.getX() != 0f) {
                left.set(direction.getY(), -direction.getX(), 0f);
            } else {
                left.set(0f, direction.getZ(), -direction.getY());
            }
        }

        up.set(direction).cross(left).normalize();

        dirty = true;
        dirDirty = true;
        frameDirty = true;

        Pools.Vector3.put(newDir);
    }

    /**
     * Orients this camera to the given direction coordinates and up-vector.
     *
     * @param dir - camera direction vector
     * @param upVec - normalized vertical reference vector
     */
    public void lookAlong(Vector3 dir, Vector3 upVec) {
        lookAlong(dir.getX(), dir.getY(), dir.getZ(), upVec);
    }

    /**
     * Gives this camera's direction vector.
     *
     * @return direction vector
     */
    public Vector3 getDirection() {
        return direction;
    }

    /**
     * Moves this camera along its current direction by the given scale.
     *
     * @param scale - amount of movement
     */
    public void moveAlong(float scale) {
        Vector3 scaledDir = Pools.Vector3.get().set(direction).multiply(scale);

        location.add(scaledDir);

        dirty = true;
        locDirty = true;
        frameDirty = true;

        Pools.Vector3.put(scaledDir);
    }

    /**
     * Moves this camera along the direction perpendicular to its current direction by the given scale.
     *
     * @param scale - amount of movement
     * @param horizontal - if true, moves horizontally, vertically otherwise
     */
    public void moveAcross(float scale, boolean horizontal) {
        Vector3 scaledDir = Pools.Vector3.get();

        if (horizontal) {
            scaledDir.set(left).multiply(scale);
        } else {
            scaledDir.set(up).multiply(scale);
        }

        location.add(scaledDir);

        dirty = true;
        locDirty = true;
        frameDirty = true;

        Pools.Vector3.put(scaledDir);
    }

    /**
     * Tests intersection of the given boundingBox object with this camera's view frustum. This method returns true for
     * OBJECTS that intersect or are completely within the view frustum.
     *
     * @param boundingBox - boundingBox to test intersection with
     */
    public boolean intersects(BoundingBox boundingBox) {
        for (Plane plane : frustum) {
            if (boundingBox.position(plane) == Plane.Position.NEGATIVE) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests strict containment of the given boundingBox object within this camera's view frustum. This method returns false
     * for OBJECTS that intersect or are outside the view frustum.
     *
     * @param boundingBox - boundingBox to test containment with
     */
    public boolean contains(BoundingBox boundingBox) {
        for (Plane plane : frustum) {
            if (boundingBox.position(plane) != Plane.Position.POSITIVE) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculates the camera-space position of a given screen space coordinate. A depth of 0 calculates the coordinates
     * on the near clipping plane and 1 on the far clipping plane.
     *
     * @param x - first screen-space vector component
     * @param y - second screen-space vector component
     * @param depth - distance from the near clipping plane
     * @param output - storage vector for the result
     *
     * @return the calculated world coordinates
     */
    public Vector3 getWorldCoordinates(float x, float y, float depth, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        Vector4 position = Pools.Vector4.get().set(Vector4.ZERO);

        try {
            position.setX(x / width * 2f - 1f);
            position.setY(y / height * 2f - 1f);
            position.setZ(depth * 2f - 1f);
            position.setW(1f);

            updateViewProjection();

            Matrix4 viewProjInv = Pools.Matrix4.get().set(viewProjMatrix).invert();
            viewProjInv.transform(position, position);
            Pools.Matrix4.put(viewProjInv);

            position.multiply(1f / position.getW());
            output.set(position.getX(), position.getY(), position.getZ());
        } catch (EngineException ex) {
            /* ignore exception when matrix cannot be inverted */
        }

        Pools.Vector4.put(position);

        return output;
    }

    /**
     * Calculates the camera-space position of a given screen space coordinate. A depth of 0 calculates the coordinates
     * on the near clipping plane and 1 on the far clipping plane.
     *
     * @param x - first screen-space vector component
     * @param y - second screen-space vector component
     * @param depth - distance from the near clipping plane
     * @param output - storage vector for the result
     *
     * @return the calculated world coordinates
     */
    public Vector3 getWorldCoordinates(int x, int y, float depth, Vector3 output) {
        return getWorldCoordinates((float) x, (float) y, depth, output);
    }

    /**
     * Calculates the camera-space position of a given screen space coordinate. A depth of 0 calculates the coordinates
     * on the near clipping plane and 1 on the far clipping plane.
     *
     * @param screenPosition - screen-space position vector
     * @param depth - distance from the near clipping plane
     * @param output - storage vector for the result
     *
     * @return the calculated world coordinates
     */
    public Vector3 getWorldCoordinates(Point screenPosition, float depth, Vector3 output) {
        return getWorldCoordinates(screenPosition.getX(), screenPosition.getY(), depth, output);
    }

    /**
     * Calculates the screen-space position of a given camera space coordinate.
     *
     * @param x - first world-space vector component
     * @param y - second world-space vector component
     * @param z - third world-space vector component
     * @param output - storage vector for the result
     *
     * @return the calculated screen-space coordinate
     */
    public Point getScreenCoordinates(float x, float y, float z, Point output) {
        if (output == null) {
            output = new Point();
        }

        Vector3 worldPos = Pools.Vector3.get().set(x, y, z);
        Vector4 position = Pools.Vector4.get().set(x, y, z, 1f);

        updateViewProjection();
        viewProjMatrix.transform(position, position);

        position.multiply(1f / position.getW());
        worldPos.set(position.getX(), position.getY(), position.getZ());

        output.setX(EngineMath.round(((worldPos.getX() + 1f) * 0.5f * width)));
        output.setY(EngineMath.round(((worldPos.getY() + 1f) * 0.5f * height)));

        Pools.Vector3.put(worldPos);
        Pools.Vector4.put(position);

        return output;
    }

    /**
     * Calculates the screen-space position of a given camera space coordinate.
     *
     * @param worldPosition - world-space position
     * @param output - storage vector for the result
     *
     * @return the calculated screen-space coordinate
     */
    public Point getScreenCoordinates(Vector3 worldPosition, Point output) {
        return getScreenCoordinates(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), output);
    }

    /**
     * Gives this camera's projection matrix.
     *
     * @return 4x4 projection matrix
     */
    public Matrix4 getProjectionMatrix() {
        updateProjection();
        return projMatrix;
    }

    /**
     * Gives this camera's view matrix.
     *
     * @return 4x4 view matrix
     */
    public Matrix4 getViewMatrix() {
        updateView();
        return viewMatrix;
    }

    /**
     * Gives this camera's view-projection matrix.
     *
     * @return 4x4 view-projection matrix
     */
    public Matrix4 getViewProjectionMatrix() {
        updateViewProjection();
        return viewProjMatrix;
    }

    /**
     * Gives this camera's dirty flag.
     *
     * @return true if this camera has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Gives this camera's location dirty flag.
     *
     * @return true if this camera's location has been modified since the last frame
     */
    public boolean isLocationDirty() {
        return locDirty;
    }

    /**
     * Gives this camera's direction dirty flag.
     *
     * @return true if this camera's direction has been modified since the last frame
     */
    public boolean isDirectionDirty() {
        return dirDirty;
    }

    /**
     * Gives this camera's frustum dirty flag.
     *
     * @return true if this camera's frustum has been modified since the last frame
     */
    public boolean isFrustumDirty() {
        return frustumDirty;
    }

    /**
     * Gives this camera's frame dirty flag.
     *
     * @return true if this camera's frame has been modified since the last frame
     */
    public boolean isFrameDirty() {
        return frameDirty;
    }

    /**
     * Gives this camera's field range dirty flag.
     *
     * @return true if this camera's field range has been modified since the last frame
     */
    public boolean isFieldRangeDirty() {
        return fieldRangeDirty;
    }

    /**
     * Gives this camera's focal distance dirty flag.
     *
     * @return true if this camera's focal distance has been modified since the last frame
     */
    public boolean isFocalDistanceDirty() {
        return focalDistanceDirty;
    }

    /**
     * Updates this camera's frustum.
     */
    public void updateFrustum() {
        if (frustumDirty) {
            if (projection == Projection.PERSPECTIVE) {
                float nearSquared = fNear * fNear;
                float leftSquared = fLeft * fLeft;
                float rightSquared = fRight * fRight;
                float bottomSquared = fBottom * fBottom;
                float topSquared = fTop * fTop;

                float invLength = 1f / EngineMath.sqrt(nearSquared + leftSquared);
                cLeft0 = fNear * invLength;
                cLeft1 = -fLeft * invLength;

                invLength = 1f / EngineMath.sqrt(nearSquared + rightSquared);
                cRight0 = -fNear * invLength;
                cRight1 = fRight * invLength;

                invLength = 1f / EngineMath.sqrt(nearSquared + bottomSquared);
                cBottom0 = fNear * invLength;
                cBottom1 = -fBottom * invLength;

                invLength = 1f / EngineMath.sqrt(nearSquared + topSquared);
                cTop0 = -fNear * invLength;
                cTop1 = fTop * invLength;
            } else if (projection == Projection.ORTHOGRAPHIC) {
                cLeft0 = 1;
                cLeft1 = 0;

                cRight0 = -1;
                cRight1 = 0;

                cBottom0 = 1;
                cBottom1 = 0;

                cTop0 = -1;
                cTop1 = 0;
            }

            frameDirty = true;
            updateProjection = true;

            updateFrame();
        }
    }

    /**
     * Updates this camera's frame.
     */
    public void updateFrame() {
        if (frameDirty) {
            float dirDotLoc = direction.dot(location);
            Vector3 planeNormal = Pools.Vector3.get();

            // left plane
            planeNormal.setX(left.getX() * cLeft0);
            planeNormal.setY(left.getY() * cLeft0);
            planeNormal.setZ(left.getZ() * cLeft0);
            planeNormal.add(direction.getX() * cLeft1, direction.getY() * cLeft1, direction.getZ() * cLeft1).normalize();

            frustum[Frustum.LEFT.value].setNormal(planeNormal);
            frustum[Frustum.LEFT.value].setConstant(location.dot(planeNormal));

            // right plane
            planeNormal.setX(left.getX() * cRight0);
            planeNormal.setY(left.getY() * cRight0);
            planeNormal.setZ(left.getZ() * cRight0);
            planeNormal.add(direction.getX() * cRight1, direction.getY() * cRight1, direction.getZ() * cRight1).normalize();

            frustum[Frustum.RIGHT.value].setNormal(planeNormal);
            frustum[Frustum.RIGHT.value].setConstant(location.dot(planeNormal));

            // bottom plane
            planeNormal.setX(up.getX() * cBottom0);
            planeNormal.setY(up.getY() * cBottom0);
            planeNormal.setZ(up.getZ() * cBottom0);
            planeNormal.add(direction.getX() * cBottom1, direction.getY() * cBottom1, direction.getZ() * cBottom1).normalize();

            frustum[Frustum.BOTTOM.value].setNormal(planeNormal);
            frustum[Frustum.BOTTOM.value].setConstant(location.dot(planeNormal));

            // top plane
            planeNormal.setX(up.getX() * cTop0);
            planeNormal.setY(up.getY() * cTop0);
            planeNormal.setZ(up.getZ() * cTop0);
            planeNormal.add(direction.getX() * cTop1, direction.getY() * cTop1, direction.getZ() * cTop1).normalize();

            frustum[Frustum.TOP.value].setNormal(planeNormal);
            frustum[Frustum.TOP.value].setConstant(location.dot(planeNormal));

            // far plane
            planeNormal.set(direction).multiply(-1f);
            frustum[Frustum.FAR.value].setNormal(planeNormal);
            frustum[Frustum.FAR.value].setConstant(-(dirDotLoc + fFar));

            // near plane
            frustum[Frustum.NEAR.value].setNormal(direction);
            frustum[Frustum.NEAR.value].setConstant(dirDotLoc + fNear);

            if (projection == Projection.ORTHOGRAPHIC) {
                frustum[Frustum.LEFT.value].setConstant(frustum[Frustum.LEFT.value].getConstant() + fLeft);
                frustum[Frustum.RIGHT.value].setConstant(frustum[Frustum.RIGHT.value].getConstant() - fRight);
                frustum[Frustum.TOP.value].setConstant(frustum[Frustum.TOP.value].getConstant() - fTop);
                frustum[Frustum.BOTTOM.value].setConstant(frustum[Frustum.BOTTOM.value].getConstant() + fBottom);
            }

            updateView = true;

            Pools.Vector3.put(planeNormal);
        }
    }

    /**
     * Updates this camera's projection matrix.
     */
    public void updateProjection() {
        updateFrustum();

        if (updateProjection) {
            projMatrix.set(Matrix4.IDENTITY);

            if (projection == Projection.PERSPECTIVE) {
                projMatrix.set(0, 0, (2f * fNear) / (fRight - fLeft));
                projMatrix.set(1, 1, (2f * fNear) / (fTop - fBottom));
                projMatrix.set(2, 2, -(fFar + fNear) / (fFar - fNear));
                projMatrix.set(3, 3, 0f);
                projMatrix.set(2, 0, (fRight + fLeft) / (fRight - fLeft));
                projMatrix.set(2, 1, (fTop + fBottom) / (fTop - fBottom));
                projMatrix.set(2, 3, -1f);
                projMatrix.set(3, 2, -(2f * fFar * fNear) / (fFar - fNear));
            } else if (projection == Projection.ORTHOGRAPHIC) {
                projMatrix.set(0, 0, 2f / (fRight - fLeft));
                projMatrix.set(1, 1, 2f / (fTop - fBottom));
                projMatrix.set(2, 2, -2f / (fFar - fNear));
                projMatrix.set(3, 3, 1f);
                projMatrix.set(3, 0, -(fRight + fLeft) / (fRight - fLeft));
                projMatrix.set(3, 1, -(fTop + fBottom) / (fTop - fBottom));
                projMatrix.set(3, 2, -(fFar + fNear) / (fFar - fNear));
            }

            updateProjection = false;
        }
    }

    /**
     * Updates this camera's view matrix.
     */
    public void updateView() {
        updateFrame();

        if (updateView) {
            viewMatrix.set(Matrix4.IDENTITY);

            viewMatrix.set(0, 0, -left.getX());
            viewMatrix.set(1, 0, -left.getY());
            viewMatrix.set(2, 0, -left.getZ());

            viewMatrix.set(0, 1, up.getX());
            viewMatrix.set(1, 1, up.getY());
            viewMatrix.set(2, 1, up.getZ());

            viewMatrix.set(0, 2, -direction.getX());
            viewMatrix.set(1, 2, -direction.getY());
            viewMatrix.set(2, 2, -direction.getZ());

            viewMatrix.set(3, 0, left.dot(location));
            viewMatrix.set(3, 1, -up.dot(location));
            viewMatrix.set(3, 2, direction.dot(location));

            updateView = false;
        }
    }

    /**
     * Updates this camera's view-projection matrix.
     */
    public void updateViewProjection() {
        updateProjection();
        updateView();

        if (dirty) {
            viewProjMatrix.set(viewMatrix).multiply(projMatrix);
        }
    }

    /**
     * Gives a float buffer containing this camera's view matrix values.
     *
     * @return float buffer containing this camera's view matrix values
     */
    public FloatBuffer getViewBuffer() {
        if (dirty) {
            viewMatrix.toFloatBuffer(viewBuffer);
        }

        return viewBuffer;
    }

    /**
     * Gives a float buffer containing this camera's projection matrix values.
     *
     * @return float buffer containing this camera's projection matrix values
     */
    public FloatBuffer getProjectionBuffer() {
        if (dirty) {
            projMatrix.toFloatBuffer(projBuffer);
        }

        return projBuffer;
    }

    /**
     * Gives a float buffer containing this camera's view-projection matrix values.
     *
     * @return float buffer containing this camera's view-projection matrix values
     */
    public FloatBuffer getViewProjectionBuffer() {
        if (dirty) {
            viewProjMatrix.toFloatBuffer(viewProjBuffer);
        }

        return viewProjBuffer;
    }

    /**
     * Calculates the corners of the frustum in world space.
     *
     * @param storage - output storage array of vectors
     *
     * @return - corners of the frustum in world space
     */
    public Vector3[] getCorners(Vector3[] storage) {
        if (storage == null) {
            storage = new Vector3[8];
        }

        Matrix4 matrix = Pools.Matrix4.get();
        Vector4 vector = Pools.Vector4.get();

        matrix.set(getViewProjectionMatrix()).invert();

        for (int i = 0; i < 8; i++) {
            matrix.transform(NDC[i], vector).divide(vector.getW());
            storage[i] = vector.toVector3(storage[i]);
        }

        Pools.Matrix4.put(matrix);
        Pools.Vector4.put(vector);

        return storage;
    }

    /**
     * Unsets this camera's dirty flags.
     */
    public void clean() {
        dirty = false;
        locDirty = false;
        dirDirty = false;
        frameDirty = false;
        frustumDirty = false;
        fieldRangeDirty = false;
        focalDistanceDirty = false;
    }

    /**
     * Resets this camera's dirty flags.
     */
    public void reset() {
        dirty = true;
        locDirty = true;
        dirDirty = true;
        frameDirty = true;
        frustumDirty = true;
        fieldRangeDirty = true;
        focalDistanceDirty = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "\n"
                + "Location: " + location + "\n"
                + "Direction: " + direction + "\n"
                + "Projection: " + projection;
    }
}
