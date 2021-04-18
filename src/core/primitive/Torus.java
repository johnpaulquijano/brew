package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.utility.EngineException;

/**
 * A doughnut-shaped surface.
 *
 * @author John Paul Quijano
 */
public class Torus extends Geometry {
    public static final int DEFAULT_RAD_SAMPLES = 64;
    public static final int DEFAULT_CON_SAMPLES = 32;
    public static final float DEFAULT_TUBE_RADIUS = 0.5f;
    public static final float DEFAULT_HOLE_RADIUS = 0.5f;

    private int radSamples;
    private int conSamples;
    private float tubeRadius;
    private float holeRadius;

    /**
     * Constructs a torus with default parameters.
     */
    public Torus() {
        super(Type.TRIS, 2048, 12288);

        radSamples = DEFAULT_RAD_SAMPLES;
        conSamples = DEFAULT_CON_SAMPLES;
        tubeRadius = DEFAULT_TUBE_RADIUS;
        holeRadius = DEFAULT_HOLE_RADIUS;

        updateTorus();
    }

    /**
     * Constructs a torus with the given parameters.
     *
     * @param radSamples - radial subdivisions
     * @param conSamples - concentric subdivisions
     * @param tubeRadius - radius of the tube forming the ring
     * @param holeRadius - radius of this torus's hole
     */
    public Torus(int radSamples, int conSamples, float tubeRadius, float holeRadius) {
        super(Type.TRIS, radSamples * conSamples, radSamples * conSamples * 6);

        this.radSamples = radSamples;
        this.conSamples = conSamples;
        this.tubeRadius = tubeRadius;
        this.holeRadius = holeRadius;

        updateTorus();
    }

    /**
     * Sets the radius of the tube forming the ring.
     *
     * @param radius - tube radius to set
     */
    public void setTubeRadius(float radius) {
        tubeRadius = radius;
    }

    /**
     * Returns the radius of the tube.
     *
     * @return radius of the tube
     */
    public float getTubeRadius() {
        return tubeRadius;
    }

    /**
     * Sets the radius of the ring.
     *
     * @param radius - hole radius to set
     */
    public void setHoleRadius(float radius) {
        holeRadius = radius;
    }

    /**
     * Returns the radius of the hole.
     *
     * @return radius of the hole
     */
    public float getHoleRadius() {
        return holeRadius;
    }

    /**
     * Sets the number of radial subdivisions.
     *
     * @param samples - radial samples to set
     */
    public void setRadialSamples(int samples) {
        radSamples = samples;
    }

    /**
     * Returns the number of radial subdivisions.
     *
     * @return number of radial subdivisions
     */
    public int getRadialSamples() {
        return radSamples;
    }

    /**
     * Sets the number of concentric subdivisions.
     *
     * @param samples - concentric samples to set
     */
    public void setConcentricSamples(int samples) {
        conSamples = samples;
    }

    /**
     * Returns the number of concentric subdivisions.
     *
     * @return number of concentric subdivisions
     */
    public int getConcentricSamples() {
        return conSamples;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updateTorus() {
        createTorus(radSamples, conSamples, tubeRadius, holeRadius);
    }

    /**
     * Programmatically creates this torus' geometry.
     *
     * @param radSamples - radial subdivisions
     * @param conSamples - concentric subdivisions
     * @param tubeRadius - radius of the tube forming the ring
     * @param holeRadius - radius of this torus's hole
     */
    protected void createTorus(int radSamples, int conSamples, float tubeRadius, float holeRadius) {
        if (radSamples < 3 || conSamples < 3) {
            throw new EngineException("Radial and concentric samples must not be less than 3.");
        }

        int index = 0;
        int quadIndex;
        float radZ;
        float radX;
        float conY;
        float conX;
        float radAngle;
        float conAngle;

        for (int i = 0; i < conSamples; i++) //setup coordinates
        {
            radAngle = (i * EngineMath.TWO_PI / conSamples);
            radZ = EngineMath.sin(radAngle) * tubeRadius;
            radX = EngineMath.cos(radAngle) * tubeRadius + tubeRadius + holeRadius;

            for (int j = 0; j < radSamples; j++) {
                conAngle = (j * EngineMath.TWO_PI / radSamples);
                conX = EngineMath.sin(conAngle) * radX;
                conY = EngineMath.cos(conAngle) * radX;

                setCoordinate(index++, conX, conY, radZ);
            }
        }

        index = 0;

        for (int i = 0; i < conSamples; i++) //setup connectivity
        {
            for (int j = 0; j < radSamples; j++) {
                quadIndex = j + i * radSamples;

                if (i == conSamples - 1) //last concentric strip
                {
                    if (j == radSamples - 1) //last radial quad
                    {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, j);
                        setIndex(index++, 0);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, 0);
                        setIndex(index++, quadIndex + 1 - radSamples);
                    } else {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, j);
                        setIndex(index++, j + 1);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, j + 1);
                        setIndex(index++, quadIndex + 1);
                    }
                } else {
                    if (j == radSamples - 1) //last radial quad
                    {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + radSamples);
                        setIndex(index++, quadIndex + 1);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1);
                        setIndex(index++, quadIndex + 1 - radSamples);
                    } else {
                        //first triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + radSamples);
                        setIndex(index++, quadIndex + radSamples + 1);

                        //second triangle forming quad
                        setIndex(index++, quadIndex);
                        setIndex(index++, quadIndex + 1 + radSamples);
                        setIndex(index++, quadIndex + 1);
                    }
                }
            }
        }
    }
}
