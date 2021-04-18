package core;

import core.math.Vector3;
import core.utility.Colors;

import java.nio.IntBuffer;
import java.util.List;

/**
 * Base class for all influencing lights.
 *
 * @author John Paul Quijano
 */
public abstract class Light<S extends Shadow> extends CacheObject {
    public static final int DATA_SIZE = 16;

    public static final Vector3 DEFAULT_COLOR = new Vector3(Colors.WHITE3);

    protected boolean dirty;
    protected boolean enabled;
    protected Vector3 color;
    protected LightType type;
    protected S shadow;

    /**
     * Creates a light with default attributes.
     */
    public Light(LightType t) {
        super(DATA_SIZE);

        type = t;
        dirty = true;
        enabled = true;
        color = new Vector3(DEFAULT_COLOR);

        dataBuffer.put(0, color.getX()).put(1, color.getY()).put(2, color.getZ());
        dataBuffer.put(3, enabled ? 1f : 0f);
        dataBuffer.put(7, type.getID());
    }

    /**
     * Gives the type of this light.
     *
     * @return type of this light
     */
    public LightType getType() {
        return type;
    }

    /**
     * Sets this light's attributes to the given template's attributes.
     *
     * @param template - template to copy attributes from
     *
     * @return this light
     */
    public void set(Light template) {
        setColor(template.color);
        setEnabled(template.enabled);
    }

    /**
     * Turns this light on or off.
     *
     * @param enabled - if true, light is turned on
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        dataBuffer.put(3, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Checks if this light is enabled.
     *
     * @return true if this light is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets this light's color.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
        dataBuffer.put(0, r).put(1, g).put(2, b);
        dirty = true;
    }

    /**
     * Sets this light's color.
     *
     * @param color - three-component color vector
     */
    public void setColor(Vector3 color) {
        setColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives this light's color.
     *
     * @return color
     */
    public Vector3 getColor() {
        return color;
    }

    /**
     * Sets the shadow source.
     *
     * @param shadow - shadow source
     */
    public void setShadow(S shadow) {
        if (shadow != null) {
            shadow.setNotify(this);
        }

        if (this.shadow != null) {
            this.shadow.removeNotify();
        }

        this.shadow = shadow;
    }

    /**
     * Gives the shadow source.
     *
     * @return shadow source
     */
    public S getShadow() {
        return shadow;
    }

    /**
     * Checks if this light has been modified since the last frame.
     *
     * @return true if this light has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Resets this light's dirty flags.
     */
    public void clean() {
        dirty = false;
    }
}
