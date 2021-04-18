package core.shader;

import core.shader.Shader.Qualifier;
import core.shader.Shader.Type;
import core.utility.EngineException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a shading language variable.
 *
 * @author John Paul Quijano
 */
public class Variable extends ShaderComponent {
    private int size;
    private Type type;
    private Qualifier[] qualifiers;
    private String value;

    /**
     * Creates a variable with the given parameters.
     *
     * @param type - data type
     * @param identifier - variable identifier
     * @param value - initial value
     * @param size - array numCached
     * @param qualifiers - variable qualifiers
     */
    public Variable(Type type, String identifier, String value, int size, Qualifier... qualifiers) {
        super(identifier);

        if (type == null) {
            throw new EngineException("Type cannot be null.");
        }

        this.type = type;
        this.value = value;
        this.size = size;
        this.qualifiers = qualifiers;
    }

    /**
     * Sets the initial value of this variable.
     *
     * @param value - initial value of this variable
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the initial value of this variable.
     *
     * @return initial value of this variable
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the data type of this variable.
     *
     * @return data type of this variable
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the length of the array.
     *
     * @return array length
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the array of qualifiers.
     *
     * @return array of qualifiers
     */
    public Qualifier[] getQualifiers() {
        return qualifiers;
    }

    /**
     * Checks whether or not this variable contains the given qualifier.
     *
     * @param qualifier - qualifier to test containment
     * @return true if this variable has the given qualifier
     */
    public boolean hasQualifier(Qualifier qualifier) {
        return Arrays.asList(qualifiers).contains(qualifier);
    }

    @Override
    public String toString() {
        String str = "";

        for (Qualifier qualifier : qualifiers) {
            str += qualifier + " ";
        }

        str += type + " " + getIdentifier();

        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Variable)) {
            return false;
        }

        return ((ShaderComponent) o).getIdentifier().equals(getIdentifier());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(getIdentifier());
        return hash;
    }
}
