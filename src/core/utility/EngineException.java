package core.utility;

/**
 * Generic in-engine exception class.
 *
 * @author John Paul Quijano
 */
public class EngineException extends RuntimeException {
    /**
     * Creates an engine exception with the given message.
     *
     * @param message - description of the exception
     */
    public EngineException(String message) {
        super(message);
    }
}
