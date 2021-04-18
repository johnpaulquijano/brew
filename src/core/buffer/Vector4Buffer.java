package core.buffer;

import core.math.Vector4;
import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Pools;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Convenience class for fast storage and retrieval of Vector4 objects.
 *
 * @author John Paul Quijano
 */
public final class Vector4Buffer {
    private int size;
    private FloatBuffer buffer;
    private FloatBuffer bufferImmutable;

    /**
     * @param size - maximum number of elements this buffer can hold
     */
    public Vector4Buffer(int size) {
        this.size = size;
        buffer = Buffers.createFloatBuffer(size * 4);
        bufferImmutable = buffer.asReadOnlyBuffer();
    }

    /**
     * Overwrites the element at the specified index with the given value.
     *
     * @param index - position of an element in the buffer
     * @param vector - element in the buffer
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(int index, Vector4 vector) {
        int start = index * 4;
        buffer.put(start, vector.getX()).put(start + 1, vector.getY()).put(start + 2, vector.getZ()).put(start + 3, vector.getW());
        return this;
    }

    /**
     * Overwrites the element at the specified index with the given value.
     *
     * @param index - position of an element in the buffer
     * @param x - x vector component
     * @param y - y vector component
     * @param z - z vector component
     * @param w - w vector component
     * @return this vector buffer
     */
    public Vector4Buffer set(int index, float x, float y, float z, float w) {
        int start = index * 4;
        buffer.put(start, x).put(start + 1, y).put(start + 2, z).put(start + 3, w);
        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(Vector4Buffer vecBuf) {
        Vector4 coord = Pools.Vector4.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(i, coord);
        }

        Pools.Vector4.put(coord);
        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer starting from the given index.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(int index, Vector4Buffer vecBuf) {
        Vector4 coord = Pools.Vector4.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(index + i, coord);
        }

        Pools.Vector4.put(coord);
        return this;
    }

    /**
     * Copies all the elements of the given list to this buffer.
     *
     * @param vecList - list to copy from
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(List<Vector4> vecList) {
        for (int i = 0; i < vecList.size(); i++) {
            set(i, vecList.get(i));
        }

        return this;
    }

    /**
     * Copies all the elements of the given list to this buffer starting from the given index.
     *
     * @param vecList - list to copy from
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(int index, List<Vector4> vecList) {
        for (int i = 0; i < vecList.size(); i++) {
            set(index + i, vecList.get(i));
        }

        return this;
    }

    /**
     * Copies all the elements of the given array to this buffer.
     *
     * @param vecArr - array to copy from
     * @return this vector buffer
     */
    public Vector4Buffer set(Vector4[] vecArr) {
        for (int i = 0; i < vecArr.length; i++) {
            set(i, vecArr[i]);
        }

        return this;
    }

    /**
     * Copies all the elements of the given array to this buffer starting from the given index.
     *
     * @param vecArr - array to copy from
     */
    public Vector4Buffer set(int index, Vector4[] vecArr) {
        for (int i = 0; i < vecArr.length; i++) {
            set(index + i, vecArr[i]);
        }

        return this;
    }

    /**
     * Sets a vector component at the specified index.
     *
     * @param elementIndex   - position of the element in the buffer
     * @param componentIndex - values between 0 and 3, inclusive, which corresponds to the x, y, z, and w vector components respectively
     *
     * @return this vector buffer
     */
    public Vector4Buffer set(int elementIndex, int componentIndex, float value) {
        if (componentIndex > 3) {
            throw new EngineException("Component index must be in the range of [0, 3].");
        }

        buffer.put(elementIndex * 4 + componentIndex, value);
        return this;
    }

    /**
     * Retrieves the element at the specified index.
     *
     * @param index - position of an element in the buffer
     * @param store - storage object
     *
     * @return element at the specified index
     */
    public Vector4 get(int index, Vector4 store) {
        Vector4 returnVec = store;

        if (returnVec == null) {
            returnVec = new Vector4();
        }

        return returnVec.set(buffer.get(index * 4), buffer.get(index * 4 + 1), buffer.get(index * 4 + 2), buffer.get(index * 4 + 3));
    }

    /**
     * Retrieves a vector component at the specified index.
     *
     * @param elementIndex   - position of the element in the buffer
     * @param componentIndex - values between 0 and 3 inclusive, which corresponds to the x, y, z, and w vector components respectively
     *
     * @return vector component at the specified index
     */
    public float get(int elementIndex, int componentIndex) {
        if (componentIndex > 3) {
            throw new EngineException("Component index must be in the range of [0, 3].");
        }

        return buffer.get(elementIndex * 4 + componentIndex);
    }

    public void clear() {
        buffer.clear();
    }

    /**
     * Returns the backing read-only buffer.
     *
     * @return backing read-only buffer
     */
    public FloatBuffer toFloatBuffer() {
        return bufferImmutable;
    }

    /**
     * Returns the maximum number of elements this buffer can hold.
     *
     * @return capacity of this buffer
     */
    public int size() {
        return size;
    }
}
