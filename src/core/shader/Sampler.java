package core.shader;

import core.GL;
import core.utility.Buffers;

import java.nio.IntBuffer;

/**
 * A sampler variable.
 *
 * @author John Paul Quijano
 */
public class Sampler extends Variable {
    private IntBuffer units;

    /**
     * Creates a sampler with the given parameters.
     *
     * @param type - data type
     * @param identifier - variable identifier
     * @param size - array numCached
     * @param qualifiers - variable qualifiers
     */
    public Sampler(Shader.Type type, String identifier, int size, Shader.Qualifier... qualifiers) {
        super(type, identifier, null, size, qualifiers);

        units = Buffers.createIntBuffer(size == 0 ? 1 : size);
    }

    /**
     * Returns the sampler unit id at the given index.
     */
    public int getUnit(int index) {
        return units.get(index);
    }

    /**
     * Initializes the shader sampler units.
     *
     * @param index - sampler unit
     */
    int initUnits(int index, Shader shader) {
        int unit = index;

        if (getSize() > 0) {
            for (int i = 0; i < getSize(); i++) {
                units.put(i, unit++);
            }
        } else {
            units.put(0, unit++);
        }

        GL.setArray1(id, units);

        return unit;
    }
}
