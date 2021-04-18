package core.utility;

import java.util.ArrayDeque;

/**
 * A simple pooling system. The objects in this pool will be visible only to the creating thread.
 *
 * @author John Paul Quijano
 */
public class Pool<T extends Poolable> {
    private int size;
    private int free;
    private boolean flex;
    private Class<T> poolable;
    private ThreadLocal<ArrayDeque<T>> pool;

    /**
     * Creates a pool of the given type of poolable objects.
     *
     * @param poolable - poolable class
     * @param size - numCached of the pool
     * @param flex - if true, pool is replenished when it runs out of objects
     */
    public Pool(Class<T> poolable, int size, boolean flex) {
        this.size = size;
        this.flex = flex;
        this.poolable = poolable;

        free = size;

        pool = new ThreadLocal<ArrayDeque<T>>() {
            @Override
            protected ArrayDeque<T> initialValue() {
                ArrayDeque<T> objects = new ArrayDeque<>(size);

                for (int i = 0; i < size; i++) {
                    try {
                        objects.push(poolable.newInstance());
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw new EngineException("Failed to create pool.");
                    }
                }

                return objects;
            }
        };
    }

    /**
     * Gives the numCached of this pool.
     *
     * @return numCached of this pool
     */
    public int getSize() {
        return size;
    }

    /**
     * Gives the remaining retrievable objects from this pool.
     *
     * @return remaining retrievable objects
     */
    public int getFree() {
        return free;
    }

    /**
     * Retrieves an object from this pool.
     *
     * @return object from this pool
     */
    public T get() {
        ArrayDeque<T> objects = pool.get();

        if (objects.isEmpty()) {
            if (flex) {
                int incSize = size / 2;

                for (int i = 0; i < incSize; i++) {
                    try {
                        objects.push(poolable.newInstance());
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw new EngineException("Failed to replenish pool.");
                    }
                }

                size += incSize;
            } else {
                throw new EngineException("Pool is empty.");
            }
        }

        --free;

        return objects.pop();
    }

    /**
     * Returns an object to this pool.
     *
     * @param object the object to return
     */
    public void put(T object) {
        if (object == null) {
            throw new EngineException("Cannot release null objects into the pool.");
        }

        ArrayDeque<T> objects = pool.get();
        objects.push(object);
        ++free;
    }
}
