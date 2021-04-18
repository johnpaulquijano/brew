package core.shader;

import core.shader.Shader.Stage;

import java.util.Objects;

/**
 * Contains the executed code for each shader stage.
 *
 * @author John Paul Quijano
 */
public class Executable extends ShaderComponent {
    private String vertexSource;
    private String fragmentSource;

    /**
     * Creates an executable with the given identifier.
     *
     * @param identifier - a unique string
     */
    public Executable(String identifier) {
        super(identifier);
    }

    /**
     * Sets the source code to be executed at the given shader stage.
     *
     * @param stage - shader execution stage
     * @param source - source code
     */
    public void setSource(Stage stage, String source) {
        if (stage == Stage.VERTEX) {
            vertexSource = source;
        } else if (stage == Stage.FRAGMENT) {
            fragmentSource = source;
        }
    }

    /**
     * Gives the source code for the vertex stage.
     *
     * @return source code for the vertex stage
     */
    public String getVertexSource() {
        return vertexSource;
    }

    /**
     * Gives the source code for the fragment stage.
     *
     * @return source code for the fragment stage
     */
    public String getFragmentSource() {
        return fragmentSource;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Executable)) {
            return false;
        }

        return ((ShaderComponent) o).getIdentifier().equals(getIdentifier());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(getIdentifier());
        return hash;
    }
}
