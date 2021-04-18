package core.utility;

import core.Camera;
import core.Display;
import core.Engine;
import core.Keyboard;
import core.event.EngineEvent;
import core.event.KeyEvent;
import core.event.MouseEvent;
import core.event.listener.EngineListener;
import core.event.listener.MouseListener;
import core.event.type.EngineEventType;
import core.event.type.MouseEventType;
import core.math.EngineMath;
import core.math.Vector3;

public class Navigator implements EngineListener, MouseListener {
    private float sensitivity;
    private float keyMoveScale;
    private float keyLookScale;
    private float mouseMoveScale;
    private float mouseLookScale;
    private boolean enabled;
    private boolean mouseEnabled;
    private boolean keyboardEnabled;
    private KeyEvent.Key mfKey;
    private KeyEvent.Key mbKey;
    private KeyEvent.Key mrKey;
    private KeyEvent.Key mlKey;
    private KeyEvent.Key luKey;
    private KeyEvent.Key ldKey;
    private KeyEvent.Key lrKey;
    private KeyEvent.Key llKey;

    private Engine engine;
    private Camera camera;
    private Display display;
    private Keyboard keyboard;

    public Navigator() {
        enabled = true;
        mouseEnabled = true;
        keyboardEnabled = true;

        sensitivity = 0.5f;
        keyMoveScale = 0.025f;
        keyLookScale = 5f;
        mouseMoveScale = 0.1f;
        mouseLookScale = 1f;

        mfKey = KeyEvent.Key.W;
        mbKey = KeyEvent.Key.S;
        mrKey = KeyEvent.Key.D;
        mlKey = KeyEvent.Key.A;
        luKey = KeyEvent.Key.UP;
        ldKey = KeyEvent.Key.DOWN;
        lrKey = KeyEvent.Key.RIGHT;
        llKey = KeyEvent.Key.LEFT;
    }

    /**
     * If set, enables this navigator.
     *
     * @param enabled - if true, this navigator is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether or not this navigator is enabled.
     *
     * @return true if this navigator is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * If set, enables mouse control.
     *
     * @param enabled - if true, mouse control is enabled
     */
    public void setMouseEnabled(boolean enabled) {
        mouseEnabled = enabled;
    }

    /**
     * Returns whether or not mouse control is enabled.
     *
     * @return true if mouse control is enabled
     */
    public boolean isMouseEnabled() {
        return mouseEnabled;
    }

    /**
     * If set, enables key controls.
     *
     * @param enabled - if true, keyboard control is enabled
     */
    public void setKeyboardEnabled(boolean enabled) {
        keyboardEnabled = enabled;
    }

    /**
     * Returns whether or not keyboard control is enabled.
     *
     * @return true if keyboard control is enabled
     */
    public boolean isKeyboardEnabled() {
        return keyboardEnabled;
    }

    /**
     * Sets sensitivity of all devices. Input is clamped between 0 and 1, inclusive.
     *
     * @param sensitivity - control sensitivity
     */
    public void setSensitivity(float sensitivity) {
        this.sensitivity = EngineMath.clamp(sensitivity, 0, 1);
    }

    /**
     * Returns the control sensitivity.
     *
     * @return control sensitivity
     */
    public float getSensitivity() {
        return sensitivity;
    }

    /**
     * Scales the sensitivity of keyboard control for camera movement. Default value is 1.
     *
     * @param scale - the scaling value
     */
    public void setKeyMoveScale(float scale) {
        keyMoveScale = scale;
    }

    /**
     * Returns the scaling value for camera movement using the keyboard.
     *
     * @return scaling value
     */
    public float getKeyMoveScale() {
        return keyMoveScale;
    }

    /**
     * Scales the sensitivity of keyboard control for camera rotation. Default value is 1.
     *
     * @param scale - the scaling value
     */
    public void setKeyLookScale(float scale) {
        keyLookScale = scale;
    }

    /**
     * Returns the scaling value for camera rotation using the keyboard.
     *
     * @return scaling value
     */
    public float getKeyLookScale() {
        return keyLookScale;
    }

    /**
     * Scales the sensitivity of mouse control for camera movement. Default value is 1.
     *
     * @param scale - the scaling value
     */
    public void setMouseMoveScale(float scale) {
        mouseMoveScale = scale;
    }

