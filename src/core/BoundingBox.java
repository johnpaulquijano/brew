package core;

import core.buffer.Vector3Buffer;
import core.math.EngineMath;
import core.math.Matrix3;
import core.math.Plane;
import core.math.Vector3;
import core.utility.EngineException;
import core.utility.Poolable;
import core.utility.Pools;

/**
 * An axis-aligned bounding box.
 *
 * @author John Paul Quijano
 */
public class BoundingBox implements Poolable {
    public static final BoundingBox INFINITE = new BoundingBox();
    public static final BoundingBox ZERO = new BoundingBox(Vector3.ZERO, Vector3.ZERO);

    private boolean dirty;
    private Vector3 center;
    private Vector3 extent;

    /**
     * Constructs a bounding box object centered at the origin and has infinite extents.
     */
    public BoundingBox() {
        center = new Vector3();
        extent = new Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        dirty = true;
    }

    /**
     * Constructs a bounding box object with the given extent and center.
     *
     * @param extent - halved width, height, and length of this bounding box
     * @param center - location of this bounding box
     */
    public BoundingBox(Vector3 extent, Vector3 center) {
        this.center = new Vector3(center);
        this.extent = new Vector3(extent);
        dirty = true;
    }

    /**
     * Sets the center and extent of this bounding box to that of the given template.
     *
     * @param template - bounding box to copy attributes from
     *
     * @return this bounding box
     */
    public BoundingBox set(BoundingBox template) {
        if (template == null) {
            return this;
        }

        return set(template.extent, template.center);
    }

    /**
     * Sets this bounding box extent and center.
     *
     * @param extent - halved width, height, and length of this bounding box
     * @param center - location of this bounding box
     *
     * @return this bounding box
     */
    public BoundingBox set(Vector3 extent, Vector3 center) {
        this.center.set(center);
        this.extent.set(extent);

        dirty = true;

        return this;
    }

    /**
     * Sets the location of this bounding box.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     */
    public void setCenter(float x, float y, float z) {
        center.set(x, y, z);
        dirty = true;
    }

    /**
     * Sets the location of this bounding box.
     *
     * @param center - location of this bounding box
     */
    public void setCenter(Vector3 center) {
        setCenter(center.getX(), center.getY(), center.getZ());
    }

    /**
     * Returns the location of this bounding box.
     *
     * @return location of this bounding box
     */
    public Vector3 getCenter() {
        return center;
    }

    /**
     * Sets the extents of this bounding box.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     */
    public void setExtent(float x, float y, float z) {
        extent.set(x, y, z);
        dirty = true;
    }

    /**
     * Sets the extents of this bounding box.
     *
     * @param extent - halved width, height, and length of this bounding box
     */
    public void setExtent(Vector3 extent) {
        setExtent(extent.getX(), extent.getY(), extent.getZ());
    }

    /**
     * Returns the extents of this bounding box.
     *
     * @return extents of this bounding box
     */
    public Vector3 getExtent() {
        return extent;
    }

    /**
     * Calculates the squared distance from the given point to the nearest edge of this bounding box object.
     *
     * @param point - reference point
     *
     * @return squared distance from the given point to the nearest edge of this bounding box
     */
    public float edgeDistanceSquared(Vector3 point) {
        float distPartial;
        float distSquared = 0f;
        Vector3 closest = Pools.Vector3.get();

        closest.set(point).subtract(center);

        if (closest.getX() < -extent.getX()) {
            distPartial = closest.getX() + extent.getX();
            distSquared += distPartial * distPartial;
        } else if (closest.getX() > extent.getX()) {
            distPartial = closest.getX() - extent.getX();
            distSquared += distPartial * distPartial;
        }

        if (closest.getY() < -extent.getY()) {
            distPartial = closest.getY() + extent.getY();
            distSquared += distPartial * distPartial;
        } else if (closest.getY() > extent.getY()) {
            distPartial = closest.getY() - extent.getY();
            distSquared += distPartial * distPartial;
        }

        if (closest.getZ() < -extent.getZ()) {
            distPartial = closest.getZ() + extent.getZ();
            distSquared += distPartial * distPartial;
        } else if (closest.getZ() > extent.getZ()) {
            distPartial = closest.getZ() - extent.getZ();
            distSquared += distPartial * distPartial;
        }

        Pools.Vector3.put(closest);

        return distSquared;
    }

