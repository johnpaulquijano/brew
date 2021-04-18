package core.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of frames to be animated.
 *
 * @author John Paul Quijano
 */
public class Animation {
    public enum Type {
        INTERPOLATED, BAKED
    }

    public static final float DEFAULT_SPEED = 1f;
    public static final Type DEFAULT_TYPE = Type.INTERPOLATED;

    private int next;
    private int current;
    private float speed;
    private float quantum;
    private float duration;
    private double startTime;
    private double animationTime;
    private boolean paused;
    private boolean reset;
    private Joint bind;
    private Joint output;
    private List<Frame> keyFrames;
    private List<Frame> bakedFrames;
    private Type type;

    public Animation() {
        type = DEFAULT_TYPE;
        speed = DEFAULT_SPEED;
        keyFrames = new ArrayList<>();
        bakedFrames = new ArrayList<>();

        next = 1;
        current = 0;
    }

    /**
     * Sets the type of animation. INTERPOLATED calculates the current frame to display by interpolating between keyframes.
     * BAKED chooses a pre-calculated frame from a list based on the current animation time.
     *
     * Each animation type has its pros and cons. INTERPOLATED gives smoother and more dynamic results but is more
     * computationally expensive than BAKED. BAKED, although much faster than INTERPOLATED, uses more memory to store
     * the pre-calculated frames.
     *
     * @param type - the animation type
     */
    public void setType(Type type) {
        this.type = type;
        next = 1;
        current = 0;
    }

    /**
     * Gives the type of animation.
     *
     * @return type of animation
     */
    public Type getType() {
        return type;
    }

    /**
     * Adds all the keyframes in the input list to this animation's list of keyframes.
     *
     * @param input - a list of keyframes
     */
    public void addKeyFrames(List<Frame> input) {
        keyFrames.addAll(input);
    }

    /**
     * Adds a keyframe to this animation.
     *
     * @param frame - an animation keyframe
     */
    public void addKeyFrame(Frame frame) {
        keyFrames.add(frame);
    }

    /**
     * Gives the keyframe at the given index.
     *
     * @param index - index of a keyframe
     */
    public Frame getKeyFrame(int index) {
        return keyFrames.get(index);
    }

    /**
     * Removes the given keyframe from this animation.
     *
     * @param frame - keyframe to remove
     */
    public void removeKeyFrame(Frame frame) {
        keyFrames.remove(frame);
    }

    /**
     * Removes the keyframe at the given index from this animation.
     *
     * @param index - index of the keyframe to remove
     */
    public void removeKeyFrame(int index) {
        keyFrames.remove(index);
    }

    /**
     * Removes all keyframes from this animation.
     */
    public void clearKeyFrames() {
        keyFrames.clear();
    }

    /**
     * Gives the number of keyframes.
     */
    public int numKeyFrames() {
        return keyFrames.size();
    }

    /**
     * Gives the number of baked frames.
     */
    public int numBakedFrames() {
        return bakedFrames.size();
    }

    /**
     * Pauses and resumes animation.
     *
     * @param paused - animation is paused if true
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Gives true if animation currently paused.
     *
     * @return true if animation is currently paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the inverse bind pose.
     *
     * @param bind - root of the inverse bind pose
     */
    public void setBind(Joint bind) {
        this.bind = bind;
    }

    /**
     * Gives the bind pose.
     *
     * @return the bind pose
     */
    public Joint getBind() {
        return bind;
    }

    /**
     * Sets the scaling factor for the animation speed. The higher this value the faster the animation.
     *
     * @param speed - animation speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Gives the animation speed.
     *
     * @return animation speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Advances the animation by calculating the current animation time. This value is then used to determine the current
     * frame to be displayed.
     *
     * @param systemTime - engine running duration in seconds
     *
     * @return root of the final transformed pose
     */
    public Joint update(double systemTime, double deltaTime) {
        if (paused) {
            startTime = systemTime - animationTime;

            if (output != null) {
                return output;
            }
        }

        if (reset) {
            next = 1;
            current = 0;
            startTime = systemTime;
            reset = false;
        }

        animationTime = systemTime - startTime;
        duration = keyFrames.get(keyFrames.size() - 1).getTime();

        float time = (float) animationTime * speed;

        if (type == Type.INTERPOLATED) {
            if (time >= duration) {
                reset = true;
            } else if (time >= keyFrames.get(next).getTime()) {
                current = next;
                next++;
            }

            Frame c = keyFrames.get(current);
            Frame n = keyFrames.get(next);

            float delta = (time - c.getTime()) / (n.getTime() - c.getTime());

            output = c.getPose().interpolate(n.getPose(), output, delta).transform(bind).resolve();
        } else if (type == Type.BAKED) {
            current = (int) (time / quantum) % bakedFrames.size();

            if (current >= bakedFrames.size()) {
                reset = true;
            }

            output = bakedFrames.get(current).getPose();
        }

        return output;
    }

    /**
     * Creates a list of pre-calculated frames where the frame to display is selected based on the calculated animation time.
     *
     * @param frames - the number of frames to create
     */
    public void bake(int frames) {
        bakedFrames.clear();

        duration = keyFrames.get(keyFrames.size() - 1).getTime();

        int currFrame = 0;
        int nextFrame = 1;
        quantum = duration / frames;

        for (int i = 0; i < frames; i++) {
            float time = quantum * i;
            Frame frame = new Frame();
            Joint pose = null;

            if (time >= keyFrames.get(nextFrame).getTime()) {
                currFrame++;
                nextFrame++;
            }

            Frame c = keyFrames.get(currFrame);
            Frame n = keyFrames.get(nextFrame);

            float d = n.getTime() - c.getTime();
            float delta = (time - c.getTime()) / d;

            pose = c.getPose().interpolate(n.getPose(), pose, delta).transform(bind).resolve();

            frame.setTime(time);
            frame.setPose(pose);

            bakedFrames.add(frame);
        }
    }
}
