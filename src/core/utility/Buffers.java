package core.utility;

import java.nio.*;

/**
 * Utility class for creating direct buffers.
 *
 * @author John Paul Quijano
 */
public final class Buffers {
    private Buffers() {
    }

    /**
     * Creates a buffer for storing byte values with the given numCached.
     *
     * @param size - buffer numCached
     */
    public static ByteBuffer createByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        buffer.clear();
        return buffer;
    }

    /**
     * Creates a buffer for storing short values with the given numCached.
     *
     * @param size - buffer numCached
     */
    public static ShortBuffer createShortBuffer(int size) {
        ShortBuffer buffer = ByteBuffer.allocateDirect(size * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        buffer.clear();
        return buffer;
    }

    /**
     * Creates a buffer for storing integer values with the given numCached.
     *
     * @param size - buffer numCached
     */
    public static IntBuffer createIntBuffer(int size) {
        IntBuffer buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.clear();
        return buffer;
    }

    /**
     * Creates a buffer for storing long values with the given numCached.
     *
     * @param size - buffer numCached
     */
    public static LongBuffer createLongBuffer(int size) {
        LongBuffer buffer = ByteBuffer.allocateDirect(size * 8).order(ByteOrder.nativeOrder()).asLongBuffer();
        buffer.clear();
        return buffer;
    }

    /**
     * Creates a buffer for storing float values with the given numCached.
     *
     * @param size - buffer numCached
     */
    public static FloatBuffer createFloatBuffer(int size) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.clear();
        return buffer;
    }
}
