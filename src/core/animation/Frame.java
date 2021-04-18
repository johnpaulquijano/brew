package core.animation;

/**
 * Represents an animation frame.
 *
 * @author John Paul Quijano
 */
public class Frame {
    private float time;
    private Joint pose;

    public Frame(){}

    public Frame(float time, Joint pose) {
        this.time = time;
        this.pose = pose;
    }

    /**
     * Sets the time when this frame is applied to the model.
     *
     * @param time - time when this frame is applied to the model
     */
    public void setTime(float time) {
        this.time = time;
    }

    /**
     * Gives time when this frame is applied to the model.
     *
     * @return the time when this frame is appleid to the model
     */
    public float getTime() {
        return time;
    }

    /**
     * Sets the posed joint hierarchy for this frame.
     *
     * @param pose - root node of the joint hierarchy
     */
    public void setPose(Joint pose) {
        this.pose = pose;
    }

    /**
     * Gives the posed joint hierarchy for this frame.
     *
     * @return posed joint hierarchy for this frame
     */
    public Joint getPose() {
        return pose;
    }

    /**
     * Creates a copy of this frame.
     *
     * @return copy of this frame
     */
    public Frame copy() {
        Frame copy = new Frame();

        copy.time = time;
        copy.pose = pose.deepCopy();

        return copy;
    }
}
