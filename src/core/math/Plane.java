package core.math;

import core.utility.Poolable;

/**
 * Implementation of the plane equation.
 *
 * @author John Paul Quijano
 */
public final class Plane implements Poolable {
    public enum Position {
        NEITHER(0),
        POSITIVE(1),
        NEGATIVE(-1);

        private int value;

        Position(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private float constant;
    private Vector3 normal;

    /**
     * Creates a plane with a unit-y normal and a zero constant.
     */
    public Plane() {
        this(Vector3.UNIT_Y, 0);
    }

    /**
     * Creates a plane with the given normal and constant.
     *
     * @param normal - plane normal
     * @param constant - constant part of the equation
     */
    public Plane(Vector3 normal, float constant) {
        this.normal = new Vector3(normal);
        this.constant = constant;
    }

    /**
     * Sets this plane's attributes to that of the given template.
     *
     * @param template - plane to copy attributes from
     *
     * @return this plane
     */
    public Plane set(Plane template) {
        setConstant(template.constant);
        setNormal(template.normal);
        return this;
    }

    /**
     * Sets this plane's attributes to the given normal and constant.
     *
     * @param normal - plane normal
     * @param constant - constant part of the equation
     * @return this plane
     */
    public Plane set(Vector3 normal, float constant) {
        this.constant = constant;
        this.normal.set(normal);
        return this;
    }

    /**
     * Defines this plane given a triangle.
     *
     * @param pointA - first point of the triangle
     * @param pointB - second point of the triangle
     * @param pointC - third point of the triangle
     *
     * @return this plane
     */
    public Plane set(Vector3 pointA, Vector3 pointB, Vector3 pointC) {
        normal.set(pointB).subtract(pointA);
        normal.cross(pointC.getX() - pointA.getX(), pointC.getY() - pointA.getY(), pointC.getZ() - pointA.getZ()).normalize();
        constant = normal.dot(pointA);

        return this;
    }

    /**
     * Sets this plane's constant.
     *
     * @param constant - value to set
     *
     * @return this plane
     */
    public Plane setConstant(float constant) {
        this.constant = constant;
        return this;
    }

    /**
     * Gives the constant term of the plane equation.
     *
     * @return constant term of the plane equation
     */
    public float getConstant() {
        return constant;
    }

    /**
     * Sets this plane's normal.
     *
     * @param x - first vector component
     * @param y - second vector component
     * @param z - third vector component
     *
     * @return this plane
     */
    public Plane setNormal(float x, float y, float z) {
        normal.set(x, y, z);
        return this;
    }

    /**
     * Sets this plane's normal.
     *
     * @param normal - the new normal vector
     *
     * @return this plane
     */
    public Plane setNormal(Vector3 normal) {
        return setNormal(normal.getX(), normal.getY(), normal.getZ());
    }

    /**
     * Gives this plane's normal.
     *
     * @return normal
     */
    public Vector3 getNormal() {
        return normal;
    }



    /**
     * Calculates the signed squared distance from the point to this plane. The sign of this value depends on the
     * position of the point relative to this plane. This value is 0 if the point is on the plane.
     *
     * @return signed distance
     */
    public float signedDistance(Vector3 point) {
        return normal.dot(point) - constant;
    }

    /**
     * Determines the position of the point relative to this plane.
     *
     * @param point - the reference point
     *
     * @return position of the point relative to this plane
     */
    public Position position(Vector3 point) {
        float distance = signedDistance(point);

        if (distance < 0) {
            return Position.NEGATIVE;
        }

        if (distance > 0) {
            return Position.POSITIVE;
        }

        return Position.NEITHER;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + normal.toString() + ", " + constant + "]";
    }

    /**
     * Checks for field-for-field equality.
     *
     * @return true if this plane equals the given plane field-for-field
     */
    public boolean equals(Plane plane) {
        return plane.getConstant() == constant && plane.normal.equals(normal);
    }
}
