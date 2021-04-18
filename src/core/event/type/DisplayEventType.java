package core.event.type;

import core.EventType;

/**
 * Enumeration of display event types.
 */
public enum DisplayEventType implements EventType {
    /**
     * Fired when the display is moved.
     */
    MOVE,
    /**
     * Fired when the display is resized.
     */
    RESIZE,
    /**
     * Fired when the display gains focus.
     */
    FOCUS,
    /**
     * Fired when the display loses focus.
     */
    UNFOCUS,
    /**
     * Fired when the display is iconified.
     */
    ICONIFY,
    /**
     * Fired when the display is restored from being iconified.
     */
    RESTORE,
    /**
     * Fired when the display is closed and destroyed.
     */
    DISPOSE,
    /**
     * Fired when a request to close the display is received such as when the close button is clicked.
     */
    CLOSE_REQUEST
}
