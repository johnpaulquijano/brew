package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.utility.EngineException;

/**
 * A cylindrical surface.
 *
 * @author John Paul Quijano
 */
public class Cylinder extends Geometry {
    public static final int DEFAULT_STACKS = 1;
    public static final int DEFAULT_SLICES = 64;
    public static final float DEFAULT_HEIGHT = 1f;
    public static final float DEFAULT_RADIUS = 1f;

    private int stacks;
    private int slices;
    private float radius;
    private float height;

    /**
     * Constructs a cylinder with default parameters.
     */
    public Cylinder() {
        super(Type.TRIS, 65, 384);

        stacks = DEFAULT_STACKS;
        slices = DEFAULT_SLICES;
        height = DEFAULT_HEIGHT;
        radius = DEFAULT_RADIUS;

        updateCylinder();
    }

    /**
     * Constructs a cylinder with the given parameters.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param height - height of the cylinder
     * @param radius - radius of the base
     */
    public Cylinder(int stacks, int slices, float height, float radius) {
        super(Type.TRIS, slices * stacks + slices, slices * stacks * 6);

        this.stacks = stacks;
        this.slices = slices;
        this.height = height;
        this.radius = radius;

        updateCylinder();
    }

    /**
     * Sets the radius of this cylinder's bases.
     *
     * @param radius - the radius to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns the radius of this cylinder.
     *
     * @return radius of this cylinder
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the distance between the centers of this cylinder's bases.
     *
     * @param height - the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns the height of this cylinder.
     *
     * @return height of this cylinder
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets this cylinder's horizontal divisions.
     *
     * @param stacks - the stacks to set
     */
    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    /**
     * Returns this cylinder's horizontal divisions.
     *
     * @return this cylinder's horizontal divisions
     */
    public int getStacks() {
        return stacks;
    }

    /**
     * Sets this cylinder's vertical divisions.
     *
     * @param slices - the slices to set
     */
    public void setSlices(int slices) {
        this.slices = slices;
    }

    /**
     * Returns this cylinder's vertical divisions.
     *
     * @return this cylinder's vertical divisions
     */
    public int getSlices() {
        return slices;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updateCylinder() {
        createCylinder(stacks, slices, height, radius);
    }

    /**
     * Programmatically creates this cylinder's geometry.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param height - height of the cylinder
     * @param radius - radius of the base
     */
    protected void createCylinder(int stacks, int slices, float height, float radius) {
        if (stacks < 1 || slices < 3) {
            throw new EngineException("Stacks must not be less than 1 and slices must not be less than 3.");
        }

        float x;
        float y;
        float z;
        float angle;
        int index = 0;
        int quadIndex = 0;
        float halfHeight = height / 2f;
        float stackHeight = height / stacks;

        for (int i = 0; i < stacks + 1; i++) //setup coordinates
        {
            y = halfHeight - stackHeight * i;

            for (int j = 0; j < slices; j++) {
                angle = (j * EngineMath.TWO_PI / slices);
                x = EngineMath.cos(angle) * radius;
                z = EngineMath.sin(angle) * radius;

                setCoordinate(index, x, y, z);

                index++;
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