    /**
     * Returns the scaling value for camera movement using the mouse.
     *
     * @return scaling value
     */
    public float getMouseMoveScale() {
        return mouseMoveScale;
    }

    /**
     * Scales the sensitivity of mouse control for camera rotation. Default value is 1.
     *
     * @param scale - the scaling value
     */
    public void setMouseLookScale(float scale) {
        mouseLookScale = scale;
    }

    /**
     * Returns the scaling value for camera rotation using the mouse.
     *
     * @return scaling value
     */
    public float getMouseLookScale() {
        return mouseLookScale;
    }

    /**
     * Sets the key used to move the camera forward. Default key is Keyboard.KEY_W.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setMoveForwardKey(KeyEvent.Key key) {
        mfKey = key;
    }

    /**
     * Returns the key used to move the camera forward.
     *
     * @return key used to move the camera forward
     */
    public KeyEvent.Key getMoveForwardKey() {
        return mfKey;
    }

    /**
     * Sets the key used to move the camera backward. Default key is Keyboard.KEY_S.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setMoveBackwardKey(KeyEvent.Key key) {
        mbKey = key;
    }

    /**
     * Returns the key used to move the camera backward.
     *
     * @return key used to move the camera backward
     */
    public KeyEvent.Key getMoveBackwardKey() {
        return mbKey;
    }

    /**
     * Sets the key used to move the camera to the right. Default key is Keyboard.KEY_D.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setMoveRightKey(KeyEvent.Key key) {
        mrKey = key;
    }

    /**
     * Returns the key used to move the camera to the right.
     *
     * @return key used to move the camera to the right
     */
    public KeyEvent.Key getMoveRightKey() {
        return mrKey;
    }

    /**
     * Sets the key used to move the camera to the left. Default key is Keyboard.KEY_A.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setMoveLeftKey(KeyEvent.Key key) {
        mlKey = key;
    }

    /**
     * Returns the key used to move the camera to the left.
     *
     * @return key used to move the camera to the left
     */
    public KeyEvent.Key getMoveLeftKey() {
        return mlKey;
    }

    /**
     * Sets the key used to rotate the camera upwards. Default key is Keyboard.KEY_UP.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setLookUpKey(KeyEvent.Key key) {
        luKey = key;
    }

    /**
     * Returns the key used to rotate the camera upwards.
     *
     * @return key used to rotate the camera upwards
     */
    public KeyEvent.Key getLookUpKey() {
        return luKey;
    }

    /**
     * Sets the key used to rotate the camera downwards. Default key is Keyboard.KEY_DOWN.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setLookDownKey(KeyEvent.Key key) {
        ldKey = key;
    }

    /**
     * Returns the key used to rotate the camera downwards.
     *
     * @return key used to rotate the camera downwards
     */
    public KeyEvent.Key getLookDownKey() {
        return ldKey;
    }

    /**
     * Sets the key used to rotate the camera to the right. Default key is Keyboard.KEY_RIGHT.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setLookRightKey(KeyEvent.Key key) {
        lrKey = key;
    }

    /**
     * Returns the key used to rotate the camera to the right.
     *
     * @return key used to rotate the camera to the right
     */
    public KeyEvent.Key getLookRightKey() {
        return lrKey;
    }

    /**
     * Sets the key used to rotate the camera to the left. Default key is Keyboard.KEY_LEFT.
     *
     * @param key - a Keyboard.KEY_* constant
     */
    public void setLookLeftKey(KeyEvent.Key key) {
        llKey = key;
    }

    /**
     * Returns the key used to rotate the camera to the left.
     *
     * @return key used to rotate the camera to the left
     */
    public KeyEvent.Key getLookLeftKey() {
        return llKey;
    }

    /**
     * Sets the camera to control. Input cannot be null.
     *
     * @param camera - camera to control
     */
    public void setCamera(Camera camera) {
        if (camera == null) {
            throw new EngineException("Camera cannot be null.");
        }

        this.camera = camera;
    }

    /**
     * Returns the camera controlled by this navigator.
     *
     * @return camera controlled by this navigator
     */
    public Camera getCamera() {
        return camera;
    }