    /**
     * Calculates the distance from the given point to the nearest edge of this bounding box object.
     *
     * @param point - the reference point
     *
     * @return distance from the given point to the nearest edge of this bounding box
     */
    public float edgeDistance(Vector3 point) {
        return EngineMath.sqrt(edgeDistanceSquared(point));
    }

    /**
     * Applies the given transformation to this bounding box object and stores the result to the output bounding box object.
     * Unless the given output storage is this object, this bounding box' extent and center are not modified.
     *
     * @param transform - transformation to be applied to this bounding box
     * @param output - storage for the resulting transformation
     *
     * @return the output bounding box
     */
    public BoundingBox transform(Transform transform, BoundingBox output) {
        if (output == null) {
            output = new BoundingBox();
        }

        if (isInfinite()) {
            return output.set(this);
        }

        Matrix3 rotMatrix = Pools.Matrix3.get().set(transform.getRotation()).absolute();

        output.center.set(center).add(transform.getTranslation());
        rotMatrix.transform(extent, output.extent).multiply(transform.getScale());

        Pools.Matrix3.put(rotMatrix);

        dirty = true;

        return output;
    }

    /**
     * Applies the given transformation to this bounding box object.
     *
     * @param transform - transformation to be applied to this bounding box
     */
    public void transform(Transform transform) {
        transform(transform, this);
    }

    /**
     * Calculates the combined dimensions of this bounding box and the given bounding box and sets the result to the
     * output bounding box object. Unless the given output storage is this object, this bounding box's extent and center
     * are not modified.
     *
     * @param boundingBox - bounding box to combine with
     * @param output - storage for the resulting combination
     *
     * @return output boundingBox
     */
    public BoundingBox combine(BoundingBox boundingBox, BoundingBox output) {
        if (output == null) {
            output = new BoundingBox();
        }

        if (isInfinite() || boundingBox.isInfinite()) {
            output.center.set(Vector3.ZERO);
            output.extent.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
            return output;
        }

        if (isZero()) {
            output.set(boundingBox);
            return output;
        }

        Vector3 temp1 = Pools.Vector3.get();
        Vector3 temp2 = Pools.Vector3.get();
        Vector3 temp3 = Pools.Vector3.get();

        temp1.set(center).subtract(extent);
        temp3.set(boundingBox.center).subtract(boundingBox.extent);

        if (temp1.getX() > temp3.getX()) {
            temp1.setX(temp3.getX());
        }

        if (temp1.getY() > temp3.getY()) {
            temp1.setY(temp3.getY());
        }

        if (temp1.getZ() > temp3.getZ()) {
            temp1.setZ(temp3.getZ());
        }

        temp2.set(center).add(extent);
        temp3.set(boundingBox.center).add(boundingBox.extent);

        if (temp2.getX() < temp3.getX()) {
            temp2.setX(temp3.getX());
        }

        if (temp2.getY() < temp3.getY()) {
            temp2.setY(temp3.getY());
        }

        if (temp2.getZ() < temp3.getZ()) {
            temp2.setZ(temp3.getZ());
        }

        output.center.set(temp1).add(temp2).multiply(0.5f);

        output.extent.setX(temp2.getX() - output.center.getX());
        output.extent.setY(temp2.getY() - output.center.getY());
        output.extent.setZ(temp2.getZ() - output.center.getZ());

        Pools.Vector3.put(temp1);
        Pools.Vector3.put(temp2);
        Pools.Vector3.put(temp3);

        dirty = true;

        return output;
    }

    /**
     * Calculates the combined dimensions of this bounding box and the given bounding box. Unless the given output
     * storage is this object, this bounding box's extent and center are not modified.
     *
     * @param boundingBox - boundingBox to combine with
     */
    public void combine(BoundingBox boundingBox) {
        combine(boundingBox, this);
    }

