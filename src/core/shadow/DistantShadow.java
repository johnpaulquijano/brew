package core.shadow;

import core.*;
import core.light.DistantLight;
import core.math.EngineMath;
import core.math.Vector3;
import core.utility.Pools;

/**
 * Shadow produced by a distant light.
 */
public class DistantShadow extends Shadow<DistantLight> {
    private Camera camera;
    private Vector3[] corners;

    public DistantShadow() {
        super(1);
        corners = new Vector3[8];
        shadowMap = new ShadowMap(resolution, false);
        cameras[0].setProjection(Camera.Projection.ORTHOGRAPHIC);
    }

    /**
     * Sets the main viewing camera used in light frustum calculation.
     *
     * @param camera - viewing camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Gives the main viewing camera used in light frustum calculation.
     *
     * @return main viewing camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Snaps the given value to the nearest multiple of the given unit.
     *
     * @param value - input value
     * @param unit - a quantum
     *
     * @return quantized value
     */
    private float quantize(float value, float unit){
        return EngineMath.round(value / unit) * unit;
    }

    @Override
    public void updateFrustum() {
        if (camera.isDirty() || source.isDirty()) {
            Vector3 coord = Pools.Vector3.get();

            float maxWidth = Float.MIN_VALUE;
            float minWidth = Float.MAX_VALUE;
            float maxHeight = Float.MIN_VALUE;
            float minHeight = Float.MAX_VALUE;
            float maxDepth = Float.MIN_VALUE;
            float minDepth = Float.MAX_VALUE;

            camera.getCorners(corners);

            for (int i = 0; i < 8; i++) {
                coord.set(corners[i]);

                /**
                 * Project frustum coordinates to light view space then get min and max values. Note that x-coordinate
                 * is negated to mean increasing values to the right.
                 */
                float x = -coord.dot(cameras[0].getLeft());
                float y = coord.dot(cameras[0].getUp());
                float z = coord.dot(cameras[0].getDirection());

                maxWidth = EngineMath.max(maxWidth, x);
                minWidth = EngineMath.min(minWidth, x);
                maxHeight = EngineMath.max(maxHeight, y);
                minHeight = EngineMath.min(minHeight, y);
                maxDepth = EngineMath.max(maxDepth, z);
                minDepth = EngineMath.min(minDepth, z);
            }

            coord.set(maxWidth, maxHeight, maxDepth).add(minWidth, minHeight, minDepth).multiply(0.5f);

            float extent = coord.distance(maxWidth, maxHeight, maxDepth);
            float quantum = 2f * extent / (float) resolution;

            coord.setX(quantize(coord.getX(), quantum));
            coord.setY(quantize(coord.getY(), quantum));
            coord.setZ(quantize(coord.getZ(), quantum));

            float left = coord.getX() - extent;
            float right = coord.getX() + extent;
            float bot = coord.getY() - extent;
            float top = coord.getY() + extent;
            float near = coord.getZ() - extent * 3f;
            float far = coord.getZ() + extent;

            cameras[0].lookAlong(source.getDirection(), Vector3.UNIT_Y);
            cameras[0].setOrthographicFrustum(left, right, bot, top, near, far);
            cameras[0].updateViewProjection();

            Pools.Vector3.put(coord);
        }
    }
}
