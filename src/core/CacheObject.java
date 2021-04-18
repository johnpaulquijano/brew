package core;

import core.utility.Buffers;

import java.nio.FloatBuffer;

/**
 * Base class for objects which attributes are cached within the graphics context.
 *
 * @author John Paul Quijano
 */
public abstract class CacheObject {
    protected int index;
    protected int dataSize;
    protected FloatBuffer dataBuffer;

    /**
     * Creates a cache object with the given data numCached.
     *
     * @param dataSize - length of the data buffer
     */
    public CacheObject(int dataSize) {
        this.dataSize = dataSize;
        dataBuffer = Buffers.createFloatBuffer(dataSize);
        index = -1;
    }

    /**
     * Checks if this object is contained in a cache.
     *
     * @return true if this object is contained in a cache
     */
    public boolean isCached() {
        return index > -1;
    }

    /**
     * Gives the cache index. Initial value is -1 (not cached).
     *
     * @return cache index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gives the length of the data buffer.
     *
     * @return length of the data buffer
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * Gives the float buffer containing data.
     *
     * @return buffer containing data
     */
    public FloatBuffer getDataBuffer() {
        return dataBuffer;
    }
}
