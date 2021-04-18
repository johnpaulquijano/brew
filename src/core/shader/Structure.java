package core.shader;

import java.util.Objects;

/**
 * Represents a shading language structure declaration.
 *
 * @author John Paul Quijano
 */
public class Structure extends ShaderComponent {
    private Variable[] members;

    /**
     * Creates a structure with the given identifier and member variables.
     *
     * @param identifier - a unique string
     * @param members - array of member variables
     */
    public Structure(String identifier, Variable... members) {
        super(identifier);

        this.members = members;
    }

    /**
     * Gives the array of member variables.
     *
     * @return array of member variables
     */
    public Variable[] getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Structure)) {
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