    /**
     * Calculates a minimum bounding volume for the given set of coordinates.
     *
     * @param coords - vector buffer containing the coordinates
     */
    public void fromCoordinates(Vector3Buffer coords) {
        if (coords == null) {
            return;
        }

        Vector3 temp = coords.get(0, Pools.Vector3.get());
        float minX = temp.getX(), minY = temp.getY(), minZ = temp.getZ();
        float maxX = temp.getX(), maxY = temp.getY(), maxZ = temp.getZ();

        for (int i = 1; i < coords.size(); i++) {
            coords.get(i, temp);

            if (temp.getX() < minX) {
                minX = temp.getX();
            } else if (temp.getX() > maxX) {
                maxX = temp.getX();
            }

            if (temp.getY() < minY) {
                minY = temp.getY();
            } else if (temp.getY() > maxY) {
                maxY = temp.getY();
            }

            if (temp.getZ() < minZ) {
                minZ = temp.getZ();
            } else if (temp.getZ() > maxZ) {
                maxZ = temp.getZ();
            }
        }

        center.set(minX + maxX, minY + maxY, minZ + maxZ);
        center.multiply(0.5f);

        extent.setX(maxX - center.getX());
        extent.setY(maxY - center.getY());
        extent.setZ(maxZ - center.getZ());

        dirty = true;

        Pools.Vector3.put(temp);
    }

    /**
     * Tests intersection with another bounding box object. This immediately returns true if either of this bounding box
     * or the given bounding box is infinite.
     *
     * @param boundingBox - boundingBox to test intersection with
     *
     * @return true if this bounding box intersects the given bounding box
     */
    public boolean intersects(BoundingBox boundingBox) {
        if (isInfinite() || boundingBox.isInfinite()) {
            return true;
        }

        return !(center.getX() + extent.getX() < boundingBox.center.getX() - boundingBox.extent.getX() || center.getX() - extent.getX() > boundingBox.center.getX() + boundingBox.extent.getX())
                || (center.getY() + extent.getY() < boundingBox.center.getY() - boundingBox.extent.getY() || center.getY() - extent.getY() > boundingBox.center.getY() + boundingBox.extent.getY())
                || (center.getZ() + extent.getZ() < boundingBox.center.getZ() - boundingBox.extent.getZ() || center.getZ() - extent.getX() > boundingBox.center.getZ() + boundingBox.extent.getZ());
    }

    /**
     * Tests containment of the given bounding box object by this bounding box object. This immediately returns true if
     * this boundinbounding boxgBox is infinite and false if the given bounding box is infinite.
     *
     * @param boundingBox - boundingBox to test containment of
     *
     * @return true if this boundingBox fully contains the given boundingBox
     */
    public boolean contains(BoundingBox boundingBox) {
        if (isInfinite()) {
            return true;
        }

        if (boundingBox.isInfinite()) {
            return false;
        }

        return !(center.getX() + extent.getX() < boundingBox.center.getX() + boundingBox.extent.getX() || center.getX() - extent.getX() > boundingBox.center.getX() - boundingBox.extent.getX())
                || (center.getY() + extent.getY() < boundingBox.center.getY() + boundingBox.extent.getY() || center.getY() - extent.getY() > boundingBox.center.getY() - boundingBox.extent.getY())
                || (center.getZ() + extent.getZ() < boundingBox.center.getZ() + boundingBox.extent.getZ() || center.getZ() - extent.getX() > boundingBox.center.getZ() - boundingBox.extent.getZ());
    }

    /**
     * Tests containment of the given point by this bounding box object. This immediately returns true if this bounding box is infinite.
     *
     * @param point - point to test containment of
     *
     * @return true if this bounding box fully contains the given point
     */
    public boolean contains(Vector3 point) {
        if (isInfinite()) {
            return true;
        }

        return EngineMath.abs(center.getX() - point.getX()) < extent.getX()
                && EngineMath.abs(center.getY() - point.getY()) < extent.getY()
                && EngineMath.abs(center.getZ() - point.getZ()) < extent.getZ();
    }

    /**
     * Calculates the coordinates of this bounding box corners. Output array should have a length of 8, otherwise, an exception is thrown.
     *
     * @param output - storage array for the output
     *
     * @return the output vector array
     */
    public Vector3[] getCorners(Vector3[] output) {
        if (output == null) {
            output = new Vector3[8];
        }

        if (output.length < 8) {
            throw new EngineException("Input array should have a length of at least 8.");
        }

        float posX = center.getX() + extent.getX();
        float negX = center.getX() - extent.getX();
        float posY = center.getY() + extent.getY();
        float negY = center.getY() - extent.getY();
        float posZ = center.getZ() + extent.getZ();
        float negZ = center.getZ() - extent.getZ();

        output[0].set(negX, posY, posZ);
        output[1].set(negX, negY, posZ);
        output[2].set(posX, negY, posZ);
        output[3].set(posX, posY, posZ);
        output[4].set(posX, posY, negZ);
        output[5].set(posX, negY, negZ);
        output[6].set(negX, negY, negZ);
        output[7].set(negX, posY, negZ);

        return output;
    }

