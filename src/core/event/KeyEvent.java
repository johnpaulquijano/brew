package core.event;

import core.Event;
import core.Keyboard;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Events fired by the keyboard.
 */
public class KeyEvent extends Event<Keyboard> {
    public enum Key {
        NUM_0(GLFW.GLFW_KEY_0),
        NUM_1(GLFW.GLFW_KEY_1),
        NUM_2(GLFW.GLFW_KEY_2),
        NUM_3(GLFW.GLFW_KEY_3),
        NUM_4(GLFW.GLFW_KEY_4),
        NUM_5(GLFW.GLFW_KEY_5),
        NUM_6(GLFW.GLFW_KEY_6),
        NUM_7(GLFW.GLFW_KEY_7),
        NUM_8(GLFW.GLFW_KEY_8),
        NUM_9(GLFW.GLFW_KEY_9),
        A(GLFW.GLFW_KEY_A),
        APOSTROPHE(GLFW.GLFW_KEY_APOSTROPHE),
        B(GLFW.GLFW_KEY_B),
        BACKSLASH(GLFW.GLFW_KEY_BACKSLASH),
        BACKSPACE(GLFW.GLFW_KEY_BACKSPACE),
        C(GLFW.GLFW_KEY_C),
        CAPS_LOCK(GLFW.GLFW_KEY_CAPS_LOCK),
        COMMA(GLFW.GLFW_KEY_COMMA),
        D(GLFW.GLFW_KEY_D),
        DELETE(GLFW.GLFW_KEY_DELETE),
        DOWN(GLFW.GLFW_KEY_DOWN),
        E(GLFW.GLFW_KEY_E),
        END(GLFW.GLFW_KEY_END),
        ENTER(GLFW.GLFW_KEY_ENTER),
        EQUAL(GLFW.GLFW_KEY_EQUAL),
        ESCAPE(GLFW.GLFW_KEY_ESCAPE),
        F(GLFW.GLFW_KEY_F),
        F1(GLFW.GLFW_KEY_F1),
        F10(GLFW.GLFW_KEY_F10),
        F11(GLFW.GLFW_KEY_F11),
        F12(GLFW.GLFW_KEY_F12),
        F13(GLFW.GLFW_KEY_F13),
        F14(GLFW.GLFW_KEY_F14),
        F15(GLFW.GLFW_KEY_F15),
        F16(GLFW.GLFW_KEY_F16),
        F17(GLFW.GLFW_KEY_F17),
        F18(GLFW.GLFW_KEY_F18),
        F19(GLFW.GLFW_KEY_F19),
        F2(GLFW.GLFW_KEY_F2),
        F20(GLFW.GLFW_KEY_F20),
        F21(GLFW.GLFW_KEY_F21),
        F22(GLFW.GLFW_KEY_F22),
        F23(GLFW.GLFW_KEY_F23),
        F24(GLFW.GLFW_KEY_F24),
        F25(GLFW.GLFW_KEY_F25),
        F3(GLFW.GLFW_KEY_F3),
        F4(GLFW.GLFW_KEY_F4),
        F5(GLFW.GLFW_KEY_F5),
        F6(GLFW.GLFW_KEY_F6),
        F7(GLFW.GLFW_KEY_F7),
        F8(GLFW.GLFW_KEY_F8),
        F9(GLFW.GLFW_KEY_F9),
        G(GLFW.GLFW_KEY_G),
        GRAVE_ACCENT(GLFW.GLFW_KEY_GRAVE_ACCENT),
        H(GLFW.GLFW_KEY_H),
        HOME(GLFW.GLFW_KEY_HOME),
        I(GLFW.GLFW_KEY_I),
        INSERT(GLFW.GLFW_KEY_INSERT),
        J(GLFW.GLFW_KEY_J),
        K(GLFW.GLFW_KEY_K),
        KP_0(GLFW.GLFW_KEY_KP_0),
        KP_1(GLFW.GLFW_KEY_KP_1),
        KP_2(GLFW.GLFW_KEY_KP_2),
        KP_3(GLFW.GLFW_KEY_KP_3),
        KP_4(GLFW.GLFW_KEY_KP_4),
        KP_5(GLFW.GLFW_KEY_KP_5),
        KP_6(GLFW.GLFW_KEY_KP_6),
        KP_7(GLFW.GLFW_KEY_KP_7),
        KP_8(GLFW.GLFW_KEY_KP_8),
        KP_9(GLFW.GLFW_KEY_KP_9),
        KP_ADD(GLFW.GLFW_KEY_KP_ADD),
        KP_DECIMAL(GLFW.GLFW_KEY_KP_DECIMAL),
        KP_DIVIDE(GLFW.GLFW_KEY_KP_DIVIDE),
        KP_ENTER(GLFW.GLFW_KEY_KP_ENTER),
        KP_EQUAL(GLFW.GLFW_KEY_KP_EQUAL),
        KP_MULTIPLY(GLFW.GLFW_KEY_KP_MULTIPLY),
        KP_SUBTRACT(GLFW.GLFW_KEY_KP_SUBTRACT),
        L(GLFW.GLFW_KEY_L),
        LAST(GLFW.GLFW_KEY_LAST),
        LEFT(GLFW.GLFW_KEY_LEFT),
        LEFT_ALT(GLFW.GLFW_KEY_LEFT_ALT),
        LEFT_BRACKET(GLFW.GLFW_KEY_LEFT_BRACKET),
        LEFT_CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL),
        LEFT_SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT),
        LEFT_SUPER(GLFW.GLFW_KEY_LEFT_SUPER),
        M(GLFW.GLFW_KEY_M),
        MENU(GLFW.GLFW_KEY_MENU),
        MINUS(GLFW.GLFW_KEY_MINUS),
        N(GLFW.GLFW_KEY_N),
        NUM_LOCK(GLFW.GLFW_KEY_NUM_LOCK),
        O(GLFW.GLFW_KEY_O),
        P(GLFW.GLFW_KEY_P),
        PAGE_DOWN(GLFW.GLFW_KEY_PAGE_DOWN),
        PAGE_UP(GLFW.GLFW_KEY_PAGE_UP),
        PAUSE(GLFW.GLFW_KEY_PAUSE),
        PERIOD(GLFW.GLFW_KEY_PERIOD),
        PRINT_SCREEN(GLFW.GLFW_KEY_PRINT_SCREEN),
        Q(GLFW.GLFW_KEY_Q),
        R(GLFW.GLFW_KEY_R),
        RIGHT(GLFW.GLFW_KEY_RIGHT),
        RIGHT_ALT(GLFW.GLFW_KEY_RIGHT_ALT),
        RIGHT_BRACKET(GLFW.GLFW_KEY_RIGHT_BRACKET),
        RIGHT_CONTROL(GLFW.GLFW_KEY_RIGHT_CONTROL),
        RIGHT_SHIFT(GLFW.GLFW_KEY_RIGHT_SHIFT),
        S(GLFW.GLFW_KEY_S),
        SCROLL_LOCK(GLFW.GLFW_KEY_SCROLL_LOCK),
        SEMICOLON(GLFW.GLFW_KEY_SEMICOLON),
        SLASH(GLFW.GLFW_KEY_SLASH),
        SPACE(GLFW.GLFW_KEY_SPACE),
        T(GLFW.GLFW_KEY_T),
        TAB(GLFW.GLFW_KEY_TAB),
        U(GLFW.GLFW_KEY_U),
        UNKNOWN(GLFW.GLFW_KEY_UNKNOWN),
        UP(GLFW.GLFW_KEY_UP),
        V(GLFW.GLFW_KEY_V),
        W(GLFW.GLFW_KEY_W),
        WORLD_1(GLFW.GLFW_KEY_WORLD_1),
        WORLD_2(GLFW.GLFW_KEY_WORLD_2),
        X(GLFW.GLFW_KEY_X),
        Y(GLFW.GLFW_KEY_Y),
        Z(GLFW.GLFW_KEY_Z);

        private int id;
        private static Map<Integer, Key> keyMap = new HashMap<>();

        Key(int id) {
            this.id = id;
        }

        static {
            for (Key key : Key.values()) {
                keyMap.put(key.getID(), key);
            }
        }

        public int getID() {
            return id;
        }

        public static Key get(int id) {
            return keyMap.get(id);
        }
    }

    private Key key;
    private int code;
    private boolean altPressed;
    private boolean shiftPressed;
    private boolean superPressed;
    private boolean controlPressed;

    public KeyEvent(Keyboard source) {
        super(source);
    }

    /**
     * Sets the key where the last event originated.
     *
     * @param key - key where the last event originated
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * Gives the key where the last event originated.
     *
     * @return key where the last event originated
     */
    public Key getKey() {
        return key;
    }

    /**
     * Sets the system-specific scan code of the last pressed key.
     *
     * @param code - system-specific scan code of the last pressed key
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gives the system-specific scan code of the last pressed key.
     *
     * @return system-specific scan code of the last pressed key
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets whether or not any of the control keys is currently pressed.
     *
     * @param pressed - true to flag that any of the control keys is currently pressed
     */
    public void setControlPressed(boolean pressed) {
        controlPressed = pressed;
    }

    /**
     * Checks if any of the control keys is currently pressed.
     *
     * @return true if any of the control keys is currently pressed
     */
    public boolean isControlPressed() {
        return controlPressed;
    }

    /**
     * Sets whether or not any of the shift keys is currently pressed.
     *
     * @param pressed - true to flag that any of the shift keys is currently pressed
     */
    public void setShiftPressed(boolean pressed) {
        shiftPressed = pressed;
    }

    /**
     * Checks if any of the shift keys is currently pressed.
     *
     * @return true if any of the shift keys is currently pressed
     */
    public boolean isShiftPressed() {
        return shiftPressed;
    }

    /**
     * Sets whether or not any of the super keys is currently pressed.
     *
     * @param pressed - true to flag that any of the super keys is currently pressed
     */
    public void setSuperPressed(boolean pressed) {
        superPressed = pressed;
    }

    /**
     * Checks if any of the super keys is currently pressed.
     *
     * @return true if any of the super keys is currently pressed
     */
    public boolean isSuperPressed() {
        return superPressed;
    }

    /**
     * Sets whether or not any of the alt keys is currently pressed.
     *
     * @param pressed - true to flag that any of the alt keys is currently pressed
     */
    public void setAltPressed(boolean pressed) {
        altPressed = pressed;
    }

    /**
     * Checks if any of the alt keys is currently pressed.
     *
     * @return true if any of the alt keys is currently pressed
     */
    public boolean isAltPressed() {
        return altPressed;
    }
}
