package core.shader;

import core.shader.Shader.Qualifier;
import core.shader.Shader.Stage;
import core.shader.Shader.Type;
import core.utility.EngineException;

import java.util.Objects;

/**
 * Represents a shading language function.
 *
 * @author John Paul Quijano
 */
public class Function extends ShaderComponent {
    private Stage stage;
    private Type returnType;
    private Variable[] parameters;
    private String source;

    /**
     * Creates a function with the given parameters.
     *
     * @param returnType - return data type
     * @param identifier - a unique identifier
     * @param stage - shader program execution stage
     * @param parameters - function parameters
     */
    public Function(Type returnType, String identifier, Stage stage, Variable... parameters) {
        super(identifier);

        if (returnType == null) {
            throw new EngineException("Return type cannot be null.");
        }

        this.stage = stage;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    /**
     * Sets the source code.
     *
     * @param source - the shader source code
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gives the source code.
     *
     * @return source code
     */
    public String getSource() {
        return source;
    }

    /**
     * Gives the function return data type.
     *
     * @return function return data type
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Gives the shader execution stage.
     *
     * @return shader execution stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Gives the array of function parameters.
     *
     * @return array of function parameters
     */
    public Variable[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        String str = "";

        if (source != null) {
            String src[] = source.split("\n");

            str += returnType.getValue() + " " + getIdentifier() + "(";

            for (int i = 0; i < parameters.length; i++) {
                Variable parameter = parameters[i];

                for (Qualifier qualifier : parameter.getQualifiers()) {
                    str += qualifier + " ";
                }

                str += parameter.getType().getValue();

                if (parameter.getSize() > 0) {
                    str += "[" + parameter.getSize() + "]";
                }

                str += " " + parameter.getIdentifier();

                if (i < parameters.length - 1) {
                    str += ", ";
                }
            }

            str += ") {\n";

            for (String s : src) {
                str += "    " + s + "\n";
            }

            str += "}\n";
        }

        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Function)) {
            return false;
        }

        Function function = (Function) o;

        return function.getIdentifier().equals(getIdentifier()) && function.stage == stage;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(getIdentifier());
        hash = 97 * hash + Objects.hashCode(stage);
        return hash;
    }
}
