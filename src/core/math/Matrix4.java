package core.math;

import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Poolable;

import java.nio.FloatBuffer;

/**
 * A 16-component square matrix.
 *
 * @author John Paul Quijano
 */
public class Matrix4 implements Poolable {
    public final static Matrix4 ZERO = new Matrix4(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
    public final static Matrix4 IDENTITY = new Matrix4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f);

    private float[][] data;

    /**
     * Creates a 16-component square identity matrix.
     */
    public Matrix4() {
        this(IDENTITY);
    }

    /**
     * Creates a Creates a 16-component square matrix with the given initial values.
     */
    public Matrix4(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
        data = new float[4][4];

        data[0][0] = m00;
        data[0][1] = m01;
        data[0][2] = m02;
        data[0][3] = m03;
        data[1][0] = m10;
        data[1][1] = m11;
        data[1][2] = m12;
        data[1][3] = m13;
        data[2][0] = m20;
        data[2][1] = m21;
        data[2][2] = m22;
        data[2][3] = m23;
        data[3][0] = m30;
        data[3][1] = m31;
        data[3][2] = m32;
        data[3][3] = m33;
    }

    /**
     * Creates a 16-component square matrix from the given template.
     *
     * @param template - matrix to copy data from
     */
    public Matrix4(Matrix4 template) {
        data = new float[4][4];

        data[0][0] = template.data[0][0];
        data[0][1] = template.data[0][1];
        data[0][2] = template.data[0][2];
        data[0][3] = template.data[0][3];
        data[1][0] = template.data[1][0];
        data[1][1] = template.data[1][1];
        data[1][2] = template.data[1][2];
        data[1][3] = template.data[1][3];
        data[2][0] = template.data[2][0];
        data[2][1] = template.data[2][1];
        data[2][2] = template.data[2][2];
        data[2][3] = template.data[2][3];
        data[3][0] = template.data[3][0];
        data[3][1] = template.data[3][1];
        data[3][2] = template.data[3][2];
        data[3][3] = template.data[3][3];
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
    public Matrix4 set(int row, int column, float value) {
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
    public Matrix4 set(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
        data[0][0] = m00;
        data[0][1] = m01;
        data[0][2] = m02;
        data[0][3] = m03;
        data[1][0] = m10;
        data[1][1] = m11;
        data[1][2] = m12;
        data[1][3] = m13;
        data[2][0] = m20;
        data[2][1] = m21;
        data[2][2] = m22;
        data[2][3] = m23;
        data[3][0] = m30;
        data[3][1] = m31;
        data[3][2] = m32;
        data[3][3] = m33;

        return this;
    }

    /**
     * Sets this matrix to the given template.
     *
     * @param template - matrix to copy data from
     *
     * @return this matrix
     */
    public Matrix4 set(Matrix4 template) {
        data[0][0] = template.data[0][0];
        data[0][1] = template.data[0][1];
        data[0][2] = template.data[0][2];
        data[0][3] = template.data[0][3];
        data[1][0] = template.data[1][0];
        data[1][1] = template.data[1][1];
        data[1][2] = template.data[1][2];
        data[1][3] = template.data[1][3];
        data[2][0] = template.data[2][0];
        data[2][1] = template.data[2][1];
        data[2][2] = template.data[2][2];
        data[2][3] = template.data[2][3];
        data[3][0] = template.data[3][0];
        data[3][1] = template.data[3][1];
        data[3][2] = template.data[3][2];
        data[3][3] = template.data[3][3];

        return this;
    }

    /**
     * Sets this matrix's upper-left 3x3 matrix to the given values.
     *
     * @return this matrix
     */
    public Matrix4 set(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
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
     * Sets this matrix's upper-left 3x3 matrix to the given template.
     *
     * @return this matrix
     */
    public Matrix4 set(Matrix3 source) {
        data[0][0] = source.data[0][0];
        data[0][1] = source.data[0][1];
        data[0][2] = source.data[0][2];
        data[1][0] = source.data[1][0];
        data[1][1] = source.data[1][1];
        data[1][2] = source.data[1][2];
        data[2][0] = source.data[2][0];
        data[2][1] = source.data[2][1];
        data[2][2] = source.data[2][2];

        return this;
    }

    /**
     * Sets this matrix's upper-left 3x3 matrix to the given quaternion.
     *
     * @param quat - source quaternion
     * @return this matrix
     */
    public Matrix4 set(Quaternion quat) {
        return quat.toMatrix4(this);
    }

    /**
     * Gives this matrix's upper-left 3x3 matrix.
     *
     * @param output - storage matrix
     * @return the output matrix
     */
    public Matrix3 toMatrix3(Matrix3 output) {
        if (output == null) {
            output = new Matrix3();
        }

        output.data[0][0] = data[0][0];
        output.data[0][1] = data[0][1];
        output.data[0][2] = data[0][2];
        output.data[1][0] = data[1][0];
        output.data[1][1] = data[1][1];
        output.data[1][2] = data[1][2];
        output.data[2][0] = data[2][0];
        output.data[2][1] = data[2][1];
        output.data[2][2] = data[2][2];

        return output;
    }

    /**
     * Stores this matrix's values to the given output buffer.
     *
     * @param output - output float buffer
     * @return the output float buffer
     */
    public FloatBuffer toFloatBuffer(FloatBuffer output) {
        if (output == null) {
            output = Buffers.createFloatBuffer(16);
        }

        output.clear();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                output.put(data[i][j]);
            }
        }

        output.flip();

        return output;
    }

    /**
     * Multiplies the given input matrix by this matrix in that order, then stores the result to this matrix.
     *
     * @param matrix - the matrix to multiply by
     * @return this matrix
     */
    public Matrix4 multiply(Matrix4 matrix) {
        float m00 = data[0][0] * matrix.data[0][0]
                + data[0][1] * matrix.data[1][0]
                + data[0][2] * matrix.data[2][0]
                + data[0][3] * matrix.data[3][0];

        float m01 = data[0][0] * matrix.data[0][1]
                + data[0][1] * matrix.data[1][1]
                + data[0][2] * matrix.data[2][1]
                + data[0][3] * matrix.data[3][1];

        float m02 = data[0][0] * matrix.data[0][2]
                + data[0][1] * matrix.data[1][2]
                + data[0][2] * matrix.data[2][2]
                + data[0][3] * matrix.data[3][2];

        float m03 = data[0][0] * matrix.data[0][3]
                + data[0][1] * matrix.data[1][3]
                + data[0][2] * matrix.data[2][3]
                + data[0][3] * matrix.data[3][3];

        float m10 = data[1][0] * matrix.data[0][0]
                + data[1][1] * matrix.data[1][0]
                + data[1][2] * matrix.data[2][0]
                + data[1][3] * matrix.data[3][0];

        float m11 = data[1][0] * matrix.data[0][1]
                + data[1][1] * matrix.data[1][1]
                + data[1][2] * matrix.data[2][1]
                + data[1][3] * matrix.data[3][1];

        float m12 = data[1][0] * matrix.data[0][2]
                + data[1][1] * matrix.data[1][2]
                + data[1][2] * matrix.data[2][2]
                + data[1][3] * matrix.data[3][2];

        float m13 = data[1][0] * matrix.data[0][3]
                + data[1][1] * matrix.data[1][3]
                + data[1][2] * matrix.data[2][3]
                + data[1][3] * matrix.data[3][3];

        float m20 = data[2][0] * matrix.data[0][0]
                + data[2][1] * matrix.data[1][0]
                + data[2][2] * matrix.data[2][0]
                + data[2][3] * matrix.data[3][0];

        float m21 = data[2][0] * matrix.data[0][1]
                + data[2][1] * matrix.data[1][1]
                + data[2][2] * matrix.data[2][1]
                + data[2][3] * matrix.data[3][1];

        float m22 = data[2][0] * matrix.data[0][2]
                + data[2][1] * matrix.data[1][2]
                + data[2][2] * matrix.data[2][2]
                + data[2][3] * matrix.data[3][2];

        float m23 = data[2][0] * matrix.data[0][3]
                + data[2][1] * matrix.data[1][3]
                + data[2][2] * matrix.data[2][3]
                + data[2][3] * matrix.data[3][3];

        float m30 = data[3][0] * matrix.data[0][0]
                + data[3][1] * matrix.data[1][0]
                + data[3][2] * matrix.data[2][0]
                + data[3][3] * matrix.data[3][0];

        float m31 = data[3][0] * matrix.data[0][1]
                + data[3][1] * matrix.data[1][1]
                + data[3][2] * matrix.data[2][1]
                + data[3][3] * matrix.data[3][1];

        float m32 = data[3][0] * matrix.data[0][2]
                + data[3][1] * matrix.data[1][2]
                + data[3][2] * matrix.data[2][2]
                + data[3][3] * matrix.data[3][2];

        float m33 = data[3][0] * matrix.data[0][3]
                + data[3][1] * matrix.data[1][3]
                + data[3][2] * matrix.data[2][3]
                + data[3][3] * matrix.data[3][3];

        return Matrix4.this.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /**
     * Multiplies all matrix values by the given scalar.
     *
     * @param scalar - scalar value to multiply by
     * @return this matrix
     */
    public Matrix4 multiply(float scalar) {
        data[0][0] *= scalar;
        data[0][1] *= scalar;
        data[0][2] *= scalar;
        data[0][3] *= scalar;
        data[1][0] *= scalar;
        data[1][1] *= scalar;
        data[1][2] *= scalar;
        data[1][3] *= scalar;
        data[2][0] *= scalar;
        data[2][1] *= scalar;
        data[2][2] *= scalar;
        data[2][3] *= scalar;
        data[3][0] *= scalar;
        data[3][1] *= scalar;
        data[3][2] *= scalar;
        data[3][3] *= scalar;

        return this;
    }

    /**
     * Calculates the transpose of this matrix.
     *
     * @return this matrix
     */
    public Matrix4 transpose() {
        float m01 = data[0][1];
        float m02 = data[0][2];
        float m03 = data[0][3];
        float m12 = data[1][2];
        float m13 = data[1][3];
        float m23 = data[2][3];

        data[0][1] = data[1][0];
        data[0][2] = data[2][0];
        data[0][3] = data[3][0];
        data[1][2] = data[2][1];
        data[1][3] = data[3][1];
        data[2][3] = data[3][2];

        data[1][0] = m01;
        data[2][0] = m02;
        data[3][0] = m03;
        data[2][1] = m12;
        data[3][1] = m13;
        data[3][2] = m23;

        return this;
    }

    /**
     * Calculates the inverse of this matrix.
     *
     * @return this matrix
     */
    public Matrix4 invert() {
        float a0 = data[0][0] * data[1][1] - data[0][1] * data[1][0];
        float a1 = data[0][0] * data[1][2] - data[0][2] * data[1][0];
        float a2 = data[0][0] * data[1][3] - data[0][3] * data[1][0];
        float a3 = data[0][1] * data[1][2] - data[0][2] * data[1][1];
        float a4 = data[0][1] * data[1][3] - data[0][3] * data[1][1];
        float a5 = data[0][2] * data[1][3] - data[0][3] * data[1][2];
        float b0 = data[2][0] * data[3][1] - data[2][1] * data[3][0];
        float b1 = data[2][0] * data[3][2] - data[2][2] * data[3][0];
        float b2 = data[2][0] * data[3][3] - data[2][3] * data[3][0];
        float b3 = data[2][1] * data[3][2] - data[2][2] * data[3][1];
        float b4 = data[2][1] * data[3][3] - data[2][3] * data[3][1];
        float b5 = data[2][2] * data[3][3] - data[2][3] * data[3][2];

        float det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;

        if (EngineMath.abs(det) <= EngineMath.EPSILON) {
            throw new EngineException("Matrix cannot be inverted.");
        }

        float m00 = +data[1][1] * b5 - data[1][2] * b4 + data[1][3] * b3;
        float m10 = -data[1][0] * b5 + data[1][2] * b2 - data[1][3] * b1;
        float m20 = +data[1][0] * b4 - data[1][1] * b2 + data[1][3] * b0;
        float m30 = -data[1][0] * b3 + data[1][1] * b1 - data[1][2] * b0;
        float m01 = -data[0][1] * b5 + data[0][2] * b4 - data[0][3] * b3;
        float m11 = +data[0][0] * b5 - data[0][2] * b2 + data[0][3] * b1;
        float m21 = -data[0][0] * b4 + data[0][1] * b2 - data[0][3] * b0;
        float m31 = +data[0][0] * b3 - data[0][1] * b1 + data[0][2] * b0;
        float m02 = +data[3][1] * a5 - data[3][2] * a4 + data[3][3] * a3;
        float m12 = -data[3][0] * a5 + data[3][2] * a2 - data[3][3] * a1;
        float m22 = +data[3][0] * a4 - data[3][1] * a2 + data[3][3] * a0;
        float m32 = -data[3][0] * a3 + data[3][1] * a1 - data[3][2] * a0;
        float m03 = -data[2][1] * a5 + data[2][2] * a4 - data[2][3] * a3;
        float m13 = +data[2][0] * a5 - data[2][2] * a2 + data[2][3] * a1;
        float m23 = -data[2][0] * a4 + data[2][1] * a2 - data[2][3] * a0;
        float m33 = +data[2][0] * a3 - data[2][1] * a1 + data[2][2] * a0;

        return set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33).multiply(1f / det);
    }

    /**
     * Calculates the determinant of this matrix.
     *
     * @return this matrix
     */
    public float determinant() {
        float a0 = data[0][0] * data[1][1] - data[0][1] * data[1][0];
        float a1 = data[0][0] * data[1][2] - data[0][2] * data[1][0];
        float a2 = data[0][0] * data[1][3] - data[0][3] * data[1][0];
        float a3 = data[0][1] * data[1][2] - data[0][2] * data[1][1];
        float a4 = data[0][1] * data[1][3] - data[0][3] * data[1][1];
        float a5 = data[0][2] * data[1][3] - data[0][3] * data[1][2];
        float b0 = data[2][0] * data[3][1] - data[2][1] * data[3][0];
        float b1 = data[2][0] * data[3][2] - data[2][2] * data[3][0];
        float b2 = data[2][0] * data[3][3] - data[2][3] * data[3][0];
        float b3 = data[2][1] * data[3][2] - data[2][2] * data[3][1];
        float b4 = data[2][1] * data[3][3] - data[2][3] * data[3][1];
        float b5 = data[2][2] * data[3][3] - data[2][3] * data[3][2];

        return a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;
    }

    /**
     * Ensures that each component is positive.
     *
     * @return this matrix
     */
    public Matrix4 absolute() {
        data[0][0] = EngineMath.abs(data[0][0]);
        data[0][1] = EngineMath.abs(data[0][1]);
        data[0][2] = EngineMath.abs(data[0][2]);
        data[0][3] = EngineMath.abs(data[0][3]);
        data[1][0] = EngineMath.abs(data[1][0]);
        data[1][1] = EngineMath.abs(data[1][1]);
        data[1][2] = EngineMath.abs(data[1][2]);
        data[1][3] = EngineMath.abs(data[1][3]);
        data[2][0] = EngineMath.abs(data[2][0]);
        data[2][1] = EngineMath.abs(data[2][1]);
        data[2][2] = EngineMath.abs(data[2][2]);
        data[2][3] = EngineMath.abs(data[2][3]);
        data[3][0] = EngineMath.abs(data[3][0]);
        data[3][1] = EngineMath.abs(data[3][1]);
        data[3][2] = EngineMath.abs(data[3][2]);
        data[3][3] = EngineMath.abs(data[3][3]);

        return this;
    }

    /**
     * Transforms the components of the input vector by this matrix and stores
     * the result in the output vector.
     * <p>
     * This method creates a storage vector if the output parameter is null.
     *
     * @param input  a point in space
     * @param output storage for the result of transformation
     * @return the output vector
     */
    public Vector4 transform(Vector4 input, Vector4 output) {
        if (output == null) {
            output = new Vector4();
        }

        float x = input.getX();
        float y = input.getY();
        float z = input.getZ();
        float w = input.getW();

        output.setX(data[0][0] * x + data[1][0] * y + data[2][0] * z + data[3][0] * w);
        output.setY(data[0][1] * x + data[1][1] * y + data[2][1] * z + data[3][1] * w);
        output.setZ(data[0][2] * x + data[1][2] * y + data[2][2] * z + data[3][2] * w);
        output.setW(data[0][3] * x + data[1][3] * y + data[2][3] * z + data[3][3] * w);

        return output;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName() + " [\n");

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
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
     * @param matrix - the matrix to test equality with
     * @return true if this matrix equals the given matrix field-for-field
     */
    public boolean equals(Matrix4 matrix) {
        return data[0][0] == matrix.data[0][0]
                && data[0][1] == matrix.data[0][1]
                && data[0][2] == matrix.data[0][2]
                && data[0][3] == matrix.data[0][3]
                && data[1][0] == matrix.data[1][0]
                && data[1][1] == matrix.data[1][1]
                && data[1][2] == matrix.data[1][2]
                && data[1][3] == matrix.data[1][3]
                && data[2][0] == matrix.data[2][0]
                && data[2][1] == matrix.data[2][1]
                && data[2][2] == matrix.data[2][2]
                && data[2][3] == matrix.data[2][3]
                && data[3][0] == matrix.data[3][0]
                && data[3][1] == matrix.data[3][1]
                && data[3][2] == matrix.data[3][2]
                && data[3][3] == matrix.data[3][3];
    }
}
