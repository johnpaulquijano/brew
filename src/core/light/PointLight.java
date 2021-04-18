package core.light;

import core.math.Vector3;
import core.Light;
import core.LightType;
import core.shadow.PointShadow;
import core.utility.EngineException;

/**
 * A light source originating from a point in space and radiating to all directions.
 *
 * @author John Paul Quijano
 */
public class PointLight extends Light<PointShadow> {
    public static final Vector3 DEFAULT_LOCATION = Vector3.ZERO;
    public static final Vector3 DEFAULT_ATTENUATION = new Vector3(0.5f, 0.05f, 0.005f);

    private boolean locDirty;
    private Vector3 location;
    private Vector3 attenuation;

    /**
     * Creates a point light with default attributes.
     */
    public PointLight() {
        super(LightType.POINT);

        location = new Vector3(DEFAULT_LOCATION);
        attenuation = new Vector3(DEFAULT_ATTENUATION);

        locDirty = true;

        dataBuffer.put(4, location.getX()).put(5, location.getY()).put(6, location.getZ());
        dataBuffer.put(12, attenuation.getX()).put(13, attenuation.getY()).put(14, attenuation.getZ());
    }

    /**
     * Sets this point light's attributes to the given light's attributes.
     *
     * @param light - light to copy attributes from
     *
     * @return this point light
     */
    public PointLight set(PointLight light) {
        super.set(light);

        setLocation(light.location);
        setAttenuation(light.attenuation);

        return this;
    }

    /**
     * Sets the location.
     *
     * @param x - x location
     * @param y - y location
     * @param z - z location
     */
    public void setLocation(float x, float y, float z) {
        location.set(x, y, z);
        dataBuffer.put(4, x).put(5, y).put(6, z);
        locDirty = true;
        dirty = true;
    }

    /**
     * Sets the location.
     *
     * @param location - three-component location vector
     */
    public void setLocation(Vector3 location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Gives the location.
     *
     * @return location
     */
    public Vector3 getLocation() {
        return location;
    }

    /**
     * Sets the attenuation.
     *
     * @param constant - constant attenuation
     * @param linear - linear attenuation
     * @param quadratic - quadratic attenuation
     */
    public void setAttenuation(float constant, float linear, float quadratic) {
        attenuation.set(constant, linear, quadratic);
        dataBuffer.put(12, constant).put(13, linear).put(14, quadratic);
        dirty = true;
    }

    /**
     * Sets the attenuation.
     *
     * @param attenuation - three-component attenuation vector
     */
    public void setAttenuation(Vector3 attenuation) {
        setAttenuation(attenuation.getX(), attenuation.getY(), attenuation.getZ());
    }

    /**
     * Gives the attenuation.
     *
     * @return attenuation
     */
    public Vector3 getAttenuation() {
        return attenuation;
    }

    /**
     * Checks if this point light has moved.
     *
     * @return true if this point light has moved
     */
    public boolean isLocationDirty() {
        return locDirty;
    }

    @Override
    public void clean() {
        super.clean();
        locDirty = false;
    }
}
