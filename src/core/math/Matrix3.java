package core.math;

import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Poolable;

import java.nio.FloatBuffer;

/**
 * A 9-component square matrix.
 *
 * @author John Paul Quijano
 */
public class Matrix3 implements Poolable {
    public static Matrix3 ZERO = new Matrix3(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
    public static Matrix3 IDENTITY = new Matrix3(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f);

    protected float[][] data;

    /**
     * Creates a 9-component square identity matrix.
     */
    public Matrix3() {
        this(IDENTITY);
    }

    /**
     * Creates a Creates a 9-component square matrix with the given initial values.
     */
    public Matrix3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        data = new float[3][3];

        data[0][0] = m00;
        data[0][1] = m01;
        data[0][2] = m02;
        data[1][0] = m10;
        data[1][1] = m11;
        data[1][2] = m12;
        data[2][0] = m20;
        data[2][1] = m21;
        data[2][2] = m22;
    }

    /**
     * Creates a 9-component square matrix from the given template.
     *
     * @param template - matrix to copy data from
     */
    public Matrix3(Matrix3 template) {
        data = new float[3][3];

        data[0][0] = template.data[0][0];
        data[0][1] = template.data[0][1];
        data[0][2] = template.data[0][2];
        data[1][0] = template.data[1][0];
        data[1][1] = template.data[1][1];
        data[1][2] = template.data[1][2];
        data[2][0] = template.data[2][0];
        data[2][1] = template.data[2][1];
        data[2][2] = template.data[2][2];
    }

    /**
     * Sets the value at the given row and column.
     *
     * @param row - row index
     * @param column - column index
     * @param value - value to set
     *
     * @return this matrix
     */
    public Matrix3 set(int row, int column, float value) {
        data[row][column] = value;
        return this;
    }

    /**
     * Gives the value at the given row and column.
     *
     * @param row - row index
     * @param column - column index
     *
     * @return value at the given row and column
     */
    public float get(int row, int column) {
        return data[row][column];
    }

    /**
     * Sets this matrix to the given values.
     *
     * @return this matrix
     */
    public Matrix3 set(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        data[0][0] = m00;
        data[0][1] = m01;
        data[0][2] = m02;
        data[1][0] = m10;
        data[1][1] = m11;
        data[1][2] = m12;
        data[2][0] = m20;
        data[2][1] = m21;
        data[2][2] = m22;

        return this;
    }

    /**
     * Sets this matrix to the given template.
     *
     * @param template - matrix to copy data from
     *
     * @return this matrix
     */
    public Matrix3 set(Matrix3 template) {
        data[0][0] = template.data[0][0];
        data[0][1] = template.data[0][1];
        data[0][2] = template.data[0][2];
        data[1][0] = template.data[1][0];
        data[1][1] = template.data[1][1];
        data[1][2] = template.data[1][2];
        data[2][0] = template.data[2][0];
        data[2][1] = template.data[2][1];
        data[2][2] = template.data[2][2];

        return this;
    }

    /**
     * Sets this matrix from the given quaternion.
     *
     * @param quat - source quaternion
     *
     * @return this matrix
     */
    public Matrix3 set(Quaternion quat) {
        if (quat == null) {
            return this;
        }

        return quat.toMatrix3(this);
    }

    /**
     * Sets this matrix from the given axes.
     *
     * @param u - first column
     * @param v - second column
     * @param w - third column
     *
     * @return this matrix
     */
    public Matrix3 fromAxes(Vector3 u, Vector3 v, Vector3 w) {
        data[0][0] = u.getX();
        data[1][0] = u.getY();
        data[2][0] = u.getZ();

        data[0][1] = v.getX();
        data[1][1] = v.getY();
        data[2][1] = v.getZ();

        data[0][2] = w.getX();
        data[1][2] = w.getY();
        data[2][2] = w.getZ();

        return this;
    }

    /**
     * Angle-axis rotation representation. The rotation axis must be normalized.
     *
     * @param angle - amount of rotation in radians
     * @param axis - normalized axis of rotation
     *
     * @return this matrix
     */
    public Matrix3 fromAngleAxis(float angle, Vector3 axis) {
        float cos = EngineMath.cos(angle);
        float sin = EngineMath.sin(angle);
        float cosComp = 1f - cos;
        float squaredX = axis.getX() * axis.getX();
        float squaredY = axis.getY() * axis.getY();
        float squaredZ = axis.getZ() * axis.getZ();
        float xyCosComp = axis.getX() * axis.getY() * cosComp;
        float xzCosComp = axis.getX() * axis.getZ() * cosComp;
        float yzCosComp = axis.getY() * axis.getZ() * cosComp;
        float xSin = axis.getX() * sin;
        float ySin = axis.getY() * sin;
        float zSin = axis.getZ() * sin;

        data[0][0] = squaredX * cosComp + cos;
        data[0][1] = xyCosComp - zSin;
        data[0][2] = xzCosComp + ySin;
        data[1][0] = xyCosComp + zSin;
        data[1][1] = squaredY * cosComp + cos;
        data[1][2] = yzCosComp - xSin;
        data[2][0] = xzCosComp - ySin;
        data[2][1] = yzCosComp + xSin;
        data[2][2] = squaredZ * cosComp + cos;

        return this;
    }

