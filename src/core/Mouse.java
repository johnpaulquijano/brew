package core;

import core.event.MouseEvent;
import core.event.listener.MouseListener;
import core.math.EngineMath;
import core.math.Point;

import java.util.ArrayList;

/**
 * Access to native mouse input.
 *
 * @author John Paul Quijano
 */
public final class Mouse {
    public enum Cursor {
        ARROW, IBEAM, CROSSHAIR, HAND, HRESIZE, VRESIZE
    }

    Display display;
    MouseEvent event;
    ArrayList<MouseListener> listeners;

    private float spotX;
    private float spotY;
    private float sensitivity;
    private boolean grabbed;
    private boolean visible;
    private Cursor cursor;

    Mouse() {
        spotX = 0f;
        spotY = 1f;
        sensitivity = 1f;
        visible = true;
        cursor = Cursor.ARROW;
        event = new MouseEvent(this);
        listeners = new ArrayList<>();
    }

    /**
     * Positions the cursor at the given coordinates.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     */
    public void setLocation(int x, int y) {
        display.setCursorLocation(EngineMath.clamp(x, 0, display.getWidth()), EngineMath.clamp(y, 0, display.getHeight()));
    }

    /**
     * Moves the cursor to the given position relative to the display's origin.
     *
     * @param loc - coordinates relative to the display's origin
     */
    public void setLocation(Point loc) {
        setLocation(loc.getX(), loc.getY());
    }

    /**
     * Checks if the given mouse button is pressed.
     *
     * @param button - button to check for press event
     */
    public boolean isButtonPressed(int button) {
        return display.isButtonPressed(button);
    }

    /**
     * Sets the position of the event origin in normalized coordinates relative to the cursor's local coordinates.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     */
    public void setHotspot(float x, float y) {
        spotX = x;
        spotY = y;
    }

    /**
     * Gives the hotspot's x location relative to the cursor's location.
     *
     * @return hotspot x coordinate
     */
    public float getHostspotX() {
        return spotX;
    }

    /**
     * Gives the hotspot's y location relative to the cursor's location.
     *
     * @return hotspot y coordinate
     */
    public float getHostspotY() {
        return spotY;
    }

    /**
     * Sets the non-native mouse's sensitivity.
     *
     * @param sensitivity - mouse sensitivity
     */
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Gives the non-native mouse's sensitivity.
     *
     * @return non-native mouse's sensitivity
     */
    public float getSensitivity() {
        return sensitivity;
    }

    /**
     * If set, the cursor is hidden but behaves normally.
     *
     * @param visible - true to make cursor visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        display.setCursorVisible(visible);
    }

    /**
     * Checks if the mouse cursor is visible.
     *
     * @return true if the mouse cursor is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * If set, the cursor is hidden but behaves as if it's always centered.
     *
     * @param grabbed - true to grab cursor
     */
    public void setGrabbed(boolean grabbed) {
        this.grabbed = grabbed;
        visible = !grabbed;
        display.setCursorGrabbed(grabbed);
    }

    /**
     * Checks if the mouse cursor is grabbed.
     *
     * @return true if the mouse cursor is grabbed
     */
    public boolean isGrabbed() {
        return grabbed;
    }

    /**
     * Sets the cursor image displayed when in native mode.
     *
     * @param cursor - type of cursor to show
     */
    public void setCursor(Cursor cursor) {
        if (this.cursor == cursor) {
            return;
        }

        switch (cursor) {
            case ARROW:
                display.setCursorArrow();
                break;
            case IBEAM:
                display.setCursorIbeam();
                break;
            case CROSSHAIR:
                display.setCursorCrosshair();
                break;
            case HAND:
                display.setCursorHand();
                break;
            case HRESIZE:
                display.setCursorHResize();
                break;
            case VRESIZE:
                display.setCursorVResize();
                break;
        }

        this.cursor = cursor;
    }

    /**
     * Gives the type of native cursor to display.
     *
     * @return type of native cursor displayed
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * Adds a mouse listener to this mouse's list of listeners.
     *
     * @param listener - mouse listener to add
     */
    public void addListener(MouseListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the given listener.
     *
     * @param listener - listener to remove
     */
    public void removeListener(MouseListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears this mouse's list of listeners.
     */
    public void removeAllListeners() {
        listeners.clear();
    }
}
