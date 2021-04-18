package core.primitive;

import core.Geometry;
import core.math.EngineMath;
import core.math.Vector3;
import core.utility.EngineException;

import java.util.ArrayList;

/**
 * A sphere constructed by subdividing an icosahedron.
 *
 * @author John Paul Quijano
 */
public class GeoSphere extends Geometry {
    public static final float DEFAULT_RADIUS = 1f;
    public static final int DEFAULT_SUBDIVISIONS = 3;

    private static ArrayList<Vector3> vertices = new ArrayList<>();
    private static ArrayList<Integer> genIndices = new ArrayList<>();
    private static ArrayList<Integer> newIndices = new ArrayList<>();
    private float radius;
    private int subdivisions;

    /**
     * Constructs a geodesic sphere with default parameters.
     */
    public GeoSphere() {
        super(Type.TRIS, 0, 0);

        radius = DEFAULT_RADIUS;
        subdivisions = DEFAULT_SUBDIVISIONS;

        updateGeoSphere();
    }

    /**
     * Constructs a geodesic sphere with the given parameters.
     *
     * @param subdivisions - number of times an icosahedron will be subdivided
     * @param radius - radius of the sphere
     */
    public GeoSphere(int subdivisions, float radius) {
        super(Type.TRIS, 0, 0);

        this.radius = radius;
        this.subdivisions = subdivisions;

        updateGeoSphere();
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
     * Sets the number of times this icosahedron is subdivided.
     *
     * @param subdivisions - the subdivisions to set
     */
    public void setSubdivisions(int subdivisions) {
        this.subdivisions = subdivisions;
    }

    /**
     * Returns the number of times this geodesic sphere is subdivided.
     *
     * @return number of subdivisions
     */
    public int getSubdivisions() {
        return subdivisions;
    }

    /**
     * Updates this geometry. This should be called after calling mutators for the changes to take effect.
     */
    public void updateGeoSphere() {
        createGeoSphere(subdivisions, radius);
    }

    /**
     * Programmatically creates this geo-sphere's geometry.
     *
     * @param subdivisions - number of times an icosahedron will be subdivided
     * @param radius - radius of the sphere
     */
    protected void createGeoSphere(int subdivisions, float radius) {
        if (subdivisions < 0) {
            throw new EngineException("Subdivisions must not be less than 0.");
        }

        genIndices.clear();
        vertices.clear();

        int index;
        float constant = 0.5f * (1f + EngineMath.sqrt(5f));
        float invRoot = 1f / EngineMath.sqrt(1f + constant * constant);
        float u = constant * invRoot;
        float v = invRoot;

        //construct icosahedron for the base approximation
        vertices.add(new Vector3().set(u, v, 0f).normalize().multiply(radius));
        vertices.add(new Vector3().set(-u, v, 0f).normalize().multiply(radius));
        vertices.add(new Vector3().set(u, -v, 0f).normalize().multiply(radius));
        vertices.add(new Vector3().set(-u, -v, 0f).normalize().multiply(radius));
        vertices.add(new Vector3().set(v, 0f, u).normalize().multiply(radius));
        vertices.add(new Vector3().set(v, 0f, -u).normalize().multiply(radius));
        vertices.add(new Vector3().set(-v, 0f, u).normalize().multiply(radius));
        vertices.add(new Vector3().set(-v, 0f, -u).normalize().multiply(radius));
        vertices.add(new Vector3().set(0f, u, v).normalize().multiply(radius));
        vertices.add(new Vector3().set(0f, -u, v).normalize().multiply(radius));
        vertices.add(new Vector3().set(0f, u, -v).normalize().multiply(radius));
        vertices.add(new Vector3().set(0f, -u, -v).normalize().multiply(radius));

        genIndices.add(0);
        genIndices.add(8);
        genIndices.add(4);
        genIndices.add(0);
        genIndices.add(5);
        genIndices.add(10);
        genIndices.add(2);
        genIndices.add(4);
        genIndices.add(9);
        genIndices.add(2);
        genIndices.add(11);
        genIndices.add(5);
        genIndices.add(1);
        genIndices.add(6);
        genIndices.add(8);
        genIndices.add(1);
        genIndices.add(10);
        genIndices.add(7);
        genIndices.add(3);
        genIndices.add(9);
        genIndices.add(6);
        genIndices.add(3);
        genIndices.add(7);
        genIndices.add(11);
        genIndices.add(0);
        genIndices.add(10);
        genIndices.add(8);
        genIndices.add(1);
        genIndices.add(8);
        genIndices.add(10);
        genIndices.add(2);
        genIndices.add(9);
        genIndices.add(11);
        genIndices.add(3);
        genIndices.add(11);
        genIndices.add(9);
        genIndices.add(4);
        genIndices.add(2);
        genIndices.add(0);
        genIndices.add(5);
        genIndices.add(0);
        genIndices.add(2);
        genIndices.add(6);
        genIndices.add(1);
        genIndices.add(3);
        genIndices.add(7);
        genIndices.add(3);
        genIndices.add(1);
        genIndices.add(8);
        genIndices.add(6);
        genIndices.add(4);
        genIndices.add(9);
        genIndices.add(4);
        genIndices.add(6);
        genIndices.add(10);
        genIndices.add(5);
        genIndices.add(7);
        genIndices.add(11);
        genIndices.add(7);
        genIndices.add(5);

        for (int i = 0; i < subdivisions; i++) //subdivide icosahedron
        {
            index = 0;

            newIndices.clear();

            while (index < genIndices.size()) {
                //obtain points of the triangle
                Vector3 _a = vertices.get(genIndices.get(index++));
                Vector3 _b = vertices.get(genIndices.get(index++));
                Vector3 _c = vertices.get(genIndices.get(index++));

                //calculate the midpoint of each side of the triangle then push it outward along the normal
                Vector3 ab = new Vector3().set(_a).add(_b).divide(2f).normalize().multiply(radius);
                Vector3 bc = new Vector3().set(_b).add(_c).divide(2f).normalize().multiply(radius);
                Vector3 ca = new Vector3().set(_c).add(_a).divide(2f).normalize().multiply(radius);

                //check for duplicate coordinates
                int _index = contains(ab);

                if (_index < 0) {
                    vertices.add(ab);
                } else {
                    ab = vertices.get(_index);
                }

                _index = contains(bc);

                if (_index < 0) {
                    vertices.add(bc);
                } else {
                    bc = vertices.get(_index);
                }

                _index = contains(ca);

                if (_index < 0) {
                    vertices.add(ca);
                } else {
                    ca = vertices.get(_index);
                }

                //update connectivity of triangles
                newIndices.add(vertices.indexOf(_a));
                newIndices.add(vertices.indexOf(ab));
                newIndices.add(vertices.indexOf(ca));

                newIndices.add(vertices.indexOf(_b));
                newIndices.add(vertices.indexOf(bc));
                newIndices.add(vertices.indexOf(ab));

                newIndices.add(vertices.indexOf(_c));
                newIndices.add(vertices.indexOf(ca));
                newIndices.add(vertices.indexOf(bc));

                newIndices.add(vertices.indexOf(ab));
                newIndices.add(vertices.indexOf(bc));
                newIndices.add(vertices.indexOf(ca));
            }

            genIndices.clear();
            genIndices.addAll(newIndices);
        }

        Geometry geom = new Geometry(Type.TRIS, vertices.size(), genIndices.size());

        geom.setIndices(genIndices);
        geom.setCoordinates(vertices);

        set(geom);
    }

    private static int contains(Vector3 vec) {
        int index = -1;

        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).equals(vec)) {
                index = i;
                break;
            }
        }

        return index;
    }
}
