package core;

/**
 * Wrapper class for input devices such as the mouse, keyboard, and joystick.
 *
 * @author John Paul Quijano
 */
public final class Input {
    private Mouse mouse;
    private Keyboard keyboard;

    Input() {
        mouse = new Mouse();
        keyboard = new Keyboard();
    }

    /**
     * Returns a reference to the native mouse object.
     *
     * @return reference to the native mouse object
     */
    public Mouse getMouse() {
        return mouse;
    }

    /**
     * Returns a reference to the native keyboard object.
     *
     * @return reference to the native keyboard object
     */
    public Keyboard getKeyboard() {
        return keyboard;
    }
}
