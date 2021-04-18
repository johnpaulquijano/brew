package core;

/**
 * Base class for objects residing in the graphics context.
 *
 * @author John Paul Quijano
 */
public abstract class GraphicsObject {
    protected int id;

    /**
     * Checks if this object's graphics context representation has been built.
     *
     * @return true if this object's graphics context representation has been built
     */
    public boolean isBuilt() {
        return id >= 0;
    }

    /**
     * Gives the unique identifier for this graphics object.
     *
     * @return unique identifier for this graphics object
     */
    public int getID() {
        return id;
    }

    /**
     * Creates an instance of this object in the graphics context.
     */
    protected abstract void build();

    /**
     * Deallocates this object's instance from the graphics context.
     */
    protected abstract void destroy();
}
