package core.primitive;

import core.Geometry;

/**
 * A polyhedron with six sides. A box can have repeated vertices at each corner for sharp edges or a single vertex at
 * each corner for interpolated color at the edges when shaded.
 *
 * @author John Paul Quijano
 */
public class Box extends Geometry {
    public static final float DEFAULT_WIDTH = 1f;
    public static final float DEFAULT_HEIGHT = 1f;
    public static final float DEFAULT_LENGTH = 1f;

    private float width;
    private float height;
    private float length;
    private boolean merge;

    /**
     * Constructs a box with default parameters.
     */
    public Box() {
        super(Type.TRIS, 24, 36);

        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        length = DEFAULT_LENGTH;
        merge = false;

        updateBox();
    }

    /**
     * Constructs a box with the given parameters.
     *
     * @param width  - x-axis dimension
     * @param height - y-axis dimension
     * @param length - z-axis dimension
     * @param merge  - if set, corners are joined by a single vertex
     */
    public Box(float width, float height, float length, boolean merge) {
        super(Type.TRIS, merge ? 8 : 24, 36);

        this.width = width;
        this.length = length;
        this.height = height;
        this.merge = merge;

        updateBox();
    }

    /**
     * Sets this box's width.
     *
     * @param width - the width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * Returns this box's width.
     *
     * @return this box's width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets this box's height.
     *
     * @param height - the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns this box's height.
     *
     * @return this box's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets this box's length.
     *
     * @param length - the length to set
     */
    public void setLength(float length) {
        this.length = length;
    }

    /**
     * Returns this box's length.
     *
     * @return this box's length
     */
    public float getLength() {
        return length;
    }

    /**
     * If set, each corner is built with a single vertex.
     *
     * @param merge - if true, each corner is built with a single vertex
     */
    public void setMerged(boolean merge) {
        this.merge = merge;
    }

    /**
     * Returns whether or not this box's corners are merged.
     *
     * @return true if this box's corners are merged
     */
    public boolean isMerged() {
        return merge;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updateBox() {
        createBox(width, height, length, merge);
    }

    /**
     * Programmatically creates this box's geometry.
     *
     * @param width  - x-axis dimension
     * @param height - y-axis dimension
     * @param length - z-axis dimension
     * @param merge  - if set, corners are joined by a single vertex
     */
    protected void createBox(float width, float height, float length, boolean merge) {
        int index = 0;
        int quadIndex = 0;
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float halfLength = length / 2f;

        if (merge) {
            setCoordinate(0, halfWidth, halfHeight, halfLength);
            setCoordinate(1, halfWidth, halfHeight, -halfLength);
            setCoordinate(2, -halfWidth, halfHeight, -halfLength);
            setCoordinate(3, -halfWidth, halfHeight, halfLength);
            setCoordinate(4, halfWidth, -halfHeight, halfLength);
            setCoordinate(5, halfWidth, -halfHeight, -halfLength);
            setCoordinate(6, -halfWidth, -halfHeight, -halfLength);
            setCoordinate(7, -halfWidth, -halfHeight, halfLength);

            for (int i = 0; i < 4; i++) //setup connectivity
            {
                if (i == 3) {
                    //first triangle forming quad
                    setIndex(index++, quadIndex);
                    setIndex(index++, quadIndex + 4);
                    setIndex(index++, quadIndex - 3);

                    //second triangle forming quad
                    setIndex(index++, quadIndex - 3);
                    setIndex(index++, quadIndex + 4);
                    setIndex(index++, quadIndex + 1);
                } else {
                    //first triangle forming quad
                    setIndex(index++, quadIndex);
                    setIndex(index++, quadIndex + 4);
                    setIndex(index++, quadIndex + 1);

                    //second triangle forming quad
                    setIndex(index++, quadIndex + 1);
                    setIndex(index++, quadIndex + 4);
                    setIndex(index++, quadIndex + 5);
                }

                quadIndex++;
            }

            //top face
            setIndex(index++, 0);
            setIndex(index++, 1);
            setIndex(index++, 3);
            setIndex(index++, 1);
            setIndex(index++, 2);
            setIndex(index++, 3);

            //bottom face
            setIndex(index++, 4);
            setIndex(index++, 7);
            setIndex(index++, 6);
            setIndex(index++, 4);
            setIndex(index++, 6);
            setIndex(index, 5);
        } else {
            //front
            setCoordinate(0, halfWidth, halfHeight, halfLength);
            setCoordinate(1, -halfWidth, halfHeight, halfLength);
            setCoordinate(2, -halfWidth, -halfHeight, halfLength);
            setCoordinate(3, halfWidth, -halfHeight, halfLength);

            //right
            setCoordinate(4, halfWidth, halfHeight, -halfLength);
            setCoordinate(5, halfWidth, halfHeight, halfLength);
            setCoordinate(6, halfWidth, -halfHeight, halfLength);
            setCoordinate(7, halfWidth, -halfHeight, -halfLength);

            //back
            setCoordinate(8, -halfWidth, halfHeight, -halfLength);
            setCoordinate(9, halfWidth, halfHeight, -halfLength);
            setCoordinate(10, halfWidth, -halfHeight, -halfLength);
            setCoordinate(11, -halfWidth, -halfHeight, -halfLength);

            //left
            setCoordinate(12, -halfWidth, halfHeight, halfLength);
            setCoordinate(13, -halfWidth, halfHeight, -halfLength);
            setCoordinate(14, -halfWidth, -halfHeight, -halfLength);
            setCoordinate(15, -halfWidth, -halfHeight, halfLength);

            //top
            setCoordinate(16, halfWidth, halfHeight, -halfLength);
            setCoordinate(17, -halfWidth, halfHeight, -halfLength);
            setCoordinate(18, -halfWidth, halfHeight, halfLength);
            setCoordinate(19, halfWidth, halfHeight, halfLength);

            //bottom
            setCoordinate(20, halfWidth, -halfHeight, halfLength);
            setCoordinate(21, -halfWidth, -halfHeight, halfLength);
            setCoordinate(22, -halfWidth, -halfHeight, -halfLength);
            setCoordinate(23, halfWidth, -halfHeight, -halfLength);

            for (int i = 0; i < 6; i++) //setup connectivity
            {
                setIndex(index++, quadIndex);
                setIndex(index++, quadIndex + 2);
                setIndex(index++, quadIndex + 3);

                setIndex(index++, quadIndex);
                setIndex(index++, quadIndex + 1);
                setIndex(index++, quadIndex + 2);

                quadIndex += 4;
            }
        }
    }
}
