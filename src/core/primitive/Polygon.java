package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.utility.EngineException;

/**
 * A closed plane figure that can have arbitrary number of sides.
 *
 * @author John Paul Quijano
 */
public class Polygon extends Geometry {
    public static final int DEFAULT_SIDES = 4;
    public static final float DEFAULT_RADIUS = 1f;

    private int sides;
    private float radius;
    private boolean radial;

    /**
     * Constructs a polygon with default parameters.
     */
    public Polygon() {
        super(Type.TRIS, 4, 6);

        sides = DEFAULT_SIDES;
        radius = DEFAULT_RADIUS;
        radial = false;

        updatePolygon();
    }

    /**
     * Constructs a polygon with the given parameters.
     *
     * @param sides  - number of sides
     * @param radius - dimension from the center of this polygon to one of the edge vertices
     * @param radial - if true, all edge vertices are connected to a central vertex
     */
    public Polygon(int sides, float radius, boolean radial) {
        super(Type.TRIS, radial ? sides + 1 : sides, radial ? sides * 3 : (sides - 2) * 3);

        this.sides = sides;
        this.radius = radius;
        this.radial = radial;

        updatePolygon();
    }

    /**
     * Sets this polygon's radius.
     *
     * @param radius - the radius to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns this polygon's radius.
     *
     * @return this polygon's radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets this polygon's number of sides.
     *
     * @param side - the side to set
     */
    public void setSides(int side) {
        this.sides = side;
    }

    /**
     * Returns this polygon's number of sides.
     *
     * @return this polygon's number of sides
     */
    public int getSides() {
        return sides;
    }

    /**
     * If set, all edge vertices are connected to a central vertex.
     *
     * @param radial - radial
     */
    public void setRadial(boolean radial) {
        this.radial = radial;
    }

    /**
     * Returns whether or not edge vertices are connected to a central vertex.
     *
     * @return true if edge vertices are connected to a central vertex
     */
    public boolean getRadial() {
        return radial;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updatePolygon() {
        createPolygon(sides, radius, radial);
    }

    /**
     * Programmatically creates this polygon's geometry.
     *
     * @param sides  - number of sides
     * @param radius - dimension from the center of this polygon to one of the edge vertices
     * @param radial - if true, all edge vertices are connected to a central vertex
     */
    protected void createPolygon(int sides, float radius, boolean radial) {
        if (sides < 3) {
            throw new EngineException("Sides must not be less than 3.");
        }

        float x;
        float y;
        float angle;
        int index = 0;

        if (radial) {
            setCoordinate(0, 0f, 0f, 0f);

            for (int i = 1; i < sides + 1; i++) //setup coordinates
            {
                angle = (i * EngineMath.TWO_PI / sides);
                x = EngineMath.cos(angle) * radius;
                y = EngineMath.sin(angle) * radius;

                setCoordinate(i, x, y, 0f);
            }

            for (int i = 1; i < sides; i++) //setup connectivity
            {
                setIndex(index++, 0);
                setIndex(index++, i);
                setIndex(index++, i + 1);
            }

            setIndex(index++, 0);
            setIndex(index++, sides);
            setIndex(index++, 1);
        } else {
            for (int i = 0; i < sides; i++) //setup coordinates
            {
                angle = (i * EngineMath.TWO_PI / sides);
                x = EngineMath.cos(angle) * radius;
                y = EngineMath.sin(angle) * radius;

                setCoordinate(i, x, y, 0f);
            }

            for (int i = 1; i < sides - 1; i++) //setup connectivity
            {
                setIndex(index++, 0);
                setIndex(index++, i);
                setIndex(index++, i + 1);
            }
        }
    }
}
