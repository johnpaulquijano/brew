package core.math;

import core.utility.EngineException;
import core.utility.Poolable;
import core.utility.Pools;

/**
 * A 4-component vector representing rotation and orientation in 3D space.
 *
 * @author John Paul Quijano
 */
public class Quaternion implements Poolable {
    public final static Quaternion IDENTITY = new Quaternion(0f, 0f, 0f, 1f);

    private float x;
    private float y;
    private float z;
    private float w;

    /**
     * Creates an identity quaternion.
     */
    public Quaternion() {
        this(IDENTITY);
    }

    /**
     * Creates a quaternion based on the given template.
     *
     * @param template - the template to copy attributes from
     */
    public Quaternion(Quaternion template) {
        this(template.x, template.y, template.z, template.w);
    }

    /**
     * Creates a quaternion with the given parameters.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     */
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Sets this quaternion's first component.
     *
     * @param x - first component
     *
     * @return this quaternion
     */
    public Quaternion setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Gives this quaternion's first component.
     *
     * @return this quaternion's first component
     */
    public float getX() {
        return x;
    }

    /**
     * Sets this quaternion's second component.
     *
     * @param y - second component
     *
     * @return this quaternion
     */
    public Quaternion setY(float y) {
        this.y = y;
        return this;
    }

    /**
     * Gives this quaternion's second component.
     *
     * @return this quaternion's second component
     */
    public float getY() {
        return y;
    }

    /**
     * Sets this quaternion's third component.
     *
     * @param z - third component
     *
     * @return this quaternion
     */
    public Quaternion setZ(float z) {
        this.z = z;
        return this;
    }

    /**
     * Gives this quaternion's third component.
     *
     * @return this quaternion's third component
     */
    public float getZ() {
        return z;
    }

    /**
     * Sets this quaternion's fourth component.
     *
     * @param w - fourth component
     *
     * @return this quaternion
     */
    public Quaternion setW(float w) {
        this.w = w;
        return this;
    }

    /**
     * Gives this quaternion's fourth component.
     *
     * @return this quaternion's fourth component
     */
    public float getW() {
        return w;
    }

    /**
     * Sets this quaternion's coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this quaternion
     */
    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    /**
     * Sets this quaternion's attributes to the template's.
     *
     * @param template - the template to copy attributes from
     *
     * @return this quaternion
     */
    public Quaternion set(Quaternion template) {
        return set(template.x, template.y, template.z, template.w);
    }

    /**
     * Sets this quaternion's attributes from a 9-component square matrix.
     *
     * @return this quaternion
     */
    public Quaternion fromMatrix3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float _x, _y, _z, _w;
        float diaSum = m00 + m11 + m22;

        if (diaSum >= 0) {
            float s = EngineMath.sqrt(diaSum + 1f);
            float t = 0.5f / s;

            _w = s * 0.5f;
            _x = (m21 - m12) * t;
            _y = (m02 - m20) * t;
            _z = (m10 - m01) * t;
        } else if (m00 > m11 && m00 > m22) {
            float s = EngineMath.sqrt(1f + m00 - m11 - m22);
            float t = 0.5f / s;

            _x = s * 0.5f;
            _y = (m10 + m01) * t;
            _z = (m02 + m20) * t;
            _w = (m21 - m12) * t;
        } else if (m11 > m22) {
            float s = EngineMath.sqrt(1f + m11 - m00 - m22);
            float t = 0.5f / s;

            _y = s * 0.5f;
            _x = (m10 + m01) * t;
            _z = (m21 + m12) * t;
            _w = (m02 - m20) * t;
        } else {
            float s = EngineMath.sqrt(1f + m22 - m00 - m11);
            float t = 0.5f / s;

            _z = s * 0.5f;
            _x = (m02 + m20) * t;
            _y = (m21 + m12) * t;
            _w = (m10 - m01) * t;
        }

