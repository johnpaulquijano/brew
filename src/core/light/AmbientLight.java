package core.light;

import core.Light;
import core.LightType;

/**
 * Approximates a light that seems to come from all directions.
 *
 * @author John Paul Quijano
 */
public class AmbientLight extends Light {
    public AmbientLight() {
        super(LightType.AMBIENT);
    }

    /**
     * Sets this ambient light's attributes to the given light's attributes.
     *
     * @param light - light to copy attributes from
     *
     * @param light - light to copy attributes from
     */
    public AmbientLight set(AmbientLight light) {
        super.set(light);
        return this;
    }
}
