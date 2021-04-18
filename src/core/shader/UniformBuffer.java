package core.shader;

import core.utility.EngineException;

import java.util.Objects;

/**
 * Represents a uniform buffer object.
 *
 * @author John Paul Quijano
 */
public class UniformBuffer extends ShaderComponent {
    private int dataSize;
    private int bufferSize;
    private String structID;
    private String bufferID;

    /**
     * Creates a uniform buffer object with the given parameters.
     *
     * @param cacheID - a unique identifier
     * @param arrayID - buffer identifier
     * @param structID - structure identifier
     * @param bufferSize - length of the buffer
     * @param dataSize - length of data
     */
    public UniformBuffer(String cacheID, String arrayID, String structID, int bufferSize, int dataSize) {
        super(cacheID);

        if (arrayID == null || arrayID.equals("")) {
            throw new EngineException("Buffer identifier cannot be null or empty.");
        }

        if (structID == null || structID.equals("")) {
            throw new EngineException("Structure identifier cannot be null or empty.");
        }

        this.dataSize = dataSize;
        this.bufferSize = bufferSize;
        this.structID = structID;
        this.bufferID = arrayID;
    }

    /**
     * Gives the buffer identifier.
     *
     * @return buffer identifier
     */
    public String getBufferID() {
        return bufferID;
    }

    /**
     * Gives the structure identifier.
     *
     * @return structure identifier
     */
    public String getStructID() {
        return structID;
    }

    /**
     * Gives the buffer length.
     *
     * @return buffer length
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gives the data length.
     *
     * @return data length
     */
    public int getDataSize() {
        return dataSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof UniformBuffer)) {
            return false;
        }

        return ((ShaderComponent) o).getIdentifier().equals(getIdentifier());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(getIdentifier());
        return hash;
    }
}
