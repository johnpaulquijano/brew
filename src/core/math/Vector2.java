package core.math;

import core.utility.Poolable;

/**
 * A 2-component floating-point vector.
 *
 * @author John Paul Quijano
 */
public class Vector2 implements Poolable {
    public static final Vector2 ZERO = new Vector2(0f, 0f);
    public static final Vector2 ONE = new Vector2(1f, 1f);
    public static final Vector2 NEG_ONE = new Vector2(-1f, -1f);
    public static final Vector2 UNIT_X = new Vector2(1f, 0f);
    public static final Vector2 NEG_UNIT_X = new Vector2(-1f, 0f);
    public static final Vector2 UNIT_Y = new Vector2(0f, 1f);
    public static final Vector2 NEG_UNIT_Y = new Vector2(0f, -1f);

    private float x;
    private float y;

    /**
     * Creates a zero 2-component vector.
     */
    public Vector2() {
        x = 0f;
        y = 0f;
    }

    /**
     * Creates a 2-component vector from the given template.
     *
     * @param template - the template to copy attributes from
     */
    public Vector2(Vector2 template) {
        x = template.x;
        y = template.y;
    }

    /**
     * Creates a zero 2-component vector with the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Randomizes a vector.
     *
     * @param output - stroage vector for the result
     *
     * @return the output vector
     */
    public static Vector2 random(Vector2 output) {
        if (output == null) {
            output = new Vector2();
        }

        output.x = EngineMath.RANDOM.nextFloat();
        output.y = EngineMath.RANDOM.nextFloat();

        return output;
    }

    /**
     * Sets this vector's first component.
     *
     * @return this vector
     */
    public Vector2 setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Gives this vector's first component.
     *
     * @return first component
     */
    public float getX() {
        return x;
    }

    /**
     * Sets this vector's second component.
     *
     * @return this vector
     */
    public Vector2 setY(float y) {
        this.y = y;
        return this;
    }

    /**
     * Gives this vector's second component.
     *
     * @return second component
     */
    public float getY() {
        return y;
    }

    /**
     * Sets this vector's components.
     *
     * @return this vector
     */
    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    /**
     * Sets this vector's attributes to the given template's.
     *
     * @param template - the template to copy attributes from
     *
     * @return this vector
     */
    public Vector2 set(Vector2 template) {
        x = template.x;
        y = template.y;

        return this;
    }

    /**
     * Performs vector addition.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return this vector
     */
    public Vector2 add(float x, float y) {
        return set(this.x + x, this.y + y);
    }

    /**
     * Performs vector addition.
     *
     * @param input - vector to add
     *
     * @return this vector
     */
    public Vector2 add(Vector2 input) {
        return add(input.x, input.y);
    }

    /**
     * Performs vector subtraction.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return this vector
     */
    public Vector2 subtract(float x, float y) {
        return set(this.x - x, this.y - y);
    }

    /**
     * Performs vector subtraction.
     *
     * @param input - vector to subtract
     *
     * @return this vector
     */
    public Vector2 subtract(Vector2 input) {
        return subtract(input.x, input.y);
    }

    /**
     * Performs vector multiplication.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this vector
     */
    public Vector2 multiply(float scalar) {
        return set(x * scalar, y * scalar);
    }

    /**
     * Performs vector multiplication.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return this vector
     */
    public Vector2 multiply(float x, float y) {
        return set(this.x * x, this.y * y);
    }

    /**
     * Performs vector multiplication.
     *
     * @param input - vector to multiply by
     *
     * @return this vector
     */
    public Vector2 multiply(Vector2 input) {
        return multiply(input.x, input.y);
    }

    /**
     * Performs vector division.
     *
     * @param scalar - scalar value to divide by
     *
     * @return this vector
     */
    public Vector2 divide(float scalar) {
        float invScalar = 1f / scalar;
        return set(x * invScalar, y * invScalar);
    }

    /**
     * Performs vector division.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return this vector
     */
    public Vector2 divide(float x, float y) {
        return set(this.x / x, this.y / y);
    }

    /**
     * Performs vector multiplication.
     *
     * @param input - vector to divide by
     *
     * @return this vector
     */
    public Vector2 divide(Vector2 input) {
        return divide(input.x, input.y);
    }

    /**
     * Flips the sign of each component.
     *
     * @return this vector
     */
    public Vector2 negate() {
        return set(-x, -y);
    }

    /**
     * Sets all components positive.
     *
     * @return this vector
     */
    public Vector2 absolute() {
        return set(EngineMath.abs(x), EngineMath.abs(y));
    }

    /**
     * Calculates a unit vector.
     *
     * @return this vector
     */
    public Vector2 normalize() {
        float squaredLength = magnitudeSquared();

        if (EngineMath.abs(squaredLength) > EngineMath.EPSILON) {
            return multiply(EngineMath.invSqrt(squaredLength));
        }

        return this;
    }

    /**
     * Linearly interpolates between this vector and the given end vector by the given delta.
     *
     * @param end the destination
     * @param delta the step amount
     *
     * @return this vector
     */
    public Vector2 lerp(Vector2 end, float delta) {
        if (end.equals(this) || delta == 0f) {
            return this;
        }

        return set(EngineMath.lerp(this.x, end.x, delta),
                EngineMath.lerp(this.y, end.y, delta));
    }

    /**
     * Calculates this vector's squared length.
     *
     * @return this vector's squared length
     */
    public float magnitudeSquared() {
        return x * x + y * y;
    }

    /**
     * Calculates this vector's length.
     *
     * @return this vector's length
     */
    public float magnitude() {
        return EngineMath.sqrt(magnitudeSquared());
    }

    /**
     * Calculates the squared distance between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return squared distance
     */
    public float distanceSquared(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;

        return dx * dx + dy * dy;
    }

    /**
     * Calculates the squared distance between this vector and the given coordinates.
     *
     * @param input - vector to calculate squared distance from
     *
     * @return squared distance
     */
    public float distanceSquared(Vector2 input) {
        return distanceSquared(input.x, input.y);
    }

    /**
     * Calculates the distance between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return distance
     */
    public float distance(float x, float y) {
        return EngineMath.sqrt(distanceSquared(x, y));
    }

    /**
     * Calculates the distance between this vector and the given coordinates.
     *
     * @param input - vector to calculate distance from
     *
     * @return distance
     */
    public float distance(Vector2 input) {
        return EngineMath.sqrt(Vector2.this.distanceSquared(input));
    }

    /**
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     *
     * @return dot product
     */
    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    /**
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param input - vector to calculate dot product with
     *
     * @return dot product
     */
    public float dot(Vector2 input) {
        return dot(input.x, input.y);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + y + ")";
    }

    /**
     * Checks for field-for-field equality.
     *
     * @return true if this quaternion equals the given quaternion field-for-field
     */
    public boolean equals(Vector2 input) {
        return input.x == x && input.y == y;
    }
}
