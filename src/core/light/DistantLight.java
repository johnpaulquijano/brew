package core.light;

import core.Shadow;
import core.math.Vector3;
import core.Light;
import core.LightType;
import core.shadow.DistantShadow;

import java.util.ArrayList;
import java.util.List;

/**
 * Approximates distant light sources such as the sun.
 *
 * @author John Paul Quijano
 */
public class DistantLight extends Light<DistantShadow> {
    public static final Vector3 DEFAULT_DIRECTION = Vector3.NEG_UNIT_Z;

    private boolean dirDirty;
    private Vector3 direction;

    /**
     * Creates a distant light with default attributes.
     */
    public DistantLight() {
        super(LightType.DISTANT);

        direction = new Vector3(DEFAULT_DIRECTION);
        dirDirty = true;

        dataBuffer.put(8, direction.getX()).put(9, direction.getY()).put(10, direction.getZ());
    }

    /**
     * Sets this distant light's attributes to the given light's attributes.
     *
     * @param light - light to copy attributes from
     *
     * @return this distant light
     */
    public DistantLight set(DistantLight light) {
        super.set(light);
        setDirection(light.direction);
        return this;
    }

    /**
     * Sets the direction.
     *
     * @param x - x direction
     * @param y - y direction
     * @param z - z direction
     */
    public void setDirection(float x, float y, float z) {
        direction.set(x, y, z).normalize();
        dataBuffer.put(8, x).put(9, y).put(10, z);
        dirDirty = true;
        dirty = true;
    }

    /**
     * Sets the direction.
     *
     * @param direction - three-component direction vector
     */
    public void setDirection(Vector3 direction) {
        setDirection(direction.getX(), direction.getY(), direction.getZ());
    }

    /**
     * Gives the direction.
     *
     * @return direction
     */
    public Vector3 getDirection() {
        return direction;
    }

    /**
     * Checks if direction has changed.
     *
     * @return true if direction has changed
     */
    public boolean isDirectionDirty() {
        return dirDirty;
    }

    @Override
    public void clean() {
        super.clean();
        dirDirty = false;
    }
}
