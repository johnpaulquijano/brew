package core.buffer;

import core.math.Vector3;
import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Pools;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Convenience class for fast storage and retrieval of Vector3 objects.
 *
 * @author John Paul Quijano
 */
public final class Vector3Buffer {
    private int size;
    private FloatBuffer buffer;
    private FloatBuffer bufferImmutable;

    /**
     * @param size - maximum number of elements this buffer can hold
     */
    public Vector3Buffer(int size) {
        this.size = size;
        buffer = Buffers.createFloatBuffer(size * 3);
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
    public Vector3Buffer set(int index, Vector3 vector) {
        int start = index * 3;
        buffer.put(start, vector.getX()).put(start + 1, vector.getY()).put(start + 2, vector.getZ());
        return this;
    }

    /**
     * Overwrites the element at the specified index with the given value.
     *
     * @param index - position of an element in the buffer
     * @param x - x vector component
     * @param y - y vector component
     * @param z - z vector component
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(int index, float x, float y, float z) {
        int start = index * 3;
        buffer.put(start, x).put(start + 1, y).put(start + 2, z);
        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(Vector3Buffer vecBuf) {
        Vector3 coord = Pools.Vector3.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(i, coord);
        }

        Pools.Vector3.put(coord);

        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer starting from the given index.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(int index, Vector3Buffer vecBuf) {
        Vector3 coord = Pools.Vector3.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(index + i, coord);
        }

        Pools.Vector3.put(coord);

        return this;
    }

    /**
     * Copies all the elements of the given list to this buffer.
     *
     * @param vecList - list to copy from
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(List<Vector3> vecList) {
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
    public Vector3Buffer set(int index, List<Vector3> vecList) {
        for (int i = 0; i < vecList.size(); i++) {
            set(index + i, vecList.get(i));
        }

        return this;
    }

    /**
     * Copies all the elements of the given array to this buffer.
     *
     * @param vecArr - array to copy from
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(Vector3[] vecArr) {
        for (int i = 0; i < vecArr.length; i++) {
            set(i, vecArr[i]);
        }

        return this;
    }

    /**
     * Copies all the elements of the given array to this buffer starting from the given index.
     *
     * @param vecArr - array to copy from
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(int index, Vector3[] vecArr) {
        for (int i = 0; i < vecArr.length; i++) {
            set(index + i, vecArr[i]);
        }

        return this;
    }

    /**
     * Sets a vector component at the specified index.
     *
     * @param elementIndex - position of the element in the buffer
     * @param componentIndex - values between 0 and 2 inclusive, which corresponds to the x, y, and z vector components respectively
     *
     * @return this vector buffer
     */
    public Vector3Buffer set(int elementIndex, int componentIndex, float value) {
        if (componentIndex > 2) {
            throw new EngineException("Component index must be in the range of [0, 2].");
        }

        buffer.put(elementIndex * 3 + componentIndex, value);
        return this;
    }

    /**
     * Retrieves the element at the specified index.
     *
     * @param index - position of an element in the buffer
     * @param store - storage object
     */
    public Vector3 get(int index, Vector3 store) {
        Vector3 returnVec = store;

        if (returnVec == null) {
            returnVec = new Vector3();
        }

        return returnVec.set(buffer.get(index * 3), buffer.get(index * 3 + 1), buffer.get(index * 3 + 2));
    }

    /**
     * Retrieves a vector component at the specified index.
     *
     * @param elementIndex   - position of the element in the buffer
     * @param componentIndex - values between 0 and 2 inclusive, which corresponds to the x, y, and z vector components respectively
     */
    public float get(int elementIndex, int componentIndex) {
        if (componentIndex > 2) {
            throw new EngineException("Component index must be in the range of [0, 2].");
        }

        return buffer.get(elementIndex * 3 + componentIndex);
    }

    /**
     * Empties this buffer.
     */
    public void clear() {
        buffer.clear();
    }

    /**
     * Returns the backing read-only buffer.
     */
    public FloatBuffer toFloatBuffer() {
        return bufferImmutable;
    }

    /**
     * Returns the maximum number of elements this buffer can hold.
     */
    public int size() {
        return size;
    }
}
