package core;

import core.event.DisplayEvent;
import core.event.KeyEvent;
import core.event.MouseEvent;
import core.event.listener.DisplayListener;
import core.event.listener.KeyListener;
import core.event.listener.MouseListener;
import core.event.type.DisplayEventType;
import core.event.type.KeyEventType;
import core.event.type.MouseEventType;
import core.math.Point;
import core.math.Vector2;
import core.utility.EngineException;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;

/**
 * The native rendering surface.
 *
 * @author John Paul Quijano
 */
public final class Display {
    public static final int DEFAULT_WIDTH = core.GL.DEFAULT_VIEWPORT_WIDTH;
    public static final int DEFAULT_HEIGHT = core.GL.DEFAULT_VIEWPORT_HEIGHT;

    private boolean reset;
    private boolean resized;
    private boolean closeRequested;
    private int prevx;
    private int prevy;
    private int width;
    private int height;
    private int screenWidth;
    private int screenHeight;
    private int cursorState;
    private boolean pressed;
    private boolean dragged;
    private boolean initialized;
    private boolean visible;
    private boolean iconified;
    private boolean decorated;
    private boolean resizable;
    private boolean fullscreen;
    private boolean vSyncEnabled;
    private boolean dirty;
    private boolean titleDirty;
    private boolean visibleDirty;
    private boolean iconifyDirty;
    private boolean sizeDirty;
    private boolean locDirty;
    private long displayID = MemoryUtil.NULL;
    private long monitorID = MemoryUtil.NULL;
    private long arrow;
    private long ibeam;
    private long crosshair;
    private long hand;
    private long hresize;
    private long vresize;
    private Point location;
    private Vector2 center;
    private String title;
    private DisplayEvent event;
    private DisplayMode displayMode;
    private DisplayMode[] displayModes;
    private GLFWVidMode videoMode;
    private ArrayList<DisplayListener> listeners;
    private GLFWKeyCallback keyCallback;
    private GLFWErrorCallback errorCallback;
    private GLFWWindowPosCallback moveCallback;
    private GLFWWindowFocusCallback focusCallback;
    private GLFWWindowCloseCallback closeCallback;
    private GLFWWindowSizeCallback resizeCallback;
    private GLFWWindowIconifyCallback iconifyCallback;
    private GLFWCursorPosCallback mouseMoveCallback;
    private GLFWCursorEnterCallback mouseEnterCallback;
    private GLFWMouseButtonCallback mousePressCallback;
    private GLFWScrollCallback mouseScrollCallback;

    /**
     * Constructs a display given an input object.
     *
     * @param input wraps input device interfaces
     */
    Display(Input input) {
        event = new DisplayEvent(this);

        initDisplay();
        initCallbacks(this, input);
        createDisplay();

        initialized = true;
    }

    /**
     * Gives a list of available display modes.
     *
     * @return list of available display modes
     */
    public DisplayMode[] getAvailableDisplayModes() {
        return displayModes;
    }

    /**
     * Gives whether or not the given screen coordinates are within the bounds of this display.
     *
     * @param x - first screen-space vector component
     * @param y - second screen-space vector component
     *
     * @return true if the given screen coordinates are within the bounds of this display
     */
    public boolean contains(int x, int y) {
        return x > 0 && y > 0 && x < width && y < height;
    }

