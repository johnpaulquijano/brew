package core.math;

import core.utility.Poolable;

/**
 * A 2-component integer vector.
 *
 * @author John Paul Quijano
 */
public final class Point implements Poolable {
    public static final Point ZERO = new Point(0, 0);
    public static final Point ONE = new Point(1, 1);
    public static final Point NEG_ONE = new Point(-1, -1);
    public static final Point UNIT_X = new Point(1, 0);
    public static final Point NEG_UNIT_X = new Point(-1, 0);
    public static final Point UNIT_Y = new Point(0, 1);
    public static final Point NEG_UNIT_Y = new Point(0, -1);

    private int x;
    private int y;

    /**
     * Creates a zero point.
     */
    public Point() {
        x = 0;
        y = 0;
    }

    /**
     * Creates a point based on the given template.
     *
     * @param template - the template to copy attributes from
     */
    public Point(Point template) {
        x = template.x;
        y = template.y;
    }

    /**
     * Creates a point with the given coordinates.
     *
     * @param x - first component of this vector
     * @param y - second component of this vector
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the first component of this vector.
     *
     * @return this point
     */
    public Point setX(int x) {
        this.x = x;
        return this;
    }

    /**
     * Gives the first component of this vector.
     *
     * @return first component of this vector
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the second component of this vector.
     *
     * @return this point
     */
    public Point setY(int y) {
        this.y = y;
        return this;
    }

    /**
     * Gives the second component of this vector.
     *
     * @return second component of this vector
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the components of this vector.
     *
     * @param x - first component of this vector
     * @param y - second component of this vector
     *
     * @return this point
     */
    public Point set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets this point's components based on the give tempalate.
     *
     * @param template - point to copy attributes from
     *
     * @return this point
     */
    public Point set(Point template) {
        x = template.x;
        y = template.y;
        return this;
    }

    /**
     * Performs vector addition.
     *
     * @param x - first component input
     * @param y - second component input
     *
     * @return this point
     */
    public Point add(int x, int y) {
        return set(this.x + x, this.y + y);
    }

    /**
     * Performs vector addition.
     *
     * @param input - point to add to this point
     *
     * @return this point
     */
    public Point add(Point input) {
        return add(input.x, input.y);
    }

    /**
     * Performs vector subtraction.
     *
     * @param x - first component input
     * @param y - second component input
     *
     * @return this point
     */
    public Point subtract(int x, int y) {
        return set(this.x - x, this.y - y);
    }

    /**
     * Performs vector subtraction.
     *
     * @param input - point to subtract from this point
     *
     * @return this point
     */
    public Point subtract(Point input) {
        return subtract(input.x, input.y);
    }

    /**
     * Performs vector-scalar multiplication.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this point
     */
    public Point multiply(int scalar) {
        return set(x * scalar, y * scalar);
    }

    /**
     * Performs vector-vector multiplication.
     *
     * @param x - first component to multiply by
     * @param y - second component to multiply by
     *
     * @return this point
     */
    public Point multiply(int x, int y) {
        return set(this.x * x, this.y * y);
    }

    /**
     * Performs vector-vector multiplication.
     *
     * @param point - point to multiply by
     *
     * @return this point
     */
    public Point multiply(Point point) {
        return multiply(point.x, point.y);
    }

    /**
     * Performs vector-scalar division.
     *
     * @param scalar - scalar value to divide by
     *
     * @return this point
     */
    public Point divide(int scalar) {
        return set(x / scalar, y / scalar);
    }

    /**
     * Performs vector-vector division.
     *
     * @param x - first component to divide by
     * @param y - second component to divide by
     *
     * @return this point
     */
    public Point divide(int x, int y) {
        return set(this.x / x, this.y / y);
    }

    /**
     * Performs vector-vector division.
     *
     * @param point - point to divide by
     *
     * @return this point
     */
    public Point divide(Point point) {
        return divide(point.x, point.y);
    }


    /**
     * Calculates the squared distance between this point and the given coordinates.
     *
     * @param x - first component input
     * @param y - second component input
     *
     * @return squared distance
     */
    public int squaredDistance(int x, int y) {
        int dx = this.x - x;
        int dy = this.y - y;

        return dx * dx + dy * dy;
    }

    /**
     * Calculates the squared distance between this point and the given point.
     *
     * @param input - point to calculate distance from
     *
     * @return squared distance
     */
    public int squaredDistance(Point input) {
        return squaredDistance(input.x, input.y);
    }

    /**
     * Calculates the distance between this point and the given coordinates.
     *
     * @param x - first component input
     * @param y - second component input
     *
     * @return distance
     */
    public int distance(int x, int y) {
        return (int) Math.sqrt(squaredDistance(x, y));
    }

    /**
     * Calculates the distance between this point and the given point.
     *
     * @param input - point to calculate distance from
     *
     * @return distance
     */
    public int distance(Point input) {
        return (int) Math.sqrt(squaredDistance(input));
    }

    /**
     * Reverses the sign of each component.
     *
     * @return this point
     */
    public Point negate() {
        return set(-x, -y);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + y + ")";
    }

    /**
     * Field-for-field equality.
     *
     * @return true if this point equals the given point field-for-field
     */
    public boolean equals(Point point) {
        return point.x == x && point.y == y;
    }
}
