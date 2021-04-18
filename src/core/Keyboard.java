package core;

import core.event.KeyEvent;
import core.event.listener.KeyListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to keyboard input.
 *
 * @author John Paul Quijano
 */
public final class Keyboard {
    public enum Event {
        PRESSED(0), RELEASED(1), REPEATED(2), UNKNOWN(-1);

        private int value;

        Event(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    Display display;
    KeyEvent event;
    List<KeyListener> listeners;

    private boolean repeatEnabled;

    Keyboard() {
        event = new KeyEvent(this);
        listeners = new ArrayList<>();
    }

    /**
     * If enabled, KeyboardListener.pressed() is repeatedly invoked upon pressing a key.
     */
    public void setRepeatEnabled(boolean enabled) {
        repeatEnabled = enabled;
    }

    /**
     * Gives the repeat enabled state.
     *
     * @return repeat enabled state
     */
    public boolean isRepeatEnabled() {
        return repeatEnabled;
    }

    /**
     * Gives true if the given key is currently pressed.
     *
     * @return true if the given key is currently pressed
     */
    public boolean isKeyPressed(KeyEvent.Key key) {
        return display.isKeyPressed(key.getID());
    }

    /**
     * Appends a keyboard listener to this keyboard's list of listeners.
     *
     * @param listener - listener to add
     */
    public void addListener(KeyListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the listener at the given index
     *
     * @param listener - listener to remove
     */
    public void removeListener(KeyListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears the list of listeners.
     */
    public void removeAllListeners() {
        listeners.clear();
    }
}
