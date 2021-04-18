package core.event.type;

import core.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of key event types.
 */
public enum KeyEventType implements EventType {
    /**
     * Fired when a key is pressed.
     */
    PRESS(0),
    /**
     * Fired when a key is released.
     */
    RELEASE(1),
    /**
     * Fired when a key is kept pressed and the same event is emulated. Key repeat must be enabled in the Keyboard object.
     */
    REPEAT(2);

    private int id;
    private static Map<Integer, KeyEventType> keyEventMap = new HashMap<>();

    KeyEventType(int id) {
        this.id = id;
    }

    static {
        for (KeyEventType key : KeyEventType.values()) {
            keyEventMap.put(key.getID(), key);
        }
    }

    /**
     * Gives the ID of the key event.
     *
     * @return ID of the key event
     */
    public int getID() {
        return id;
    }

    /**
     * Gives the key event type with the given ID.
     *
     * @param id - integer identifier of a key event type
     *
     * @return key event type with the given ID
     */
    public static KeyEventType get(int id) {
        return keyEventMap.get(id);
    }
}
