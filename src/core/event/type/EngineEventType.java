package core.event.type;

import core.EventType;

/**
 * Enumeration of engine event types.
 */
public enum EngineEventType implements EventType {
    /**
     * Fired when the engine is paused.
     */
    PAUSE,
    /**
     * Fired when the engine is started.
     */
    START,
    /**
     * Fired when the engine is stopped.
     */
    STOP,
    /**
     * Fired at the beginning of the application loop.
     */
    LOOP_BEGIN,
    /**
     * Fired at the end of the application loop.
     */
    LOOP_END,
    /**
     * Fired when a request to stop the engine is received.
     */
    STOP_REQUESTED
}
