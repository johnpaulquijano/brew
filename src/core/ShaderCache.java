package core;

import core.shader.Shader;
import core.shader.Structure;
import core.shader.UniformBuffer;
import core.shader.Variable;
import core.utility.EngineException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages caching of data in the shader.
 *
 * @author John Paul Quijano
 */
public class ShaderCache<E extends CacheObject> {
    private int index;
    private int capacity;
    private Set<E> cache;
    private Set<E> immutableCache;
    private Structure structure;
    private UniformBuffer buffer;

    private static int cacheID;

    /**
     * Creates a shader cache backed by the given uniform buffer.
     */
    public ShaderCache(Shader shader, String structureID, String bufferID, int cacheSize, int dataSize, Variable... variables) {
        capacity = cacheSize;

        int id = cacheID++;

        cache = new HashSet<>();
        structure = new Structure(structureID, variables);
        buffer = new UniformBuffer("cache" + String.valueOf(id), bufferID, structureID, cacheSize, dataSize);

        immutableCache = Collections.unmodifiableSet(cache);

        shader.addStructure(structure);
        shader.addUniformBuffer(buffer);
    }

    /**
     * Caches the given object to the next available storage location. If the cache already contains the given object,
     * the data in the object's storage location is overwritten.
     *
     * @param object - object to cache
     */
    public void cache(E object) {
        if (cache.size() == capacity) {
            throw new EngineException("Cache overflow.");
        }

        if (cache.add(object)) {
            object.index = index++;
            GL.updateUniformBuffer(buffer.getID(), object.index * object.dataBuffer.capacity(), object.dataBuffer);
        }
    }

    /**
     * Updates data at this object's position in the cache.
     *
     * @param object - cached object
     */
    public void update(E object) {
        if (!object.isCached()) {
            throw new EngineException("Object is not in the cache.");
        }

        GL.updateUniformBuffer(buffer.getID(), object.index * object.dataBuffer.capacity(), object.dataBuffer);
    }

    /**
     * Gives the immutable set of cached objects.
     *
     * @return immutable set of cached objects
     */
    public Set<E> getObjects() {
        return immutableCache;
    }

    /**
     * Checks if the cache contains the given object.
     *
     * @param object - cache object to check containment of
     *
     * @return true if the cache contains the given object
     */
    public boolean isCached(E object) {
        return cache.contains(object);
    }

    /**
     * Gives the current number of cached objects.
     *
     * @return number of cached objects
     */
    public int numCached() {
        return cache.size();
    }

    /**
     * Gives the maximum number of objects this cache can contain.
     *
     * @return maximum number of objects this cache can hold
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Removes all cached objects.
     */
    public void clear() {
        for (E object : cache) {
            object.index = -1;
        }

        cache.clear();
    }
}
