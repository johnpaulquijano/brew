package core.shader;

import core.utility.EngineException;

/**
 * Base class for a shader building block.
 *
 * @author John Paul Quijano
 */
public abstract class ShaderComponent {
    int id;

    private String comment;
    private String identifier;

    /**
     * Gives this shader component's unique integer identifier.
     *
     * @return this shader object's unique integer identifier
     */
    public int getID() {
        return id;
    }

    /**
     * Creates a shader component with the given unique string identifier.
     *
     * @param identifier a unique string identifier
     */
    public ShaderComponent(String identifier) {
        if (identifier == null || identifier.equals("")) {
            throw new EngineException("Identifier cannot be null or empty.");
        }

        this.identifier = identifier;
    }

    /**
     * Sets the comment to append to the shader source.
     *
     * @param comment - the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment attached to the shader source.
     *
     * @return comment attached to the shader source
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the unique identifier fo this shader component.
     *
     * @param identifier - unique identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the unique identifier for this shader component.
     *
     * @return unique identifier for this shader component
     */
    public String getIdentifier() {
        return identifier;
    }
}
