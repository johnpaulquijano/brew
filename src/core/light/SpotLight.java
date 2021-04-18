package core.light;

import core.math.EngineMath;
import core.math.Vector3;
import core.Light;
import core.LightType;
import core.shadow.SpotShadow;

/**
 * A focused light source originating from a point in space.
 *
 * @author John Paul Quijano
 */
public class SpotLight extends Light<SpotShadow> {
    public static final float DEFAULT_EXPONENT = 1f;
    public static final float DEFAULT_CUTOFF = EngineMath.QUARTER_PI;
    public static final Vector3 DEFAULT_LOCATION = Vector3.ONE;
    public static final Vector3 DEFAULT_DIRECTION = Vector3.NEG_ONE;
    public static final Vector3 DEFAULT_ATTENUATION = new Vector3(0.5f, 0.05f, 0.005f);

    private float cutoff;
    private float exponent;
    private boolean locDirty;
    private boolean dirDirty;
    private boolean cutoffDirty;
    private Vector3 location;
    private Vector3 direction;
    private Vector3 attenuation;

    /**
     * Creates a spot light with default attributes.
     */
    public SpotLight() {
        super(LightType.SPOT);

        cutoff = DEFAULT_CUTOFF;
        exponent = DEFAULT_EXPONENT;
        location = new Vector3(DEFAULT_LOCATION);
        direction = new Vector3(DEFAULT_DIRECTION);
        attenuation = new Vector3(DEFAULT_ATTENUATION);

        locDirty = true;
        dirDirty = true;
        cutoffDirty = true;

        dataBuffer.put(11, cutoff * 0.5f);
        dataBuffer.put(15, exponent);
        dataBuffer.put(4, location.getX()).put(5, location.getY()).put(6, location.getZ());
        dataBuffer.put(8, direction.getX()).put(9, direction.getY()).put(10, direction.getZ());
        dataBuffer.put(12, attenuation.getX()).put(13, attenuation.getY()).put(14, attenuation.getZ());
    }

    /**
     * Sets this spot light's attributes to the given light's attributes.
     *
     * @param light - light to copy attributes from
     *
     * @return this spot light
     */
    public SpotLight set(SpotLight light) {
        super.set(light);

        setCutoff(light.cutoff);
        setExponent(light.exponent);
        setLocation(light.location);
        setDirection(light.direction);
        setAttenuation(light.attenuation);

        return this;
    }

    /**
     * Sets the angle of spread.
     */
    public void setCutoff(float cutoff) {
        this.cutoff = EngineMath.clamp(cutoff, 0f, EngineMath.PI);
        dataBuffer.put(11, this.cutoff * 0.5f);
        cutoffDirty = true;
        dirty = true;
    }

    /**
     * Gives the angle of spread.
     *
     * @return angle of spread
     */
    public float getCutoff() {
        return cutoff;
    }

    /**
     * Sets the intensity factor.
     */
    public void setExponent(float exponent) {
        this.exponent = exponent;
        dataBuffer.put(15, exponent);
        dirty = true;
    }

    /**
     * Gives the intensity factor.
     *
     * @return intensity factor
     */
    public float getExponent() {
        return exponent;
    }

    /**
     * Sets the direction.
     *
     * @param x - x-direction
     * @param y - y-direction
     * @param z - z-direction
     */
    public void setDirection(float x, float y, float z) {
        direction.set(x, y, z).normalize();
        dataBuffer.put(8, x).put(9, y).put(10, z);
        locDirty = true;
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
     * Orients this light towards the direction defined by this light's location and the given coordinates.
     *
     * @param x - x-location
     * @param y - y-location
     * @param z - z-location
     */
    public void lookAt(float x, float y, float z) {
        direction.set(x, y, z).subtract(location).normalize();
        dataBuffer.put(8, direction.getX()).put(9, direction.getY()).put(10, direction.getZ());
        locDirty = true;
        dirty = true;
    }

    /**
     * Orients this light towards the direction defined by this light's location and the given coordinates.
     *
     * @param point - three-component location vector
     */
    public void lookAt(Vector3 point) {
        lookAt(point.getX(), point.getY(), point.getZ());
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
     * Sets the location.
     *
     * @param x - x-location
     * @param y - y-location
     * @param z - z-location
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
     * Checks if location has changed.
     *
     * @return true if location has changed
     */
    public boolean isLocationDirty() {
        return locDirty;
    }

    /**
     * Checks if direction has changed.
     *
     * @return true if direction has changed
     */
    public boolean isDirectionDirty() {
        return dirDirty;
    }

    /**
     * Checks if cutoff has changed.
     *
     * @return true if cutoff has changed
     */
    public boolean isCutoffDirty() {
        return cutoffDirty;
    }

    @Override
    public void clean() {
        super.clean();

        locDirty = false;
        dirDirty = false;
        cutoffDirty = false;
    }
}