    /**
     * Calculates a rotation matrix from the given Euler angles.
     *
     * @param xAngle - rotation value around the x-axis
     * @param yAngle - rotation value around the y-axis
     * @param zAngle - rotation value around the z-axis
     *
     * @return this matrix
     */
    public Matrix3 fromAngles(float xAngle, float yAngle, float zAngle) {
        float cosX = EngineMath.cos(xAngle);
        float sinX = EngineMath.sin(xAngle);
        float cosY = EngineMath.cos(yAngle);
        float sinY = EngineMath.sin(yAngle);
        float cosZ = EngineMath.cos(zAngle);
        float sinZ = EngineMath.sin(zAngle);

        data[0][0] = cosZ * cosX;
        data[0][1] = sinZ * sinY - cosZ * sinX * cosY;
        data[0][2] = cosZ * sinX * sinY + sinZ * cosY;
        data[1][0] = sinX;
        data[1][1] = cosX * cosY;
        data[1][2] = -cosX * sinY;
        data[2][0] = -sinZ * cosX;
        data[2][1] = sinZ * sinX * cosY + cosZ * sinY;
        data[2][2] = -sinZ * sinX * sinY + cosZ * cosY;

        return this;
    }

    /**
     * Stores the column values at the given index to the given output vector.
     *
     * @param index - column index
     * @param output - storage vector
     *
     * @return the output vector
     */
    public Vector3 getColumn(int index, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        output.setX(data[0][index]);
        output.setY(data[1][index]);
        output.setZ(data[2][index]);

        return output;
    }

    /**
     * Stores the row values at the given index to the given output vector.
     *
     * @param index - row index
     * @param output - storage vector
     *
     * @return output vector
     */
    public Vector3 getRow(int index, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        output.setX(data[index][0]);
        output.setY(data[index][1]);
        output.setZ(data[index][2]);

        return output;
    }

