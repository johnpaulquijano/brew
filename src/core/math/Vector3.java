package core.math;

import core.utility.Poolable;

/**
 * A 3-component floating-point vector.
 *
 * @author John Paul Quijano
 */
public class Vector3 implements Poolable {
    public static final Vector3 ZERO = new Vector3(0f, 0f, 0f);
    public static final Vector3 ONE = new Vector3(1f, 1f, 1f);
    public static final Vector3 NEG_ONE = new Vector3(-1f, -1f, -1f);
    public static final Vector3 UNIT_X = new Vector3(1f, 0f, 0f);
    public static final Vector3 NEG_UNIT_X = new Vector3(-1f, 0f, 0f);
    public static final Vector3 UNIT_Y = new Vector3(0f, 1f, 0f);
    public static final Vector3 NEG_UNIT_Y = new Vector3(0f, -1f, 0f);
    public static final Vector3 UNIT_Z = new Vector3(0f, 0f, 1f);
    public static final Vector3 NEG_UNIT_Z = new Vector3(0f, 0f, -1f);

    private float x;
    private float y;
    private float z;

    /**
     * Creates a zero 3-component vector.
     */
    public Vector3() {
        x = 0f;
        y = 0f;
        z = 0f;
    }

    /**
     * Creates a 3-component vector from the given template.
     *
     * @param template - template to copy attributes from
     */
    public Vector3(Vector3 template) {
        x = template.x;
        y = template.y;
        z = template.z;
    }

    /**
     * Creates a zero 3-component vector with the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     */
    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Randomizes a vector.
     *
     * @param output - stroage vector for the result
     *
     * @return a randomized vector
     */
    public static Vector3 random(Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        output.x = EngineMath.RANDOM.nextFloat();
        output.y = EngineMath.RANDOM.nextFloat();
        output.z = EngineMath.RANDOM.nextFloat();

        return output;
    }

    /**
     * Sets this vector's first component.
     *
     * @return this vector
     */
    public Vector3 setX(float x) {
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
    public Vector3 setY(float y) {
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
     * Sets this vector's third component.
     *
     * @return this vector
     */
    public Vector3 setZ(float z) {
        this.z = z;
        return this;
    }

    /**
     * Gives this vector's third component.
     *
     * @return third component
     */
    public float getZ() {
        return z;
    }

    /**
     * Sets this vector's components.
     *
     * @return this vector
     */
    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    /**
     * Sets this vector's attributes to the given template's.
     *
     * @param template - the template to copy attributes from
     *
     * @return this vector
     */
    public Vector3 set(Vector3 template) {
        x = template.x;
        y = template.y;
        z = template.z;

        return this;
    }

    /**
     * Performs vector addition.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return this vector
     */
    public Vector3 add(float x, float y, float z) {
        return set(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Performs vector addition.
     *
     * @param input - vector to add
     *
     * @return this vector
     */
    public Vector3 add(Vector3 input) {
        return add(input.x, input.y, input.z);
    }

    /**
     * Performs vector subtraction.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return this vector
     */
    public Vector3 subtract(float x, float y, float z) {
        return set(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Performs vector subtraction.
     *
     * @param input - vector to subtract
     *
     * @return this vector
     */
    public Vector3 subtract(Vector3 input) {
        return subtract(input.x, input.y, input.z);
    }

    /**
     * Performs vector multiplication.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this vector
     */
    public Vector3 multiply(float scalar) {
        return set(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Performs vector multiplication.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return this vector
     */
    public Vector3 multiply(float x, float y, float z) {
        return set(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Performs vector multiplication.
     *
     * @param input - vector to multiply by
     *
     * @return this vector
     */
    public Vector3 multiply(Vector3 input) {
        return multiply(input.x, input.y, input.z);
    }

    /**
     * Performs vector division.
     *
     * @param scalar - scalar value to divde by
     *
     * @return this vector
     */
    public Vector3 divide(float scalar) {
        float invScalar = 1f / scalar;
        return set(x * invScalar, y * invScalar, z * invScalar);
    }

    /**
     * Performs vector division.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return this vector
     */
    public Vector3 divide(float x, float y, float z) {
        return set(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Performs vector division.
     *
     * @param input - vector to divide by
     *
     * @return this vector
     */
    public Vector3 divide(Vector3 input) {
        return divide(input.x, input.y, input.z);
    }

    /**
     * Flips the sign of each component.
     *
     * @return this vector
     */
    public Vector3 negate() {
        return set(-x, -y, -z);
    }

    /**
     * Sets all components positive.
     *
     * @return this vector
     */
    public Vector3 absolute() {
        return set(EngineMath.abs(x), EngineMath.abs(y), EngineMath.abs(z));
    }

    /**
     * Calculates a unit vector.
     *
     * @return this vector
     */
    public Vector3 normalize() {
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
    public Vector3 lerp(Vector3 end, float delta) {
        if (end.equals(this) || delta == 0f) {
            return this;
        }

        return set(EngineMath.lerp(this.x, end.x, delta),
                EngineMath.lerp(this.y, end.y, delta),
                EngineMath.lerp(this.z, end.z, delta));
    }

    /**
     * Calculates this vector's squared length.
     *
     * @return vector's squared length
     */
    public float magnitudeSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * Calculates this vector's length.
     *
     * @return vector's length
     */
    public float magnitude() {
        return EngineMath.sqrt(magnitudeSquared());
    }

    /**
     * Calculates the squared distance between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return squared distance
     */
    public float distanceSquared(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;

        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the squared distance between this vector and the given coordinates.
     *
     * @param input - vector to calculate distance from
     *
     * @return squared distance
     */
    public float distanceSquared(Vector3 input) {
        return distanceSquared(input.x, input.y, input.z);
    }

    /**
     * Calculates the distance between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return distance
     */
    public float distance(float x, float y, float z) {
        return EngineMath.sqrt(distanceSquared(x, y, z));
    }

    /**
     * Calculates the distance between this vector and the given coordinates.
     *
     * @param input - vector to calculate distance from
     *
     * @return distance
     */
    public float distance(Vector3 input) {
        return EngineMath.sqrt(distanceSquared(input));
    }

    /**
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return dot product
     */
    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    /**
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param input - vector to calculate dot product with
     *
     * @return dot product
     */
    public float dot(Vector3 input) {
        return dot(input.x, input.y, input.z);
    }

    /**
     * Calculates the cross product between this vector and the given coordinates then stores the result to this vector.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     *
     * @return this vector
     */
    public Vector3 cross(float x, float y, float z) {
        float newX = (this.y * z) - (this.z * y);
        float newY = (this.z * x) - (this.x * z);
        float newZ = (this.x * y) - (this.y * x);

        set(newX, newY, newZ);

        return this;
    }

    /**
     * Calculates the cross product between this vector and the given coordinates then stores the result to this vector.
     *
     * @param input - vector to calculate cross product with
     *
     * @return this vector
     */
    public Vector3 cross(Vector3 input) {
        return cross(input.x, input.y, input.z);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Checks for field-for-field equality.
     *
     * @return true if this quaternion equals the given quaternion field-for-field
     */
    public boolean equals(Vector3 input) {
        return input.x == x && input.y == y && input.z == z;
    }
}
