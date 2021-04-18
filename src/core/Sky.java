package core;

import core.math.Matrix4;
import core.utility.Buffers;
import core.utility.Pools;

import java.nio.FloatBuffer;

/**
 * A textured geometry rendered at infinity.
 *
 * @author John Paul Quijano
 */
public class Sky extends Spatial {
    private Geometry geometry;
    private TextureCube texture;
    private FloatBuffer worldViewProjBuffer;

    public Sky() {
        worldViewProjBuffer = Buffers.createFloatBuffer(16);
        setHierarchicalBoundsEnabled(false);
    }

    /**
     * Sets the sky geometry.
     *
     * @param geometry - sky geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Gives the sky geometry.
     *
     * @return sky geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the sky texture.
     *
     * @param texture - sky texture
     */
    public void setTexture(TextureCube texture) {
        this.texture = texture;
    }

    /**
     * Gives the sky texture.
     *
     * @return sky texture
     */
    public TextureCube getTexture() {
        return texture;
    }

    /**
     * Calculates this shape's world-view-projection transformations.
     *
     * @param camera - source camera of the view-projection transformation
     */
    public void calculateWVP(Camera camera) {
        /**
         * Calculate model-view-projection transformation matrix.
         */
        if (camera.isDirty()) {
            if (camera.isLocationDirty()) {
                worldTransform.setTranslation(camera.getLocation());
            }

            /** Calculate model-view-projection transformation matrix. */
            Matrix4 matrix = Pools.Matrix4.get();
            matrix.set(worldTransform.toMatrix()).multiply(camera.getViewProjectionMatrix()).toFloatBuffer(worldViewProjBuffer);
            Pools.Matrix4.put(matrix);
        }
    }

    /**
     * Gives the float buffer containing the world-view-projection transformation matrix values.
     *
     * @return float buffer containing the world-view-projection transformation matrix values
     */
    public FloatBuffer getWorldViewProjectionBuffer() {
        return worldViewProjBuffer;
    }

    @Override
    public void clean() {
        super.clean();

        texture.clean();
        geometry.clean();
    }
}
