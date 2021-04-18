package core.primitive;

import core.Geometry;
import core.utility.EngineException;

/**
 * A quadrilateral.
 *
 * @author John Paul Quijano
 */
public class Plane extends Geometry {
    public static final int DEFAULT_STACKS = 1;
    public static final int DEFAULT_SLICES = 1;
    public static final float DEFAULT_WIDTH = 1f;
    public static final float DEFAULT_HEIGHT = 1f;

    private int slices;
    private int stacks;
    private float width;
    private float height;

    /**
     * Constructs a plane with default parameters.
     */
    public Plane() {
        super(Type.TRIS, 4, 6);

        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        slices = DEFAULT_SLICES;
        stacks = DEFAULT_STACKS;

        updatePlane();
    }

    /**
     * Constructs a plane with the given parameters.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param width  - x-axis dimension
     * @param height - y-axis dimension
     */
    public Plane(int stacks, int slices, float width, float height) {
        super(Type.TRIS, (stacks + 1) * (slices + 1), stacks * slices * 6);

        this.width = width;
        this.height = height;
        this.slices = slices;
        this.stacks = stacks;

        updatePlane();
    }

    /**
     * Sets this plane's width.
     *
     * @param width - the width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * Returns this plane's width.
     *
     * @return this plane's width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets this plane's height.
     *
     * @param height - the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }


    /**
     * Returns this plane's height.
     *
     * @return this plane's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets this plane's horizontal divisions.
     *
     * @param stacks - the stacks to set
     */
    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    /**
     * Returns this plane's horizontal divisions.
     *
     * @return this plane's horizontal divisions
     */
    public int getStacks() {
        return stacks;
    }

    /**
     * Sets this plane's vertical divisions.
     *
     * @param slices - the slices to set
     */
    public void setSlices(int slices) {
        this.slices = slices;
    }

    /**
     * Returns this plane's vertical divisions.
     *
     * @return this plane's vertical divisions
     */
    public int getSlices() {
        return slices;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updatePlane() {
        createPlane(stacks, slices, width, height);
    }

    /**
     * Programmatically creates this plane's geometry.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param width  - x-axis dimension
     * @param height - y-axis dimension
     */
    protected void createPlane(int stacks, int slices, float width, float height) {
        if (stacks < 1 || slices < 1) {
            throw new EngineException("Stacks and slices must not be less than 1.");
        }

        float x;
        float y;
        int index = 0;
        int quadIndex = 0;
        float halfWidth = -width / 2f;
        float halfHeight = height / 2f;
        float unitWidth = width / slices;
        float unitHeight = height / stacks;

        for (int i = 0; i < stacks + 1; i++) //setup coordinates
        {
            y = halfHeight - (unitHeight * i);

            for (int j = 0; j < slices + 1; j++) {
                x = halfWidth + (unitWidth * j);
                setCoordinate(index++, x, y, 0f);
            }
        }

        index = 0;

        for (int i = 0; i < stacks; i++) //setup connectivity
        {
            for (int j = 0; j < slices; j++) {
                //upper triangle
                setIndex(index++, quadIndex);
                setIndex(index++, quadIndex + slices + 1);
                setIndex(index++, quadIndex + 1);

                //lower triangle
                setIndex(index++, quadIndex + 1);
                setIndex(index++, quadIndex + slices + 1);
                setIndex(index++, quadIndex + slices + 2);

                quadIndex++;
            }

            quadIndex++;
        }
    }
}
