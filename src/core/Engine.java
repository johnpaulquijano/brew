package core;

import core.event.EngineEvent;
import core.event.listener.EngineListener;
import core.event.type.EngineEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the main application loop.
 *
 * @author John Paul Quijano
 */
public final class Engine {
    private int fps;
    private int numFrames;
    private int fpsMax;
    private float delta;
    private long currTime;
    private long prevTime;
    private long prevFPSTime;
    private long nanoDeltaTime;
    private long prevDeltaTime;
    private long targetDeltaTime;
    private boolean paused;
    private boolean stopped;
    private boolean calcFPSEnabled;
    private Input input;
    private Display display;
    private Renderer renderer;
    private EngineEvent event;
    private List<EngineListener> listeners;

    private static final Engine instance = new Engine();

    private Engine() {
        input = new Input();
        display = new Display(input);
        renderer = new Renderer();
        event = new EngineEvent(this);
        listeners = new ArrayList<>();

        stopped = true;
    }

    public static Engine getInstance() {
        return instance;
    }

    /**
     * Gives the input devices wrapper.
     *
     * @return input object
     */
    public Input getInput() {
        return input;
    }

    /**
     * Gives the native display.
     *
     * @return native display
     */
    public Display getDisplay() {
        return display;
    }

    /**
     * Gives the renderer.
     *
     * @return renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Gives the FPS cap.
     *
     * @return maximum frame rate
     */
    public int getMaxFPS() {
        return fpsMax;
    }

    /**
     * Limits the frames rendered per second to the given maximum. A value of 0 means fps is uncapped which is the default.
     *
     * @param max - maximum frames per second
     */
    public void setMaxFPS(int max) {
        fpsMax = max;

        if (max > 0) {
            targetDeltaTime = 1000000000 / fpsMax;
        }
    }

    /**
     * Gives the last calculated number of rendered frames per second (FPS).
     *
     * @return last calculated number of rendered frames per second (FPS)
     */
    public int getFPS() {
        return fps;
    }

    /**
     * Gives the last calculated delta time. Delta time is the time difference between the last frame and the current frame.
     *
     * @return last calculated delta time
     */
    public float getDeltaTime() {
        return delta;
    }

    /**
     * Gives this engine's running time in seconds.
     *
     * @return engine's running time in seconds
     */
    public double getTime() {
        return display.getTime();
    }

    /**
     * If set, the number of frames rendered per second is calculated.
     *
     * @param enabled - true to enable calculation of FPS
     */
    public void setCalculateFPSEnabled(boolean enabled) {
        if (!enabled) {
            fps = 0;
        }

        calcFPSEnabled = enabled;
    }

    /**
     * Gives the FPS calculation state.
     *
     * @return true if FPS is calculated
     */
    public boolean isCalculateFPSEnabled() {
        return calcFPSEnabled;
    }

    /**
     * Adds a listener for events fired by this engine.
     *
     * @param listener - listener to add
     */
    public void addListener(EngineListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Gives the listener at the given index.
     *
     * @param index - index of the listener
     *
     * @return engine listener
     */
    public EngineListener getListener(int index) {
        return listeners.get(index);
    }

    /**
     * Gives the listener at the given index.
     *
     * @param listener - listener to remove
     */
    public void removeListener(EngineListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gives this engine's list of listeners.
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

    /**
     * Skips all rendering and other tasks in the application loop.
     */
    public void pause(boolean pause) {
        paused = pause;
    }

    /**
     * Terminates engine execution.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Starts engine execution.
     */
    public void start() {
        if (stopped) {
            GL.init();
            renderer.init();

            prevTime = System.nanoTime();
            stopped = false;

            event.setType(EngineEventType.START);

            for (EngineListener listener : listeners) {
                listener.listen(event);
            }

            while (!stopped) {
                event.setType(EngineEventType.LOOP_BEGIN);

                for (EngineListener listener : listeners) {
                    listener.listen(event);
                }

                calculateFPS();
                regulateFPS();

                display.update();

                if (display.isResized()) {
                    renderer.resize(display.getWidth(), display.getHeight());
                }

                if (display.isReset()) {
                    display.restore();
                    renderer.restore();
                    GL.restore();
                }

                if (display.isCloseRequested() || stopped) {
                    event.setType(EngineEventType.STOP_REQUESTED);

                    for (EngineListener listener : listeners) {
                        stopped |= listener.listen(event);
                    }

                    if (stopped) {
                        break;
                    }
                }

                if (paused) {
                    event.setType(EngineEventType.PAUSE);

                    for (EngineListener listener : listeners) {
                        listener.listen(event);
                    }

                    display.swap();

                    continue;
                }

                renderer.render();
                display.swap();

                event.setType(EngineEventType.LOOP_END);

                for (EngineListener listener : listeners) {
                    listener.listen(event);
                }
            }

            event.setType(EngineEventType.STOP);

            for (EngineListener listener : listeners) {
                listener.listen(event);
            }

            GL.free();
            renderer.free();
            display.free();
        }
    }

    /**
     * Calculates delta time and number of rendered frames per second (FPS).
     */
    private void calculateFPS() {
        currTime = System.nanoTime();

        nanoDeltaTime = currTime - prevTime;
        delta = nanoDeltaTime / 1000000f;
        prevTime = currTime;

        if (calcFPSEnabled) {
            if (currTime - prevFPSTime > 1000000000) {
                fps = numFrames;
                numFrames = 0;
                prevFPSTime = currTime;
            }

            numFrames++;
        }
    }

    /**
     * Keeps the FPS near the set maximum. The accuracy of this method depends on the accuracy of the underlying timing system.
     */
    private void regulateFPS() {
        if (fpsMax <= 0 || nanoDeltaTime > targetDeltaTime) {
            return;
        }

        try {
            long sleepTime = (targetDeltaTime - nanoDeltaTime) + (targetDeltaTime - prevDeltaTime);
            long nanoTime = System.nanoTime();

            while (System.nanoTime() - nanoTime < sleepTime) {
                Thread.sleep(1);
            }

            prevDeltaTime = nanoDeltaTime;
        } catch (InterruptedException e) {
            //ignore interrupt
        }
    }
}
