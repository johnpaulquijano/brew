package core.event;

import core.Event;
import core.Mouse;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Events fired by the mouse.
 */
public class MouseEvent extends Event<Mouse> {
    public enum Button {
        UNKNOWN(-1),
        B1(GLFW.GLFW_MOUSE_BUTTON_1),
        B2(GLFW.GLFW_MOUSE_BUTTON_2),
        B3(GLFW.GLFW_MOUSE_BUTTON_3),
        B4(GLFW.GLFW_MOUSE_BUTTON_4),
        B5(GLFW.GLFW_MOUSE_BUTTON_5),
        B6(GLFW.GLFW_MOUSE_BUTTON_6),
        B7(GLFW.GLFW_MOUSE_BUTTON_7),
        B8(GLFW.GLFW_MOUSE_BUTTON_8),
        LAST(GLFW.GLFW_MOUSE_BUTTON_LAST),
        LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT),
        MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE),
        RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

        private int id;
        private static Map<Integer, Button> buttonMap = new HashMap<>();

        Button(int id) {
            this.id = id;
        }

        static {
            for (Button button : Button.values()) {
                buttonMap.put(button.getID(), button);
            }
        }

        public int getID() {
            return id;
        }

        public static Button get(int id) {
            return buttonMap.get(id);
        }
    }

    public enum ScrollDirection {
        LEFT, RIGHT, UP, DOWN
    }

    private int x;
    private int y;
    private int dx;
    private int dy;
    private float scrollOffset;
    private Button button;
    private ScrollDirection scrollDirection;

    public MouseEvent(Mouse source) {
        super(source);
    }

    /**
     * Sets the cursor's horizontal location relative to the display's origin.
     *
     * @param x - cursor's horizontal location relative to the display's origin
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gives the cursor's horizontal location relative to the display's origin.
     *
     * @return cursor's horizontal location relative to the display's origin
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the cursor's vertical location relative to the display's origin.
     *
     * @param y - cursor's vertical location relative to the display's origin
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Gives the cursor's vertical location relative to the display's origin.
     *
     * @return cursor's vertical location relative to the display's origin
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the signed change in the x coordinate since the last move.
     *
     * @param dx - signed change in the x coordinate since the last move
     */
    public void setDX(int dx) {
        this.dx = dx;
    }

    /**
     * Gives the signed change in the x coordinate since the last move.
     *
     * @return signed change in x coordinate since the last move
     */
    public int getDeltaX() {
        return dx;
    }

    /**
     * Sets the signed change in the y coordinate since the last move.
     *
     * @param dy - signed change in the y coordinate since the last move
     */
    public void setDY(int dy) {
        this.dy = dy;
    }

    /**
     * Gives the signed change in the y coordinate since the last move.
     *
     * @return signed change in y coordinate since the last move
     */
    public int getDeltaY() {
        return dy;
    }

    /**
     * Sets the button where the last action event originated.
     *
     * @param button - button where the last action event originated
     */
    public void setButton(Button button) {
        this.button = button;
    }

    /**
     * Gives the button where the last action event originated.
     *
     * @return button where the last action event originated
     */
    public Button getButton() {
        return button;
    }

    /**
     * Sets the scroll offset the last time a scroll event was fired.
     *
     * @param offset - scroll offset the last time a scroll event was fired
     */
    public void setScrollOffset(float offset) {
        scrollOffset = offset;
    }

    /**
     * Gives the scroll offset the last time a scroll event was fired.
     *
     * @return scroll offset the last time a scroll event was fired
     */
    public float getScrollOffset() {
        return scrollOffset;
    }

    /**
     * Sets the direction of scroll.
     *
     * @param direction - direction of scroll
     */
    public void setScrollDirection(ScrollDirection direction) {
        scrollDirection = direction;
    }

    /**
     * Gives the direction of scroll.
     *
     * @return direction of scroll
     */
    public ScrollDirection getScrollDirection() {
        return scrollDirection;
    }
}
