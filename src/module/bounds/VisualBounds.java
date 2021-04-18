package module.bounds;

import core.BoundingBox;
import core.Geometry;
import core.Renderer;
import core.math.Vector3;
import core.utility.Colors;

/**
 * Visual representation of an axis-aligned bounding volume.
 *
 * @author John Paul Quijano
 */
public class VisualBounds {
    private Vector3 color;
    private Vector3[] coords;
    private Geometry geometry;

    public VisualBounds() {
        color = new Vector3(Colors.GREEN3);
        coords = new Vector3[8];
        geometry = new Geometry(Geometry.Type.QUADS, 8, 24);

        geometry.setColorEnabled(true);
        geometry.setAllColors(color.getX(), color.getY(), color.getZ(), 1f);

        geometry.setIndex(0, 0);
        geometry.setIndex(1, 1);
        geometry.setIndex(2, 2);
        geometry.setIndex(3, 3);

        geometry.setIndex(4, 3);
        geometry.setIndex(5, 2);
        geometry.setIndex(6, 5);
        geometry.setIndex(7, 4);

        geometry.setIndex(8, 4);
        geometry.setIndex(9, 5);
        geometry.setIndex(10, 6);
        geometry.setIndex(11, 7);

        geometry.setIndex(12, 7);
        geometry.setIndex(13, 6);
        geometry.setIndex(14, 1);
        geometry.setIndex(15, 0);

        geometry.setIndex(16, 7);
        geometry.setIndex(17, 0);
        geometry.setIndex(18, 3);
        geometry.setIndex(19, 4);

        geometry.setIndex(20, 1);
        geometry.setIndex(21, 6);
        geometry.setIndex(22, 5);
        geometry.setIndex(23, 2);

        for (int i = 0; i < 8; i++) {
            coords[i] = new Vector3();
        }
    }

    /**
     * Sets the color of the rendered bounding volume.
     *
     * @param color - a three-component color vector
     */
    public void setColor(Vector3 color) {
        this.color.set(color);
        geometry.setAllColors(color.getX(), color.getY(), color.getZ(), 1f);
    }

    /**
     * Gives the color of the rendered bounding volume.
     *
     * @return - color of the rendered bounding volume
     */
    public Vector3 getColor() {
        return color;
    }

    /**
     * Updates the geometric representation of the bounding volume based on the given boundingBox object.
     *
     * @param boundingBox - source boundingBox object
     */
    public void update(BoundingBox boundingBox) {
        geometry.setCoordinates(boundingBox.getCorners(coords));
        geometry.update();
    }

    /**
     * Builds the geometry of this visual bounds.
     *
     * @param renderer - renderer with the active graphics context
     */
    public void build(Renderer renderer) {
        renderer.build(geometry);
    }

    /**
     * Draws this visual bounds.
     */
    public void draw() {
        geometry.draw();
    }
}
