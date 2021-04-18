package core.math;

import core.utility.Poolable;

/**
 * A 4-component floating-point vector.
 *
 * @author John Paul Quijano
 */
public class Vector4 implements Poolable {
    public static final Vector4 ZERO = new Vector4(0f, 0f, 0f, 0f);
    public static final Vector4 ONE = new Vector4(1f, 1f, 1f, 1f);
    public static final Vector4 NEG_ONE = new Vector4(-1f, -1f, -1f, -1f);
    public static final Vector4 UNIT_X = new Vector4(1f, 0f, 0f, 0f);
    public static final Vector4 NEG_UNIT_X = new Vector4(-1f, 0f, 0f, 0f);
    public static final Vector4 UNIT_Y = new Vector4(0f, 1f, 0f, 0f);
    public static final Vector4 NEG_UNIT_Y = new Vector4(0f, -1f, 0f, 0f);
    public static final Vector4 UNIT_Z = new Vector4(0f, 0f, 1f, 0f);
    public static final Vector4 NEG_UNIT_Z = new Vector4(0f, 0f, -1f, 0f);
    public static final Vector4 UNIT_W = new Vector4(0f, 0f, 0f, 1f);
    public static final Vector4 NEG_UNIT_W = new Vector4(0f, 0f, 0f, -1f);

    private float x;
    private float y;
    private float z;
    private float w;

    /**
     * Creates a zero 4-component vector.
     */
    public Vector4() {
        x = 0f;
        y = 0f;
        z = 0f;
        w = 0f;
    }

    /**
     * Creates a 4-component vector from the given template.
     *
     * @param template - template to copy attributes from
     */
    public Vector4(Vector4 template) {
        x = template.x;
        y = template.y;
        z = template.z;
        w = template.w;
    }

    /**
     * Creates a zero 4-component vector with the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     */
    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Randomizes a vector.
     *
     * @param output - stroage vector for the result
     * @return a randomized vector
     */
    public static Vector4 random(Vector4 output) {
        if (output == null) {
            output = new Vector4();
        }

        output.x = EngineMath.RANDOM.nextFloat();
        output.y = EngineMath.RANDOM.nextFloat();
        output.z = EngineMath.RANDOM.nextFloat();
        output.w = EngineMath.RANDOM.nextFloat();

        return output;
    }

    /**
     * Sets this vector's first component.
     *
     * @return this vector
     */
    public Vector4 setX(float x) {
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
    public Vector4 setY(float y) {
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
    public Vector4 setZ(float z) {
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
     * Sets this vector's fourth component.
     *
     * @return this vector
     */
    public Vector4 setW(float w) {
        this.w = w;
        return this;
    }

    /**
     * Gives this vector's fourth component.
     *
     * @return third component
     */
    public float getW() {
        return w;
    }

    /**
     * Stores the first three components of this vector to the given 3-component output vector.
     *
     * @param output storage vector
     *
     * @return the output vector
     */
    public Vector3 toVector3(Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        return output.set(x, y, z);
    }

    /**
     * Sets this vector's components.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this vector
     */
    public Vector4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    /**
     * Sets this vector's x, y, and z coordinates from the given 3-component vector and the w component from the given scalar.
     *
     * @param xyz - first three components
     * @param w - last component
     *
     * @return this vector
     */
    public Vector4 set(Vector3 xyz, float w) {
        return set(xyz.getX(), xyz.getY(), xyz.getZ(), w);
    }

    /**
     * Sets this vector's attributes to the given template's.
     *
     * @param template - template to copy attributse from
     *
     * @return this vector
     */
    public Vector4 set(Vector4 template) {
        return set(template.x, template.y, template.z, template.w);
    }

    /**
     * Performs vector addition.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this vector
     */
    public Vector4 add(float x, float y, float z, float w) {
        return set(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    /**
     * Performs vector addition.
     *
     * @param input - vector to add
     *
     * @return this vector
     */
    public Vector4 add(Vector4 input) {
        return add(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector subtraction.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this vector
     */
    public Vector4 subtract(float x, float y, float z, float w) {
        return set(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    /**
     * Performs vector subtraction.
     *
     * @param input - vector to subtract
     *
     * @return this vector
     */
    public Vector4 subtract(Vector4 input) {
        return subtract(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector multiplication.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this vector
     */
    public Vector4 multiply(float scalar) {
        return set(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    /**
     * Performs vector multiplication.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     * @return this vector
     */
    public Vector4 multiply(float x, float y, float z, float w) {
        return set(this.x * x, this.y * y, this.z * z, this.w * w);
    }

    /**
     * Performs vector multiplication.
     *
     * @param input - vector to multiply by
     * @return this vector
     */
    public Vector4 multiply(Vector4 input) {
        return multiply(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector division.
     *
     * @param scalar - scalar value to divde by
     *
     * @return this vector
     */
    public Vector4 divide(float scalar) {
        float invScalar = 1f / scalar;
        return set(x * invScalar, y * invScalar, z * invScalar, w * invScalar);
    }

    /**
     * Performs vector division.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this vector
     */
    public Vector4 divide(float x, float y, float z, float w) {
        return set(this.x / x, this.y / y, this.z / z, this.w / w);
    }

    /**
     * Performs vector division.
     *
     * @param input - vector to divide by
     *
     * @return this vector
     */
    public Vector4 divide(Vector4 input) {
        return divide(input.x, input.y, input.z, input.w);
    }

    /**
     * Flips the sign of each component.
     *
     * @return this vector
     */
    public Vector4 negate() {
        return set(-x, -y, -z, -w);
    }

    /**
     * Sets all components positive.
     *
     * @return this vector
     */
    public Vector4 absolute() {
        return set(EngineMath.abs(x), EngineMath.abs(y), EngineMath.abs(z), EngineMath.abs(w));
    }

    /**
     * Calculates a unit vector.
     *
     * @return this vector
     */
    public Vector4 normalize() {
        float lengthSq = magnitudeSquared();

        if (EngineMath.abs(lengthSq) > EngineMath.EPSILON) {
            return multiply(EngineMath.invSqrt(lengthSq));
        }

        return this;
    }

    /**
     * Linearly interpolates between this vector and the given end vector by the given delta.
     *
     * @param end - the destination
     * @param delta - the step amount
     *
     * @return this vector
     */
    public Vector4 lerp(Vector4 end, float delta) {
        if (end.equals(this) || delta == 0f) {
            return this;
        }

        return set(EngineMath.lerp(this.x, end.x, delta),
                EngineMath.lerp(this.y, end.y, delta),
                EngineMath.lerp(this.z, end.z, delta),
                EngineMath.lerp(this.w, end.w, delta));
    }

    /**
     * Calculates this vector's squared length.
     *
     * @return vector's squared length
     */
    public float magnitudeSquared() {
        return x * x + y * y + z * z + w * w;
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
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return dot product
     */
    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    /**
     * Calculates the dot product between this vector and the given coordinates.
     *
     * @param input - vector to calculate dot product with
     *
     * @return dot product
     */
    public float dot(Vector4 input) {
        return dot(input.x, input.y, input.z, input.w);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + y + ", " + z + ", " + w + ")";
    }

    /**
     * Checks for field-for-field equality.
     *
     * @return true if this quaternion equals the given quaternion field-for-field
     */
    public boolean equals(Vector4 input) {
        return input.x == x && input.y == y && input.z == z && input.w == w;
    }
}