    /**
     * Gives whether or not the given screen coordinates are within the bounds of this display.
     *
     * @param point - screen-space location vector
     *
     * @return true if the given screen coordinates are within the bounds of this display
     */
    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }

    /**
     * Gives whether or not the given screen coordinates are within the bounds of this display.
     *
     * @param point - screen-space location vector
     *
     * @return true if the given screen coordinates are within the bounds of this display
     */
    public boolean contains(Vector2 point) {
        return point.getX() > 0 && point.getY() > 0 && point.getX() < width && point.getY() < height;
    }

    /**
     * Sets the text shown on this display's title bar.
     */
    public void setTitle(String title) {
        this.title = title;
        titleDirty = true;
        dirty = true;
    }

    /**
     * Gives the text shown on the title bar of this display.
     *
     * @return - text shown on the title bar of this display
     */
    public String getTitle() {
        return title;
    }

    /**
     * If set, renders this display on the screen.
     *
     * @param visible - if true, this display is rendered on the screen
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        visibleDirty = true;
        dirty = true;
    }

    /**
     * Gives this display's visibility state.
     *
     * @return true if this display is rendered on the screen
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets vertical synchronization state.
     *
     * @param enabled - true to enable v-sync
     */
    public void setVSyncEnabled(boolean enabled) {
        vSyncEnabled = enabled;
        GLFW.glfwSwapInterval(vSyncEnabled ? 1 : 0);
    }

    /**
     * Gives this display's vertical synchronization state.
     *
     * @return true if v-sync is enabled
     */
    public boolean isVSyncEnabled() {
        return vSyncEnabled;
    }

    /**
     * If set, reduces this display to a taskbar icon.
     *
     * @param iconified - true to reduce this display to a taskbar icon
     */
    public void setIconified(boolean iconified) {
        this.iconified = iconified;
        iconifyDirty = true;
        dirty = true;
    }

    /**
     * Gives this display's iconified state.
     *
     * @return true if this display is iconified
     */
    public boolean isIconified() {
        return iconified;
    }

    /**
     * If set, this display shows the title bar and other hotspots.
     *
     * @param decorated - true to show this display's title bar and other hotspots
     */
    public void setDecorated(boolean decorated) {
        if (this.decorated != decorated) {
            this.decorated = decorated;
            reset = true;
        }
    }

    /**
     * Gives this display's decorated state.
     *
     * @return true if this display is decorated
     */
    public boolean isDecorated() {
        return decorated;
    }

    /**
     * If set, this display's dimensions can be modified either visually (dragging the sides) or programmatically.
     *
     * @param resizable - true to make this display's dimensions modifiable
     */
    public void setResizable(boolean resizable) {
        if (this.resizable != resizable) {
            this.resizable = resizable;
            reset = true;
        }
    }

    /**
     * Gives this display's resizable state.
     *
     * @return true if this display is resizable
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * If set, resizes this display to cover the entire screen.
     *
     * @param fs - true to enter fullscreen exclusive mode
     */
    public void setFullscreen(boolean fs) {
        if (fullscreen != fs) {
            fullscreen = fs;
            reset = true;
        }
    }

    /**
     * Gives this display's fullscreen state.
     *
     * @return true if this display is fullscreen
     */
    public boolean isFullscreen() {
        return fullscreen;
    }

    /**
     * Sets this display's display mode.
     *
     * @param dm - display mode to set
     */
    public void setDisplayMode(DisplayMode dm) {
        if (displayMode != dm) {
            displayMode = dm;
            width = dm.getWidth();
            height = dm.getHeight();
            center.set(width, height).divide(2f);
            reset = true;
        }
    }

    /**
     * Gives this display's display mode.
     *
     * @return the display mode
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Sets this display's width.
     *
     * @param width - width to set
     */
    public void setWidth(int width) {
        this.width = width;
        center.set(width, height).divide(2f);
        sizeDirty = true;
        dirty = true;
    }

    /**
     * Gives this display's width.
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets this display's height.
     *
     * @param height - height to set
     */
    public void setHeight(int height) {
        this.height = height;
        center.set(width, height).divide(2f);
        sizeDirty = true;
        dirty = true;
    }

    /**
     * Gives this display's height.
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets this display's width and height.
     *
     * @param width - width to set
     * @param height - height to set
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        center.set(width, height).divide(2f);
        sizeDirty = true;
        dirty = true;
    }

    /**
     * Gives the centroid of the display.
     *
     * @return centroid of the display
     */
    public Vector2 getCenter() {
        return center;
    }

    /**
     * Gives the width of the monitor's display area.
     *
     * @return width of the monitor's display area
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Gives the height of the monitor's display area.
     *
     * @return height of the monitor's display area
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Moves this display's upper-left corner to the given screen coordinates.
     *
     * @param x - first vector component
     * @param y - second vector component
     */
    public void setLocation(int x, int y) {
        if (fullscreen) {
            return;
        }

        location.set(x, y);
        locDirty = true;
        dirty = true;
    }

    /**
     * Moves this display's upper-left corner to the given screen coordinates.
     *
     * @param location - location vector
     */
    public void setLocation(Point location) {
        setLocation(location.getX(), location.getY());
    }

    /**
     * Gives the location of this display.
     *
     * @return location of this display
     */
    public Point getLocation() {
        return location;
    }

    /**
     * Centers this display on the screen.
     */
    public void setCentered() {
        setLocation((screenWidth - width) / 2, (screenHeight - height) / 2 + (decorated ? 30 : 0));
    }

    /**
     * Adds a listener to this display's list of listeners.
     *
     * @param listener - listener to add
     */
    public void addListener(DisplayListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Gives the listener at the given index.
     *
     * @param index - index of the listener
     * @return listener at the given index
     */
    public DisplayListener getListener(int index) {
        return listeners.get(index);
    }

    /**
     * Removes the listener at the given index.
     *
     * @param listener - listener to remove
     */
    public void removeListener(DisplayListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears this display's list of listeners.
     */
    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Gives the number of listeners.
     *
     * @return number of listeners
     */
    public int numListeners() {
        return listeners.size();
    }

    boolean isReset() {
        return reset;
    }

    boolean isResized() {
        return resized;
    }

    boolean isCloseRequested() {
        return closeRequested;
    }

    boolean isButtonPressed(int button) {
        if (!initialized) {
            throw new EngineException("Display has not yet been initialized.");
        }

        return GLFW.glfwGetMouseButton(displayID, button) == GLFW.GLFW_PRESS;
    }

    boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(displayID, key) == GLFW.GLFW_PRESS;
    }

    void setCursorArrow() {
        GLFW.glfwSetCursor(displayID, arrow);
    }

    void setCursorIbeam() {
        GLFW.glfwSetCursor(displayID, ibeam);
    }

    void setCursorCrosshair() {
        GLFW.glfwSetCursor(displayID, crosshair);
    }

    void setCursorHand() {
        GLFW.glfwSetCursor(displayID, hand);
    }

    void setCursorHResize() {
        GLFW.glfwSetCursor(displayID, hresize);
    }

    void setCursorVResize() {
        GLFW.glfwSetCursor(displayID, vresize);
    }

    void setCursorGrabbed(boolean grabbed) {
        if (!initialized) {
            throw new EngineException("Display has not yet been initialized.");
        }

        /**
         * Reposition the cursor to the upper-right corner of the screen.
         */
        if (grabbed) {
            GLFW.glfwSetCursorPos(displayID, 1, 1);
        }

        cursorState = grabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL;
        GLFW.glfwSetInputMode(displayID, GLFW.GLFW_CURSOR, cursorState);
    }

    void setCursorVisible(boolean visible) {
        if (!initialized) {
            throw new EngineException("Display has not yet been initialized.");
        }

        cursorState = visible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN;
        GLFW.glfwSetInputMode(displayID, GLFW.GLFW_CURSOR, cursorState);
    }

    void setCursorLocation(int x, int y) {
        if (!initialized) {
            throw new EngineException("Display has not yet been initialized.");
        }

        GLFW.glfwSetCursorPos(displayID, x, y);
    }

    double getTime() {
        return GLFW.glfwGetTime();
    }

    void setTime(double time) {
        GLFW.glfwSetTime(time);
    }

    void swap() {
        GLFW.glfwSwapBuffers(displayID);

        reset = false;
        resized = false;
        closeRequested = false;
    }

    void restore() {
        if (!initialized) {
            throw new EngineException("Cannot restore display before initialization.");
        }

        GLFW.glfwDestroyWindow(displayID);
        createDisplay();
    }

    private void initDisplay() {
        if (!GLFW.glfwInit()) {
            throw new EngineException("Failed to initialize display.");
        }

        monitorID = GLFW.glfwGetPrimaryMonitor();
        videoMode = GLFW.glfwGetVideoMode(monitorID);

        title = "";
        decorated = true;
        resizable = true;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        screenWidth = videoMode.width();
        screenHeight =  videoMode.height();
        cursorState = GLFW.GLFW_CURSOR_NORMAL;
        location = new Point();
        listeners = new ArrayList<>();
        center = new Vector2(width, height).multiply(0.5f);
        displayMode = new DisplayMode(videoMode.redBits(), videoMode.refreshRate(), screenWidth, screenHeight);

        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(monitorID);
        displayModes = new DisplayMode[modes.capacity()];

        for (int i = 0; i < displayModes.length; i++) {
            GLFWVidMode mode = modes.get(i);
            displayModes[i] = new DisplayMode(mode.redBits() * 3, mode.refreshRate(), mode.width(), mode.height());
        }
    }

    private void createDisplay() {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, displayMode.getBits());
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, displayMode.getBits());
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, displayMode.getBits());
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, displayMode.getFrequency());
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, decorated ? GL11.GL_TRUE : GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GL11.GL_TRUE : GL11.GL_FALSE);

        displayID = GLFW.glfwCreateWindow(width, height, title, fullscreen ? monitorID : MemoryUtil.NULL, MemoryUtil.NULL);

        if (displayID == MemoryUtil.NULL) {
            GLFW.glfwTerminate();
            throw new EngineException("Failed to create display.");
        }

        GLFW.glfwSetErrorCallback(errorCallback);
        GLFW.glfwSetKeyCallback(displayID, keyCallback);
        GLFW.glfwSetWindowPosCallback(displayID, moveCallback);
        GLFW.glfwSetWindowCloseCallback(displayID, closeCallback);
        GLFW.glfwSetWindowSizeCallback(displayID, resizeCallback);
        GLFW.glfwSetWindowFocusCallback(displayID, focusCallback);
        GLFW.glfwSetWindowIconifyCallback(displayID, iconifyCallback);
        GLFW.glfwSetCursorPosCallback(displayID, mouseMoveCallback);
        GLFW.glfwSetCursorEnterCallback(displayID, mouseEnterCallback);
        GLFW.glfwSetMouseButtonCallback(displayID, mousePressCallback);
        GLFW.glfwSetScrollCallback(displayID, mouseScrollCallback);

        GLFW.glfwSetWindowShouldClose(displayID, true);
        GLFW.glfwSetInputMode(displayID, GLFW.GLFW_CURSOR, cursorState);
        GLFW.glfwMakeContextCurrent(displayID);
        GLFW.glfwShowWindow(displayID);
        GLFW.glfwSwapInterval(0);

        arrow = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        ibeam = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
        crosshair = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
        hand = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        hresize = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
        vresize = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);

        GL.createCapabilities(true);
    }

    private void initCallbacks(Display display, Input input) {
        Mouse mouse = input.getMouse();
        Keyboard keyboard = input.getKeyboard();

        mouse.display = display;
        keyboard.display = display;

        errorCallback = new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
            throw new EngineException("Exception occured in the native windowing system.");
            }
        };

        focusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                if (focused) {
                    event.setType(DisplayEventType.FOCUS);

                    for (DisplayListener listener : listeners) {
                        listener.listen(event);
                    }
                } else {
                    event.setType(DisplayEventType.UNFOCUS);

                    for (DisplayListener listener : listeners) {
                        listener.listen(event);
                    }
                }
            }
        };

        moveCallback = new GLFWWindowPosCallback() {
            @Override
            public void invoke(long window, int xpos, int ypos) {
                location.set(xpos, ypos);
                event.setType(DisplayEventType.MOVE);

                for (DisplayListener listener : listeners) {
                    listener.listen(event);
                }
            }
        };

        closeCallback = new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                closeRequested = true;
                event.setType(DisplayEventType.CLOSE_REQUEST);

                for (DisplayListener listener : listeners) {
                    listener.listen(event);
                }
            }
        };

        resizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                resized = true;
                event.setType(DisplayEventType.RESIZE);

                width = w;
                height = h;
                center.set(width, height).divide(2f);

                for (DisplayListener listener : listeners) {
                    listener.listen(event);
                }
            }
        };

        iconifyCallback = new GLFWWindowIconifyCallback() {
            @Override
            public void invoke(long window, boolean i) {
                iconified = i;

                if (iconified) {
                    event.setType(DisplayEventType.ICONIFY);

                    for (DisplayListener listener : listeners) {
                        listener.listen(event);
                    }
                } else {
                    event.setType(DisplayEventType.RESTORE);

                    for (DisplayListener listener : listeners) {
                        listener.listen(event);
                    }
                }
            }
        };

        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                keyboard.event.setType(KeyEventType.get(action));
                keyboard.event.setKey(KeyEvent.Key.get(key));
                keyboard.event.setCode(scancode);
                keyboard.event.setAltPressed((mods & GLFW.GLFW_MOD_ALT) == GLFW.GLFW_MOD_ALT);
                keyboard.event.setShiftPressed((mods & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT);
                keyboard.event.setSuperPressed((mods & GLFW.GLFW_MOD_SUPER) == GLFW.GLFW_MOD_SUPER);
                keyboard.event.setControlPressed((mods & GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL);

                if (action == GLFW.GLFW_PRESS || (keyboard.isRepeatEnabled() && action == GLFW.GLFW_REPEAT)) {
                    for (KeyListener listener : keyboard.listeners) {
                        listener.listen(keyboard.event);
                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    for (KeyListener listener : keyboard.listeners) {
                        listener.listen(keyboard.event);
                    }
                }
            }
        };

        mouseMoveCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                int x = (int) xpos;
                int y = (int) ypos;

                mouse.event.setX(x);
                mouse.event.setY(y);
                mouse.event.setDX(x - prevx);
                mouse.event.setDY(y - prevy);

                mouse.event.setType(MouseEventType.MOVE);

                for (MouseListener listener : mouse.listeners) {
                    listener.listen(mouse.event);
                }

                if (pressed) {
                    mouse.event.setType(MouseEventType.DRAG);

                    for (MouseListener listener : mouse.listeners) {
                        listener.listen(mouse.event);
                    }

                    dragged = true;
                }

                prevx = x;
                prevy = y;
            }
        };

        mouseEnterCallback = new GLFWCursorEnterCallback() {
            @Override
            public void invoke(long window, boolean entered) {
                if (entered) {
                    mouse.event.setType(MouseEventType.ENTER);

                    for (MouseListener listener : mouse.listeners) {
                        listener.listen(mouse.event);
                    }
                } else {
                    mouse.event.setType(MouseEventType.EXIT);

                    for (MouseListener listener : mouse.listeners) {
                        listener.listen(mouse.event);
                    }
                }
            }

            @Override
            public void callback(long args) {
            }
        };

        mousePressCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                mouse.event.setButton(MouseEvent.Button.get(button));

                if (action == GLFW.GLFW_PRESS) {
                    mouse.event.setType(MouseEventType.PRESS);

                    for (MouseListener listener : mouse.listeners) {
                        listener.listen(mouse.event);
                    }

                    pressed = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    mouse.event.setType(MouseEventType.RELEASE);

                    for (MouseListener listener : mouse.listeners) {
                        listener.listen(mouse.event);
                    }

                    if (!dragged) {
                        mouse.event.setType(MouseEventType.CLICK);

                        for (MouseListener listener : mouse.listeners) {
                            listener.listen(mouse.event);
                        }
                    }

                    pressed = false;
                    dragged = false;
                }
            }
        };

        mouseScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                mouse.event.setType(MouseEventType.SCROLL);

                if (xoffset > 0) {
                    mouse.event.setScrollOffset((float) xoffset);
                    mouse.event.setScrollDirection(MouseEvent.ScrollDirection.RIGHT);
                } else if (xoffset < 0) {
                    mouse.event.setScrollOffset((float) xoffset);
                    mouse.event.setScrollDirection(MouseEvent.ScrollDirection.LEFT);
                }

                if (yoffset > 0) {
                    mouse.event.setScrollOffset((float) yoffset);
                    mouse.event.setScrollDirection(MouseEvent.ScrollDirection.UP);
                } else if (yoffset < 0) {
                    mouse.event.setScrollOffset((float) yoffset);
                    mouse.event.setScrollDirection(MouseEvent.ScrollDirection.DOWN);
                }

                for (MouseListener listener : mouse.listeners) {
                    listener.listen(mouse.event);
                }
            }

        };
    }

    void update() {
        GLFW.glfwPollEvents();

        if (dirty) {
            if (titleDirty) {
                GLFW.glfwSetWindowTitle(displayID, title);
                titleDirty = false;
            }

            if (visibleDirty) {
                if (visible) {
                    GLFW.glfwShowWindow(displayID);
                } else {
                    GLFW.glfwHideWindow(displayID);
                }

                visibleDirty = false;
            }

            if (iconifyDirty) {
                if (iconified) {
                    GLFW.glfwIconifyWindow(displayID);
                } else {
                    GLFW.glfwRestoreWindow(displayID);
                }

                iconifyDirty = false;
            }

            if (sizeDirty) {
                GLFW.glfwSetWindowSize(displayID, width, height);
                sizeDirty = false;
            }

            if (locDirty) {
                GLFW.glfwSetWindowPos(displayID, location.getX(), location.getY());
                locDirty = false;
            }

            dirty = false;
        }
    }

    void free() {
        GLFW.glfwSetKeyCallback(displayID, null).free();
        GLFW.glfwSetCursorPosCallback(displayID, null).free();
        GLFW.glfwSetCursorEnterCallback(displayID, null).free();
        GLFW.glfwSetMouseButtonCallback(displayID, null).free();
        GLFW.glfwSetScrollCallback(displayID, null).free();
        GLFW.glfwSetWindowPosCallback(displayID, null).free();
        GLFW.glfwSetWindowCloseCallback(displayID, null).free();
        GLFW.glfwSetWindowSizeCallback(displayID, null).free();
        GLFW.glfwSetWindowIconifyCallback(displayID, null).free();
        GLFW.glfwSetWindowFocusCallback(displayID, null).free();
        GLFW.glfwSetErrorCallback(null).free();

        GLFW.glfwTerminate();

        event.setType(DisplayEventType.DISPOSE);

        for (DisplayListener listener : listeners) {
            listener.listen(event);
        }
    }
}