    /**
     * Calculates a radius which completely encloses this bounding volume.
     *
     * @return radius which completely encloses this bounding volume
     */
    public float calculateRadius() {
        return center.distance(center.getX() + extent.getX(), center.getY() + extent.getY(), center.getZ() + extent.getZ());
    }

    /**
     * Returns true if this bounding box extents are all zero and the center is at the origin.
     *
     * @return true if this bounding box extents are all zero and the center is at the origin
     */
    public boolean isZero() {
        return extent.equals(Vector3.ZERO) && center.equals(Vector3.ZERO);
    }

    /**
     * Returns true if this bounding box extents are all infinite and the center is at the origin.
     *
     * @return true if this bounding box extents are all infinite and the center is at the origin
     */
    public boolean isInfinite() {
        return Float.isInfinite(extent.getX()) && Float.isInfinite(extent.getY()) && Float.isInfinite(extent.getZ());
    }

    /**
     * Gives the modification state.
     *
     * @return true if this bounding box has been modified
     */
    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    /**
     * Field-for-field equality test. Returns true if all primitive fields of this bounding box's center and extents are
     * equal to the given bounding box's.
     *
     * @param boundingBox - boundingBox to test equality with
     *
     * @return true if this bounding box equals the given bounding box field-for-field
     */
    public boolean equals(BoundingBox boundingBox) {
        return extent.equals(boundingBox.extent) && center.equals(boundingBox.center);
    }

    /**
     * Partitions this bounding volume.
     *
     * @param divX divisions in the x-axis
     * @param divY divisions in the y-axis
     * @param divZ divisions in the z-axis
     *
     * @return array containing the resulting subdivisions
     */
    public BoundingBox[] subdivide(int divX, int divY, int divZ) {
        if (divX <= 0 || divY <= 0 || divZ <= 0) {
            throw new EngineException("Input values should be greater than zero.");
        }

        int countX = 0;
        int countY = 0;
        int countZ = 0;
        float width = (extent.getX() * 2f) / divX;
        float height = (extent.getY() * 2f) / divY;
        float length = (extent.getZ() * 2f) / divZ;
        float extentX = width * 0.5f;
        float extentY = height * 0.5f;
        float extentZ = length * 0.5f;
        float startX = (extent.getX() + center.getX()) - extentX;
        float startY = (extent.getY() + center.getY()) - extentY;
        float startZ = (extent.getZ() + center.getZ()) - extentZ;

        Vector3 ctr = Pools.Vector3.get();
        Vector3 ext = Pools.Vector3.get().set(extentX, extentY, extentZ);
        BoundingBox[] subs = new BoundingBox[divX * divY * divZ];

        for (int i = 0; i < subs.length; i++) {
            if (countX == divX) {
                countX = 0;
                countY++;

                if (countY == divY) {
                    countY = 0;
                    countZ++;
                }
            }

            float centerX = startX - width * countX;
            float centerY = startY - height * countY;
            float centerZ = startZ - length * countZ;

            ++countX;

            ctr.set(centerX, centerY, centerZ);
            subs[i] = new BoundingBox(ext, ctr);
        }

        Pools.Vector3.put(ext);
        Pools.Vector3.put(ctr);

        return subs;
    }

    /**
     * Determines the position of this bounding box relative to the given plane
     *
     * @param plane - the reference plane
     */
    public Plane.Position position(Plane plane) {
        if (isInfinite()) {
            return Plane.Position.NEITHER;
        }

        float distance = plane.signedDistance(center);

        float radius = EngineMath.abs(
            extent.getX() * plane.getNormal().getX())
            + EngineMath.abs(extent.getY() * plane.getNormal().getY())
            + EngineMath.abs(extent.getZ() * plane.getNormal().getZ()
        );

        if (distance < -radius) {
            return Plane.Position.NEGATIVE;
        }

        if (distance > radius) {
            return Plane.Position.POSITIVE;
        }

        return Plane.Position.NEITHER;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "\n" + "Center: " + center + "\n" + "Extent: " + extent;
    }
}