        return set(_x, _y, _z, _w);
    }

    /**
     * Sets this quaternion's attributes from a 9-component square matrix.
     *
     * @param matrix - the source 3 by 3 matrix
     *
     * @return this quaternion
     */
    public Quaternion fromMatrix3(Matrix3 matrix) {
        return fromMatrix3(matrix.get(0, 0), matrix.get(0, 1), matrix.get(0, 2), matrix.get(1, 0), matrix.get(1, 1), matrix.get(1, 2), matrix.get(2, 0), matrix.get(2, 1), matrix.get(2, 2));
    }

    /**
     * Translates this quaternion to a 9-component square matrix.
     *
     * @param output - storage matrix for the result
     *
     * @return the output matrix
     */
    public Matrix3 toMatrix3(Matrix3 output) {
        if (output == null) {
            output = new Matrix3();
        }

        float norm = norm();
        float s = (norm == 1f ? 2f : norm > 0f ? 2f / norm : 0);

        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        float xx = x * xs;
        float xy = x * ys;
        float xz = x * zs;
        float xw = w * xs;
        float yy = y * ys;
        float yz = y * zs;
        float yw = w * ys;
        float zz = z * zs;
        float zw = w * zs;

        output.set(0, 0, 1f - (yy + zz));
        output.set(0, 1, xy - zw);
        output.set(0, 2, xz + yw);
        output.set(1, 0, xy + zw);
        output.set(1, 1, 1f - (xx + zz));
        output.set(1, 2, yz - xw);
        output.set(2, 0, xz - yw);
        output.set(2, 1, yz + xw);
        output.set(2, 2, 1f - (xx + yy));

        return output;
    }

    /**
     * Translates this quaternion to a 16-component square matrix.
     *
     * @param output - storage matrix for the result
     *
     * @return the output matrix
     */
    public Matrix4 toMatrix4(Matrix4 output) {
        if (output == null) {
            output = new Matrix4();
        }

        Matrix3 matrix = toMatrix3(Pools.Matrix3.get());
        output.set(matrix);
        Pools.Matrix3.put(matrix);

        return output;
    }

    /**
     * Sets this quaternion from Euler angles.
     *
     * @param xAngle - rotation around the x-axis in radians
     * @param yAngle - rotation around the y-axis in radians
     * @param zAngle - rotation around the z-axis in radians
     *
     * @return this quaternion
     */
    public Quaternion fromAngles(float xAngle, float yAngle, float zAngle) {
        float halfAngle, sinX, sinY, cosX, cosY, sinZ, cosZ;

        halfAngle = xAngle * 0.5f;
        sinX = EngineMath.sin(halfAngle);
        cosX = EngineMath.cos(halfAngle);

        halfAngle = yAngle * 0.5f;
        sinY = EngineMath.sin(halfAngle);
        cosY = EngineMath.cos(halfAngle);

        halfAngle = zAngle * 0.5f;
        sinZ = EngineMath.sin(halfAngle);
        cosZ = EngineMath.cos(halfAngle);

        float cosYXcosZ = cosY * cosZ;
        float sinYXsinZ = sinY * sinZ;
        float cosYXsinZ = cosY * sinZ;
        float sinYXcosZ = sinY * cosZ;

        return set(cosYXcosZ * cosX - sinYXsinZ * sinX,
                cosYXcosZ * sinX + sinYXsinZ * cosX,
                sinYXcosZ * cosX + cosYXsinZ * sinX,
                cosYXsinZ * cosX - sinYXcosZ * sinX);
    }

    /**
     * Translates this quaternion to Euler angles.
     *
     * @param output - storage vector for the result
     *
     * @return the output vector
     */
    public Vector3 toAngles(Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        float wSquared = w * w;
        float xSquared = x * x;
        float ySquared = y * y;
        float zSquared = z * z;
        float test = x * y + z * w;
        float unit = xSquared + ySquared + zSquared + wSquared;

        if (test > 0.499 * unit) {
            output.setX(2 * EngineMath.atan2(x, w));
            output.setY(EngineMath.HALF_PI);
            output.setZ(0);
        } else if (test < -0.499 * unit) {
            output.setX(-2 * EngineMath.atan2(x, w));
            output.setY(-EngineMath.HALF_PI);
            output.setZ(0);
        } else {
            output.setX(EngineMath.atan2(2 * y * w - 2 * x * z, xSquared - ySquared - zSquared + wSquared));
            output.setY(EngineMath.asin(2 * test / unit));
            output.setZ(EngineMath.atan2(2 * x * w - 2 * y * z, -xSquared + ySquared - zSquared + wSquared));
        }

        return output;
    }

    /**
     * Axis-angle rotation representation. The rotation axis must be normalized.
     *
     * @param angle - amount of rotation in radians
     * @param axis - normalized axis of rotation
     *
     * @return this quaternion
     */
    public Quaternion fromAxisAngle(Vector3 axis, float angle) {
        if (axis.equals(Vector3.ZERO)) {
            return set(Quaternion.IDENTITY);
        }

        float halfAngle = angle * 0.5f;
        float sin = EngineMath.sin(halfAngle);

        return set(sin * axis.getX(), sin * axis.getY(), sin * axis.getZ(), EngineMath.cos(halfAngle));
    }

    /**
     * Translates this quaternion to an axis-angle representation.
     *
     * @param output - storage vector for the result
     *
     * @return 4-component vector with the first three components as the axis and the last component as the angle
     */
    public Vector4 toAxisAngle(Vector4 output) {
        if (output == null) {
            output = new Vector4();
        }

        float sqrLength = x * x + y * y + z * z;

        if (EngineMath.abs(sqrLength) <= EngineMath.EPSILON) {
            output.setX(1f);
            output.setY(0f);
            output.setZ(0f);
            output.setW(0f);
        } else {
            float invLength = (1f / EngineMath.sqrt(sqrLength));

            output.setX(x * invLength);
            output.setY(y * invLength);
            output.setZ(z * invLength);
            output.setW((2f * EngineMath.acos(w)));
        }

        return output;
    }

    /**
     * Performs vector addition.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this quaternion
     */
    public Quaternion add(float x, float y, float z, float w) {
        return set(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    /**
     * Performs vector addition.
     *
     * @param input - quaternion to add to this
     * @return this quaternion
     */
    public Quaternion add(Quaternion input) {
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
     * @return this quaternion
     */
    public Quaternion subtract(float x, float y, float z, float w) {
        return set(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    /**
     * Performs vector subtraction.
     *
     * @param input - quaternion to subtract from this
     *
     * @return this quaternion
     */
    public Quaternion subtract(Quaternion input) {
        return subtract(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector multiplication.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     * @return this quaternion
     */
    public Quaternion multiply(float x, float y, float z, float w) {
        float _x = this.x * w + this.y * z - this.z * y + this.w * x;
        float _y = -this.x * z + this.y * w + this.z * x + this.w * y;
        float _z = this.x * y - this.y * x + this.z * w + this.w * z;
        float _w = -this.x * x - this.y * y - this.z * z + this.w * w;

        return set(_x, _y, _z, _w);
    }

    /**
     * Performs vector multiplication.
     *
     * @param input - quaternion to multiply by
     *
     * @return this quaternion
     */
    public Quaternion multiply(Quaternion input) {
        return multiply(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector-scalar multiplication.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this quaternion
     */
    public Quaternion multiply(float scalar) {
        return set(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    /**
     * Performs vector division.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return this quaternion
     */
    public Quaternion divide(float x, float y, float z, float w) {
        Quaternion inverse = Pools.Quaternion.get().set(x, y, z, w).invert();
        multiply(inverse);
        Pools.Quaternion.put(inverse);

        return this;
    }

    /**
     * Performs vector division.
     *
     * @param input - quaternion to divide by
     * @return this quaternion
     */
    public Quaternion divide(Quaternion input) {
        return divide(input.x, input.y, input.z, input.w);
    }

    /**
     * Performs vector-scalar division.
     *
     * @param scalar - scalar value to divide by
     *
     * @return this quaternion
     */
    public Quaternion divide(float scalar) {
        float invScalar = 1f / scalar;
        return set(x * invScalar, y * invScalar, z * invScalar, w * invScalar);
    }

    /**
     * Calculates dot product of this quaternion and the given coordinates.
     *
     * @param x - first component
     * @param y - second component
     * @param z - third component
     * @param w - fourth component
     *
     * @return dot product
     *
     */
    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    /**
     * Calculates dot product of this quaternion and the given coordinates.
     *
     * @param input - quaternion to calculate dot product with
     *
     * @return dot product
     *
     */
    public float dot(Quaternion input) {
        return dot(input.x, input.y, input.z, input.w);
    }

    /**
     * Calculates the squared norm.
     *
     * @return squared norm (magnitude)
     */
    public float normSquared() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Calculates the norm.
     *
     * @return norm (magnitude)
     */
    public float norm() {
        return EngineMath.sqrt(normSquared());
    }

    /**
     * Normalizes this quaternion.
     *
     * @return this quaternion
     */
    public Quaternion normalize() {
        float norm = norm();

        if (EngineMath.abs(norm) <= EngineMath.EPSILON) {
            throw new EngineException("This quaternion cannot be normalized.");
        }

        float invNorm = 1f / norm;
        return set(x * invNorm, y * invNorm, z * invNorm, w * invNorm);
    }

    /**
     * Calculates the inverse.
     *
     * @return this quaternion
     */
    public Quaternion invert() {
        return normalize().conjugate();
    }

    /**
     * Calculates the conjugate.
     *
     * @return this quaternion
     */
    public Quaternion conjugate() {
        return set(-x, -y, -z, w);
    }

    /**
     * Spherical linear interpolation starting from this quaternion.
     *
     * @param end the destination
     * @param delta the step amount
     *
     * @return this quaternion
     */
    public Quaternion slerp(Quaternion end, float delta) {
        if (delta == 0f || equals(end)) {
            return this;
        } else if (delta == 1f) {
            return set(end);
        }

        float startDotEnd = dot(end);

        Quaternion e = Pools.Quaternion.get().set(end);

        if (startDotEnd < 0f) {
            e.multiply(-1f);
            startDotEnd = -startDotEnd;
        }

        float scale0 = 1f - delta;
        float scale1 = delta;

        if (1f - startDotEnd > 0.1f) {
            float theta = EngineMath.acos(startDotEnd);
            float invSinTheta = 1f / EngineMath.sin(theta);

            scale0 = EngineMath.sin((1f - delta) * theta) * invSinTheta;
            scale1 = EngineMath.sin((delta * theta)) * invSinTheta;
        }

        setX(scale0 * x + scale1 * e.x);
        setY(scale0 * y + scale1 * e.y);
        setZ(scale0 * z + scale1 * e.z);
        setW(scale0 * w + scale1 * e.w);

        Pools.Quaternion.put(e);

        return this;
    }

    /**
     * Normalized linear interpolation starting from this quaternion.
     *
     * @param end the destination
     * @param delta the step amount
     *
     * @return this quaternion
     */
    public Quaternion nlerp(Quaternion end, float delta) {
        if (delta == 0f || equals(end)) {
            return this;
        } else if (delta == 1f) {
            return set(end);
        }

        float dot = dot(end);
        float comp = 1f - delta;

        if (dot < 0f) {
            return setX(comp * x - delta * end.x)
                    .setY(comp * y - delta * end.y)
                    .setZ(comp * z - delta * end.z)
                    .setW(comp * w - delta * end.w)
                    .normalize();
        }

        return setX(comp * x + delta * end.x)
                .setY(comp * y + delta * end.y)
                .setZ(comp * z + delta * end.z)
                .setW(comp * w + delta * end.w)
                .normalize();
    }

    /**
     * Applies this quaternion to the input vector.
     *
     * @param input - vector to transform
     * @param output - storage vector for the result
     *
     * @return transformed vector
     */
    public Vector3 transform(Vector3 input, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        if (input.equals(Vector3.ZERO)) {
            output.set(Vector3.ZERO);
        } else {
            float vx = input.getX();
            float vy = input.getY();
            float vz = input.getZ();

            output.setX(w * w * vx + 2f * y * w * vz - 2f * z * w * vy + x * x * vx + 2f * y * x * vy + 2f * z * x * vz - z * z * vx - y * y * vx);
            output.setY(2f * x * y * vx + y * y * vy + 2f * z * y * vz + 2f * w * z * vx - z * z * vy + w * w * vy - 2f * x * w * vz - x * x * vy);
            output.setZ(2f * x * z * vx + 2f * y * z * vy + z * z * vz - 2f * w * y * vx - y * y * vz + 2f * w * x * vy - x * x * vz + w * w * vz);
        }

        return output;
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
    public boolean equals(Quaternion input) {
        return input.x == x && input.y == y && input.z == z && input.w == w;
    }
}
