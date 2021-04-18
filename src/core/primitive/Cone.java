package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.utility.EngineException;

/**
 * A shape whose base is a circle and whose sides taper up to a point.
 *
 * @author John Paul Quijano
 */
public class Cone extends Geometry {
    public static final int DEFAULT_STACKS = 1;
    public static final int DEFAULT_SLICES = 64;
    public static final float DEFAULT_HEIGHT = 1f;
    public static final float DEFAULT_RADIUS = 1f;

    private int stacks;
    private int slices;
    private float radius;
    private float height;
    private boolean merge;

    /**
     * Constructs a cone with default parameters.
     */
    public Cone() {
        super(Type.TRIS, 128, 384);

        merge = false;
        stacks = DEFAULT_STACKS;
        slices = DEFAULT_SLICES;
        height = DEFAULT_HEIGHT;
        radius = DEFAULT_RADIUS;

        updateCone();
    }

    /**
     * Constructs a cone with the given parameters.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param height - height of the cone
     * @param radius - radius of the base
     * @param merge  - if true, constructs a cone with a single vertex at the tip
     */
    public Cone(int stacks, int slices, float height, float radius, boolean merge) {
        super(Type.TRIS, merge ? slices * stacks + 1 : slices * stacks + slices, merge ? (slices * stacks * 6) - (slices * 3) : slices * stacks * 6);

        this.stacks = stacks;
        this.slices = slices;
        this.height = height;
        this.radius = radius;
        this.merge = merge;

        updateCone();
    }

    /**
     * Sets the radius of this cone's base.
     *
     * @param radius - radius of the cone's base
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns the radius of the cone's base.
     *
     * @return radius of the cone's base
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the distance between the center of the base and the tip.
     *
     * @param height - the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns this cone's height.
     *
     * @return this cone's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets this cone's horizontal divisions.
     *
     * @param stacks - stacks to set
     */
    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    /**
     * Returns the number of horizontal divisions.
     *
     * @return number of horizontal divisions
     */
    public int getStacks() {
        return stacks;
    }

    /**
     * Sets this cone's vertical divisions.
     *
     * @param slices - slices to set
     */
    public void setSlices(int slices) {
        this.slices = slices;
    }

    /**
     * Returns the number of vertical divisions.
     *
     * @return number of vertical divisions
     */
    public int getSlices() {
        return slices;
    }

    /**
     * Reconstructs this shape's geometries to only have one vertex at the tip.
     *
     * @param merge - merge vertices if true
     */
    public void setMerged(boolean merge) {
        this.merge = merge;
    }

    /**
     * Returns whether or not the vertices at the tip are merged.
     *
     * @return true if the vertices at the tip are merged
     */
    public boolean isMerged() {
        return merge;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for
     * the changes to take effect.
     */
    public final void updateCone() {
        createCone(stacks, slices, height, radius, merge);
    }

    /**
     * Programmatically creates this cone's geometry.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param height - height of the cone
     * @param radius - radius of the base
     * @param merge  - if true, constructs a cone with a single vertex at the tip
     */
    protected void createCone(int stacks, int slices, float height, float radius, boolean merge) {
        if (stacks < 1 || slices < 3) {
            throw new EngineException("Stacks must not be less than 1 and slices must not be less than 3.");
        }

        float x;
        float y;
        float z;
        float rad;
        float angle;
        int index = 0;
        int quadIndex;
        float halfHeight = height / 2f;
        float stackRadius = radius / stacks;
        float stackHeight = height / stacks;

        if (merge) //use only one vertex for the tip
        {
            quadIndex = 0;

            //tip of cone
            setCoordinate(index++, 0f, halfHeight, 0f);

            for (int i = 0; i < stacks; i++) //setup the rest of coordinates
            {
                rad = stackRadius * (i + 1);
                y = halfHeight - stackHeight * (i + 1);

                for (int j = 0; j < slices; j++) {
                    angle = (j * EngineMath.TWO_PI / slices);
                    z = EngineMath.sin(angle) * rad;
                    x = EngineMath.cos(angle) * rad;

                    setCoordinate(index++, x, y, z);
                }
            }

            index = 0;

            for (int i = 0; i < slices; i++) //topmost stack
            {
                if (i == slices - 1) {
                    setIndex(index++, 0);
                    setIndex(index++, 1);
                    setIndex(index++, i + 1);
                } else {
                    setIndex(index++, 0);
                    setIndex(index++, i + 2);
                    setIndex(index++, i + 1);
                }
            }

            for (int i = 1; i < stacks; i++) //setup connectivity
            {
                for (int j = 0; j < slices; j++) {
                    if (j == slices - 1) {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + slices);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, (quadIndex + 1) - slices);
                        setIndex(index++, quadIndex + 1);
                    } else {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + slices + 1);
                        setIndex(index++, quadIndex + slices);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + slices + 1);
                    }

                    quadIndex++;
                }
            }
        } else {
            quadIndex = 0;

            for (int i = 0; i < stacks + 1; i++) //setup coordinates
            {
                rad = stackRadius * i;
                y = halfHeight - stackHeight * i;

                for (int j = 0; j < slices; j++) {
                    angle = (j * EngineMath.TWO_PI / slices);
                    z = EngineMath.sin(angle) * rad;
                    x = EngineMath.cos(angle) * rad;

                    setCoordinate(index++, x, y, z);
                }
            }

            index = 0;

            for (int i = 0; i < stacks; i++) //setup connectivity
            {
                for (int j = 0; j < slices; j++) {
                    if (j == slices - 1) {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + slices);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, (quadIndex + 1) - slices);
                        setIndex(index++, quadIndex + 1);
                    } else {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + slices + 1);
                        setIndex(index++, quadIndex + slices);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + slices + 1);
                    }

                    quadIndex++;
                }
            }
        }
    }
}
