package core.math;

import core.utility.Poolable;

/**
 * A geometric object with an origin and direction.
 *
 * @author John Paul Quijano
 */
public final class Ray implements Poolable {
    private Vector3 origin;
    private Vector3 direction;

    /**
     * Creates a ray with zero origin and direction.
     */
    public Ray() {
        origin = new Vector3();
        direction = new Vector3();
    }

    /**
     * Creates a ray with the given origin and direction.
     *
     * @param origin - location of the ray
     * @param direction - direction of the ray
     */
    public Ray(Vector3 origin, Vector3 direction) {
        this.origin = new Vector3(origin);
        this.direction = new Vector3(direction);
    }

    /**
     * Sets this ray's origin.
     *
     * @return this ray
     */
    public Ray setOrigin(Vector3 origin) {
        this.origin.set(origin);
        return this;
    }

    /**
     * Gives this ray's origin.
     *
     * @return this ray's origin
     */
    public Vector3 getOrigin() {
        return origin;
    }

    /**
     * Sets this ray's direction.
     *
     * @return this ray
     */
    public Ray setDirection(Vector3 direction) {
        this.direction.set(direction);
        return this;
    }

    /**
     * Gives this ray's direction.
     *
     * @return this ray's direction
     */
    public Vector3 getDirection() {
        return direction;
    }

    /**
     * Sets this ray's attributes with the given template's.
     *
     * @param template - the template to copy attributes from
     *
     * @return this ray
     */
    public Ray set(Ray template) {
        origin.set(template.origin);
        direction.set(template.direction);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Origin: (" + origin.getX() + ", " + origin.getY() + ", " + origin.getZ() + "), "
                + "Direction: (" + direction.getX() + ", " + direction.getY() + ", " + direction.getZ() + ")]";
    }

    /**
     * Field-for-field equality.
     *
     * @return true if this ray equals the given ray field-for-field
     */
    public boolean equals(Ray ray) {
        return origin.equals(origin) && direction.equals(direction);
    }
}
