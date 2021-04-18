package core.event.type;

import core.EventType;

/**
 * Enumeration of mouse event types.
 */
public enum MouseEventType implements EventType {
    /**
     * Fired when a button is pressed.
     */
    PRESS,
    /**
     * Fired when a button is released.
     */
    RELEASE,
    /**
     * Fired when a button is pressed then released.
     */
    CLICK,
    /**
     * Fired when the cursor enters the display.
     */
    ENTER,
    /**
     * Fired when the cursor exits the display.
     */
    EXIT,
    /**
     * Fired when the cursor is moved.
     */
    MOVE,
    /**
     * Fired when the cursor is pressed then moved.
     */
    DRAG,
    /**
     * Fired when a scroller is ticked.
     */
    SCROLL
}