    @Override
    public void listen(MouseEvent event) {
        if (enabled && mouseEnabled) {
            if (event.getType() == MouseEventType.MOVE) {
                float x = sensitivity * mouseLookScale * event.getDeltaX() + display.getCenter().getX();
                float y = sensitivity * mouseLookScale * -event.getDeltaY() + display.getCenter().getY();

                Vector3 worldUp = Pools.Vector3.get().set(Vector3.UNIT_Y);
                Vector3 camDir = Pools.Vector3.get().set(camera.getDirection());
                Vector3 camFocus = camera.getWorldCoordinates(x, y, 0f, Pools.Vector3.get());

                float dirDotUp = camDir.dot(Vector3.UNIT_Y);
                camera.lookAt(camFocus, worldUp.lerp(camera.getUp(), EngineMath.abs(dirDotUp)));

                Pools.Vector3.put(worldUp);
                Pools.Vector3.put(camFocus);
                Pools.Vector3.put(camDir);
            } else if (event.getType() == MouseEventType.SCROLL) {
                switch (event.getScrollDirection()) {
                    case UP:
                        camera.moveAlong(sensitivity * mouseMoveScale * engine.getDeltaTime());
                        break;
                    case DOWN:
                        camera.moveAlong(-sensitivity * mouseMoveScale * engine.getDeltaTime());
                        break;
                    case RIGHT:
                        camera.moveAcross(sensitivity * mouseMoveScale * engine.getDeltaTime(), true);
                        break;
                    case LEFT:
                        camera.moveAcross(-sensitivity * mouseMoveScale * engine.getDeltaTime(), true);
                        break;
                }
            }
        }
    }

    @Override
    public boolean listen(EngineEvent event) {
        if (event.getType() == EngineEventType.START) {
            engine = event.getSource();
            display = engine.getDisplay();
            keyboard = engine.getInput().getKeyboard();
        } else if (event.getType() == EngineEventType.LOOP_BEGIN) {
            if (enabled && keyboardEnabled) {
                float delta = sensitivity * engine.getDeltaTime();
                float moveFactor = keyMoveScale * delta;
                float lookFactor = keyLookScale * delta;
                Vector3 camLoc = Pools.Vector3.get();
                Vector3 camDir = Pools.Vector3.get();
                Vector3 worldUp = Pools.Vector3.get().set(Vector3.UNIT_Y);

                if (keyboard.isKeyPressed(mfKey)) {
                    camera.moveAlong(moveFactor);
                }

                if (keyboard.isKeyPressed(mbKey)) {
                    camera.moveAlong(-moveFactor);
                }

                if (keyboard.isKeyPressed(mrKey)) {
                    camera.moveAcross(-moveFactor, true);
                }

                if (keyboard.isKeyPressed(mlKey)) {
                    camera.moveAcross(moveFactor, true);
                }

                if (keyboard.isKeyPressed(luKey)) {
                    float dirDotUp = camDir.set(camera.getDirection()).dot(Vector3.UNIT_Y);
                    float y = display.getCenter().getY() + lookFactor;
                    camera.getWorldCoordinates(display.getCenter().getX(), y, 0f, camLoc);
                    camera.lookAt(camLoc, worldUp.lerp(camera.getUp(), EngineMath.abs(dirDotUp)));
                }

                if (keyboard.isKeyPressed(ldKey)) {
                    float dirDotUp = camDir.set(camera.getDirection()).dot(Vector3.UNIT_Y);
                    float y = display.getCenter().getY() - lookFactor;
                    camera.getWorldCoordinates(display.getCenter().getX(), y, 0f, camLoc);
                    camera.lookAt(camLoc, worldUp.lerp(camera.getUp(), EngineMath.abs(dirDotUp)));
                }

                if (keyboard.isKeyPressed(lrKey)) {
                    float x = display.getCenter().getX() + lookFactor;
                    camera.getWorldCoordinates(x, display.getCenter().getY(), 0f, camLoc);
                    camera.lookAt(camLoc, Vector3.UNIT_Y);
                }

                if (keyboard.isKeyPressed(llKey)) {
                    float x = display.getCenter().getX() - lookFactor;
                    camera.getWorldCoordinates(x, display.getCenter().getY(), 0f, camLoc);
                    camera.lookAt(camLoc, Vector3.UNIT_Y);
                }

                Pools.Vector3.put(camLoc);
                Pools.Vector3.put(camDir);
                Pools.Vector3.put(worldUp);
            }
        }

        return true;
    }
}
