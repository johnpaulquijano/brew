package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.utility.EngineException;

/**
 * A closed surface which vertices are equidistant from the center.
 *
 * @author John Paul Quijano
 */
public class Sphere extends Geometry {
    public static final int DEFAULT_STACKS = 32;
    public static final int DEFAULT_SLICES = 64;
    public static final float DEFAULT_RADIUS = 1f;

    private int stacks;
    private int slices;
    private float radius;

    /**
     * Constructs a sphere with default parameters.
     */
    public Sphere() {
        super(Type.TRIS, 2048, 11904);

        stacks = DEFAULT_STACKS;
        slices = DEFAULT_SLICES;
        radius = DEFAULT_RADIUS;

        updateSphere();
    }

    /**
     * Constructs a sphere with the given parameters.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param radius - radius of the sphere
     */
    public Sphere(int stacks, int slices, float radius) {
        super(Type.TRIS, stacks * slices, (stacks - 2) * slices * 6 + slices * 6);

        this.stacks = stacks;
        this.slices = slices;
        this.radius = radius;

        updateSphere();
    }

    /**
     * Sets this sphere's radius.
     *
     * @param radius - the radius to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns this sphere's radius.
     *
     * @return this sphere's radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets this sphere's horizontal divisions.
     *
     * @param stacks - the stacks to set
     */
    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    /**
     * Returns this sphere's horizontal divisions.
     *
     * @return this sphere's horizontal divisions
     */
    public int getStacks() {
        return stacks;
    }

    /**
     * Sets this sphere's vertical divisions.
     *
     * @param slices - the slices to set
     */
    public void setSlices(int slices) {
        this.slices = slices;
    }

    /**
     * Returns this sphere's vertical divisions.
     *
     * @return this sphere's vertical divisions
     */
    public int getSlices() {
        return slices;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updateSphere() {
        createSphere(stacks, slices, radius);
    }

    /**
     * Programmatically creates this sphere's geometry.
     *
     * @param stacks - horizontal subdivisions
     * @param slices - vertical subdivisions
     * @param radius - radius of the sphere
     */
    protected void createSphere(int stacks, int slices, float radius) {
        if (stacks < 2 || slices < 3) {
            throw new EngineException("Stacks must not be less than 2 and slices must not be less than 3.");
        }

        float coordX, coordY, coordZ;
        float sliceAngle, stackAngle, sliceRadius;
        int index = 1;
        int quadIndex = 1;
        int vertexCount = numCoordinates();

        if (slices % 2 == 0) {
            vertexCount -= 2;
        } else {
            vertexCount -= 1;
        }

        for (int i = 0; i < stacks + 1; i++) //setup coordinates
        {
            stackAngle = (i * EngineMath.PI / stacks);
            sliceRadius = EngineMath.sin(stackAngle) * radius;
            coordY = EngineMath.cos(stackAngle) * radius;

            if (i == 0) {
                setCoordinate(0, sliceRadius, coordY, 0f);
            } else if (i == stacks) { //bottom point
                setCoordinate(vertexCount - 1, sliceRadius, coordY, 0f);
            } else if (i > 0 && i < stacks) { //stack points
                for (int j = 0; j < slices; j++) {
                    sliceAngle = (j * EngineMath.TWO_PI / slices);
                    coordX = EngineMath.cos(sliceAngle) * sliceRadius;
                    coordZ = EngineMath.sin(sliceAngle) * sliceRadius;

                    setCoordinate(index++, coordX, coordY, coordZ);
                }
            }
        }

        index = 0;

        for (int i = 0; i < stacks + 1; i++) //setup connectivity
        {
            for (int j = 0; j < slices; j++) {
                if (i == 0) //top cap
                {
                    if (j == slices - 1) {
                        setIndex(index++, 0);
                        setIndex(index++, 1);
                        setIndex(index++, j + 1);
                    } else {
                        setIndex(index++, 0);
                        setIndex(index++, j + 2);
                        setIndex(index++, j + 1);
                    }
                } else if (i == stacks) //bottom cap
                {
                    if (j == slices - 1) {
                        if (slices % 2 == 0) {
                            setIndex(index++, vertexCount - 1);
                            setIndex(index++, vertexCount - (slices - 2));
                            setIndex(index++, vertexCount - slices - (j - 2));
                        } else {
                            setIndex(index++, vertexCount - 1);
                            setIndex(index++, vertexCount - (slices - 1));
                            setIndex(index++, vertexCount - slices - (j - 1));
                        }
                    } else {
                        if (slices % 2 == 0) {
                            setIndex(index++, vertexCount - 1);
                            setIndex(index++, vertexCount - slices - (j - 1));
                            setIndex(index++, vertexCount - slices - (j - 2));
                        } else {
                            setIndex(index++, vertexCount - 1);
                            setIndex(index++, vertexCount - slices - j);
                            setIndex(index++, vertexCount - slices - (j - 1));
                        }
                    }
                } else if (i > 0 && i < stacks - 1) //stacks
                {
                    //first triangle forming quad
                    if (j == slices - 1) {
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex - slices + 1);
                        setIndex(index++, quadIndex + 1);
                    } else {
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + 1 + slices);
                    }

                    //second triangle forming quad
                    if (j == slices - 1) {
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + slices);
                    } else {
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1 + slices);
                        setIndex(index++, quadIndex + slices);
                    }

                    quadIndex++;
                }
            }
        }
    }
}
