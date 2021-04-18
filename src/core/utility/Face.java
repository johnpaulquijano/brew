package core.utility;

import core.Camera;
import core.math.Vector3;

/**
 * Face represents a polygon which is the building block of a geometry.
 *
 * @author John Paul Quijano
 */
public class Face implements Comparable<Face> {
    private int[] indices;
    private float camDistance;
    private boolean normalDirty;
    private boolean centerDirty;
    private Vector3 normal;
    private Vector3 center;
    private Vector3[] vertices;

    public Face() {
        indices = new int[3];
        vertices = new Vector3[3];
        normal = new Vector3();
        center = new Vector3();

        for (int i = 0; i < 3; i++) {
            vertices[i] = new Vector3();
        }

        normalDirty = true;
        centerDirty = true;
    }

    /**
     * Sets this face's vertex and vertex index.
     *
     * @param index - buffer index
     * @param input - the vertex to set
     */
    public void setVertex(int index, Vector3 input) {
        vertices[index].set(input);
        normalDirty = true;
        centerDirty = true;
    }

    /**
     * Sets this face's vertices.
     *
     * @param vertex0 - first vertex to set
     * @param vertex1 - second vertex to set
     * @param vertex2 - third vertex to set
     */
    public void setVertices(Vector3 vertex0, Vector3 vertex1, Vector3 vertex2) {
        vertices[0].set(vertex0);
        vertices[1].set(vertex1);
        vertices[2].set(vertex2);
        normalDirty = true;
        centerDirty = true;
    }

    /**
     * Gives the vertex at the given index.
     *
     * @param index - buffer index of the vertex
     *
     * @return vertex at the given index
     */
    public Vector3 getVertex(int index) {
        return vertices[index];
    }

    /**
     * Sets the vertex index at the given buffer index.
     *
     * @param i - buffer index
     * @param index - vertex index
     */
    public void setIndex(int i, int index) {
        indices[i] = index;
    }

    /**
     * Sets this face's vertex indices.
     *
     * @param index0 - first vertex index
     * @param index1 - second vertex index
     * @param index2 - third vertex index
     */
    public void setIndices(int index0, int index1, int index2) {
        indices[0] = index0;
        indices[1] = index1;
        indices[2] = index2;
    }

    /**
     * Gives the vertex index at the given buffer index.
     *
     * @param i - buffer index
     *
     * @return vertex index at the given buffer index
     */
    public int getIndex(int i) {
        return indices[i];
    }

    /**
     * Gives the centroid of this face.
     *
     * @return centroid of this face
     */
    public Vector3 getCenter() {
        calculateCenter();
        return center;
    }

    /**
     * Gives the normal vector of this face.
     *
     * @return normal vector of this face
     */
    public Vector3 getNormal() {
        calculateNormal();
        return normal;
    }

    /**
     * Calculates the squared distance between this face's centroid and the given camera's location. This method is used
     * internally for depth sorting translucent faces.
     *
     * @param camera - camera to calculate distance from
     */
    public float calculateDistance(Camera camera) {
        return camDistance = center.distanceSquared(camera.getLocation());
    }

    /**
     * Calculates this face's center.
     */
    public void calculateCenter() {
        if (centerDirty) {
            center.set(vertices[0]).add(vertices[1]).add(vertices[2]).divide(3f);
            centerDirty = false;
        }
    }

    /**
     * Calculates this face's normal.
     */
    public void calculateNormal() {
        if (normalDirty) {
            normal.set(vertices[1]).subtract(vertices[0]).cross(vertices[2].getX() - vertices[0].getX(), vertices[2].getY() - vertices[0].getY(), vertices[2].getZ() - vertices[0].getZ());
            normal.normalize();
            normalDirty = false;
        }
    }

    /**
     * Field-for-field equality check.
     *
     * @return true if the given face equals this face field-for-field.
     */
    public boolean equals(Face input) {
        return vertices[0].equals(input.vertices[0])
                && vertices[1].equals(input.vertices[1])
                && vertices[2].equals(input.vertices[2])
                && indices[0] == input.indices[0]
                && indices[1] == input.indices[1]
                && indices[2] == input.indices[2];
    }

    @Override
    public String toString() {
        return "Vertices: " + vertices[0] + ", " + vertices[1] + ", " + vertices[2] + "\n"
                + "Indices: " + indices[0] + ", " + indices[1] + ", " + indices[2];
    }

    @Override
    public int compareTo(Face face) {
        if (camDistance > face.camDistance) {
            return 1;
        }

        if (camDistance < face.camDistance) {
            return -1;
        }

        return 0;
    }
}
