package core.buffer;

import core.math.Vector2;
import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.Pools;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Convenience class for fast storage and retrieval of Vector2 objects.
 *
 * @author John Paul Quijano
 */
public final class Vector2Buffer {
    private int size;
    private FloatBuffer buffer;
    private FloatBuffer bufferImmutable;

    /**
     * @param size - maximum number of vectors this buffer can hold
     */
    public Vector2Buffer(int size) {
        this.size = size;
        buffer = Buffers.createFloatBuffer(size * 2);
        bufferImmutable = buffer.asReadOnlyBuffer();
    }

    /**
     * Overwrites the element at the specified index with the given value.
     *
     * @param index - position of an element in the buffer
     * @param vector - an element in the buffer
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(int index, Vector2 vector) {
        int start = index * 2;
        buffer.put(start, vector.getX()).put(start + 1, vector.getY());
        return this;
    }

    /**
     * Overwrites the element at the specified index with the given value.
     *
     * @param index - position of an element in the buffer
     * @param x - x vector component
     * @param y - y vector component
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(int index, float x, float y) {
        int start = index * 2;
        buffer.put(start, x).put(start + 1, y);
        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(Vector2Buffer vecBuf) {
        Vector2 coord = Pools.Vector2.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(i, coord);
        }

        Pools.Vector2.put(coord);

        return this;
    }

    /**
     * Copies all the elements of the given buffer to this buffer starting from the given index.
     *
     * @param vecBuf - buffer to copy from
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(int index, Vector2Buffer vecBuf) {
        Vector2 coord = Pools.Vector2.get();

        for (int i = 0; i < vecBuf.size(); i++) {
            vecBuf.get(i, coord);
            set(index + i, coord);
        }

        Pools.Vector2.put(coord);

        return this;
    }

    /**
     * Copies all the elements of the given list to this buffer.
     *
     * @param vecList - list to copy from
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(List<Vector2> vecList) {
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
    public Vector2Buffer set(int index, List<Vector2> vecList) {
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
    public Vector2Buffer set(Vector2[] vecArr) {
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
    public Vector2Buffer set(int index, Vector2[] vecArr) {
        for (int i = 0; i < vecArr.length; i++) {
            set(index + i, vecArr[i]);
        }

        return this;
    }

    /**
     * Sets a vector component at the specified index.
     *
     * @param elementIndex   - position of the element in the buffer
     * @param componentIndex - values between 0 and 1 inclusive, which corresponds to the x and y vector components respectively
     *
     * @return this vector buffer
     */
    public Vector2Buffer set(int elementIndex, int componentIndex, float value) {
        if (componentIndex > 1) {
            throw new EngineException("Component index must be in the range of [0, 1].");
        }

        buffer.put(elementIndex * 2 + componentIndex, value);

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
    public Vector2 get(int index, Vector2 store) {
        Vector2 returnVec = store;

        if (returnVec == null) {
            returnVec = new Vector2();
        }

        return returnVec.set(buffer.get(index * 2), buffer.get(index * 2 + 1));
    }

    /**
     * Retrieves a vector component at the specified index.
     *
     * @param elementIndex   - position of the element in the buffer
     * @param componentIndex - values between 0 and 1 inclusive, which corresponds to the x and y vector components respectively
     *
     * @return vector component at the specified index
     */
    public float get(int elementIndex, int componentIndex) {
        if (componentIndex > 1) {
            throw new EngineException("Component index must be in the range of [0, 1].");
        }

        return buffer.get(elementIndex * 2 + componentIndex);
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