    /**
     * Stores this matrix's values to the given output buffer.
     *
     * @param output - output float buffer
     *
     * @return output float buffer
     */
    public FloatBuffer toFloatBuffer(FloatBuffer output) {
        if (output == null) {
            output = Buffers.createFloatBuffer(9);
        }

        output.clear();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                output.put(data[i][j]);
            }
        }

        output.flip();

        return output;
    }

    /**
     * Multiplies the given input matrix by this matrix in that order, then stores the result to this matrix.
     *
     * @param input - matrix to multiply by
     *
     * @return this matrix
     */
    public Matrix3 multiply(Matrix3 input) {
        float m00 = data[0][0] * input.data[0][0] + data[0][1] * input.data[1][0] + data[0][2] * input.data[2][0];
        float m01 = data[0][0] * input.data[0][1] + data[0][1] * input.data[1][1] + data[0][2] * input.data[2][1];
        float m02 = data[0][0] * input.data[0][2] + data[0][1] * input.data[1][2] + data[0][2] * input.data[2][2];
        float m10 = data[1][0] * input.data[0][0] + data[1][1] * input.data[1][0] + data[1][2] * input.data[2][0];
        float m11 = data[1][0] * input.data[0][1] + data[1][1] * input.data[1][1] + data[1][2] * input.data[2][1];
        float m12 = data[1][0] * input.data[0][2] + data[1][1] * input.data[1][2] + data[1][2] * input.data[2][2];
        float m20 = data[2][0] * input.data[0][0] + data[2][1] * input.data[1][0] + data[2][2] * input.data[2][0];
        float m21 = data[2][0] * input.data[0][1] + data[2][1] * input.data[1][1] + data[2][2] * input.data[2][1];
        float m22 = data[2][0] * input.data[0][2] + data[2][1] * input.data[1][2] + data[2][2] * input.data[2][2];

        return set(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    /**
     * Multiplies all matrix values by the given scalar.
     *
     * @param scalar - scalar value to multiply by
     *
     * @return this matrix
     */
    public Matrix3 multiply(float scalar) {
        data[0][0] *= scalar;
        data[0][1] *= scalar;
        data[0][2] *= scalar;
        data[1][0] *= scalar;
        data[1][1] *= scalar;
        data[1][2] *= scalar;
        data[2][0] *= scalar;
        data[2][1] *= scalar;
        data[2][2] *= scalar;

        return this;
    }

    /**
     * Transforms the components of the input vector by this matrix and stores the result in the output vector.
     *
     * This method creates a storage vector if the output parameter is null.
     *
     * @param input - a point in space
     * @param output - storage for the result of transformation
     *
     * @return output vector
     */
    public Vector3 transform(Vector3 input, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        return output.setX(data[0][0] * input.getX() + data[1][0] * input.getY() + data[2][0] * input.getZ())
                .setY(data[0][1] * input.getX() + data[1][1] * input.getY() + data[2][1] * input.getZ())
                .setZ(data[0][2] * input.getX() + data[1][2] * input.getY() + data[2][2] * input.getZ());
    }

    /**
     * Applies a scale transform to this matrix.
     *
     * @param scale - vector multiplied to this matrix
     *
     * @return this matrix
     */
    public Matrix3 scale(Vector3 scale) {
        return Matrix3.this.set(data[0][0] * scale.getX(), data[0][1] * scale.getY(), data[0][2] * scale.getZ(),
                data[1][0] * scale.getX(), data[1][1] * scale.getY(), data[1][2] * scale.getZ(),
                data[2][0] * scale.getX(), data[2][1] * scale.getY(), data[2][2] * scale.getZ());
    }

    /**
     * Calculates the transpose of this matrix.
     *
     * @return this matrix
     */
    public Matrix3 transpose() {
        float m01 = data[0][1];
        float m02 = data[0][2];
        float m12 = data[1][2];

        data[0][1] = data[1][0];
        data[0][2] = data[2][0];
        data[1][2] = data[2][1];
        data[1][0] = m01;
        data[2][0] = m02;
        data[2][1] = m12;

        return this;
    }

    /**
     * Calculates the inverse of this matrix.
     *
     * @return this matrix
     */
    public Matrix3 invert() {
        float det = determinant();

        if (EngineMath.abs(det) <= EngineMath.EPSILON) {
            throw new EngineException("Matrix cannot be inverted.");
        }

        float temp00 = data[1][1] * data[2][2] - data[1][2] * data[2][1];
        float temp01 = data[0][2] * data[2][1] - data[0][1] * data[2][2];
        float temp02 = data[0][1] * data[1][2] - data[0][2] * data[1][1];
        float temp10 = data[1][2] * data[2][0] - data[1][0] * data[2][2];
        float temp11 = data[0][0] * data[2][2] - data[0][2] * data[2][0];
        float temp12 = data[0][2] * data[1][0] - data[0][0] * data[1][2];
        float temp20 = data[1][0] * data[2][1] - data[1][1] * data[2][0];
        float temp21 = data[0][1] * data[2][0] - data[0][0] * data[2][1];
        float temp22 = data[0][0] * data[1][1] - data[0][1] * data[1][0];

        return set(temp00, temp01, temp02, temp10, temp11, temp12, temp20, temp21, temp22).multiply(1f / det);
    }

    /**
     * Calculates the determinant of this matrix.
     */
    public float determinant() {
        float fCo00 = data[1][1] * data[2][2] - data[1][2] * data[2][1];
        float fCo10 = data[1][2] * data[2][0] - data[1][0] * data[2][2];
        float fCo20 = data[1][0] * data[2][1] - data[1][1] * data[2][0];

        return data[0][0] * fCo00 + data[0][1] * fCo10 + data[0][2] * fCo20;
    }

    /**
     * Ensures that each component is positive.
     *
     * @return this matrix
     */
    public Matrix3 absolute() {
        data[0][0] = EngineMath.abs(data[0][0]);
        data[0][1] = EngineMath.abs(data[0][1]);
        data[0][2] = EngineMath.abs(data[0][2]);
        data[1][0] = EngineMath.abs(data[1][0]);
        data[1][1] = EngineMath.abs(data[1][1]);
        data[1][2] = EngineMath.abs(data[1][2]);
        data[2][0] = EngineMath.abs(data[2][0]);
        data[2][1] = EngineMath.abs(data[2][1]);
        data[2][2] = EngineMath.abs(data[2][2]);

        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName() + " [\n");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.append(" ");
                result.append(data[i][j]);
            }
            result.append(" \n");
        }

        result.append("]");

        return result.toString();
    }

    /**
     * Checks for field-for-field equality.
     *
     * @param matrix - matrix to text equality with
     *
     * @return true if this matrix equals the given matrix field-for-field
     */
    public boolean equals(Matrix3 matrix) {
        return data[0][0] == matrix.data[0][0]
                && data[0][1] == matrix.data[0][1]
                && data[0][2] == matrix.data[0][2]
                && data[1][0] == matrix.data[1][0]
                && data[1][1] == matrix.data[1][1]
                && data[1][2] == matrix.data[1][2]
                && data[2][0] == matrix.data[2][0]
                && data[2][1] == matrix.data[2][1]
                && data[2][2] == matrix.data[2][2];
    }
}
