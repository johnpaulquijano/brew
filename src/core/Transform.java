package core;

import core.math.Matrix3;
import core.math.Matrix4;
import core.math.Quaternion;
import core.math.Vector3;
import core.utility.Poolable;
import core.utility.Pools;

/**
 * Encapsulates scale, rotation, and translation.
 *
 * @author John Paul Quijano
 */
public class Transform implements Poolable {
    private boolean dirty;
    private Matrix4 matrix;
    private Quaternion rotation;
    private Vector3 translation;
    private Vector3 scale;

    /**
     * Creates an identity transform.
     */
    public Transform() {
        dirty = true;
        matrix = new Matrix4();
        rotation = new Quaternion();
        translation = new Vector3();
        scale = new Vector3(Vector3.ONE);
    }

    /**
     * Sets this transform's attributes to the given template's.
     *
     * @param template - the transform to copy attributes from
     *
     * @return this transform
     */
    public Transform set(Transform template) {
        scale.set(template.scale);
        rotation.set(template.rotation);
        translation.set(template.translation);
        dirty = true;

        return this;
    }

    /**
     * Sets the rotation component of this transform.
     *
     * @param rotation - a quaternion representing this transform's rotation
     */
    public void setRotation(Quaternion rotation) {
        this.rotation.set(rotation);
        dirty = true;
    }

    /**
     * Sets the rotation component of this transform from Euler angles.
     *
     * @param angleX - rotation around the x-axis in radians
     * @param angleY - rotation around the y-axis in radians
     * @param angleZ - rotation around the z-axis in radians
     */
    public void setRotation(float angleX, float angleY, float angleZ) {
        rotation.fromAngles(angleX, angleY, angleZ);
        dirty = true;
    }

    /**
     * Sets the rotation component of this transform from Euler angles.
     *
     * @param angles - three-component vector representing Euler rotations around each axis
     */
    public void setRotation(Vector3 angles) {
        setRotation(angles.getX(), angles.getY(), angles.getZ());
    }

    /**
     * Sets the rotation component of this transform from axis-angle.
     *
     * @param axis - the axis to rotate around
     * @param angle - amount of rotation in radians
     */
    public void setRotation(Vector3 axis, float angle) {
        rotation.fromAxisAngle(axis, angle);
        dirty = true;
    }

    /**
     * Returns the quaternion representing this transform's rotation component.
     * @return quaternion representing this transform's rotation component
     */
    public Quaternion getRotation() {
        return rotation;
    }

    /**
     * Orients the object to the direction defined by its current location and the given location.
     *
     * @param locX focal x coordinate
     * @param locY focal y coordinate
     * @param locZ focal z coordinate
     * @param upX vertical reference x coordinate
     * @param upY vertical reference y coordinate
     * @param upZ vertical reference z coordinate
     */
    public void lookAt(float locX, float locY, float locZ, float upX, float upY, float upZ) {
        Vector3 up = Pools.Vector3.get();
        Vector3 dir = Pools.Vector3.get().set(locX, locY, locZ).subtract(translation).normalize();
        Vector3 left = Pools.Vector3.get().set(upX, upY, upZ).cross(dir).normalize();
        Matrix3 rotMat = Pools.Matrix3.get();

        up.set(dir).cross(left).normalize();

        rotMat.set(0, 0, left.getX());
        rotMat.set(0, 1, left.getY());
        rotMat.set(0, 2, left.getZ());

        rotMat.set(1, 0, up.getX());
        rotMat.set(1, 1, up.getY());
        rotMat.set(1, 2, up.getZ());

        rotMat.set(2, 0, dir.getX());
        rotMat.set(2, 1, dir.getY());
        rotMat.set(2, 2, dir.getZ());

        rotation.fromMatrix3(rotMat);

        dirty = true;

        Pools.Vector3.put(up);
        Pools.Vector3.put(dir);
        Pools.Vector3.put(left);
        Pools.Matrix3.put(rotMat);
    }

    /**
     * Orients the object to the direction defined by its current location and the given location.
     *
     * @param location - the focused point
     * @param up - the reference vertical vector
     */
    public void lookAt(Vector3 location, Vector3 up) {
        lookAt(location.getX(), location.getY(), location.getZ(), up.getX(), up.getY(), up.getZ());
    }

    /**
     * Orients the object to the given direction.
     *
     * @param dirX direction x coordinate
     * @param dirY direction y coordinate
     * @param dirZ direction z coordinate
     * @param upX vertical reference x coordinate
     * @param upY vertical reference y coordinate
     * @param upZ vertical reference z coordinate
     */
    public void lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        Vector3 up = Pools.Vector3.get();
        Vector3 dir = Pools.Vector3.get().set(dirX, dirY, dirZ).normalize();
        Vector3 left = Pools.Vector3.get().set(upX, upY, upZ).cross(dir).normalize();
        Matrix3 rotMat = Pools.Matrix3.get();

        up.set(dir).cross(left).normalize();

        rotMat.set(0, 0, left.getX());
        rotMat.set(0, 1, left.getY());
        rotMat.set(0, 2, left.getZ());

        rotMat.set(1, 0, up.getX());
        rotMat.set(1, 1, up.getY());
        rotMat.set(1, 2, up.getZ());

        rotMat.set(2, 0, dir.getX());
        rotMat.set(2, 1, dir.getY());
        rotMat.set(2, 2, dir.getZ());

        rotation.fromMatrix3(rotMat);

        dirty = true;

        Pools.Vector3.put(up);
        Pools.Vector3.put(dir);
        Pools.Vector3.put(left);
        Pools.Matrix3.put(rotMat);
    }

    /**
     * Orients the object to the given direction.
     *
     * @param direction - the orientation direction
     * @param up - the reference vertical vector
     */
    public void lookAlong(Vector3 direction, Vector3 up) {
        lookAlong(direction.getX(), direction.getY(), direction.getZ(), up.getX(), up.getY(), up.getZ());
    }

    /**
     * Sets this transform's translation to the given coordinates.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     */
    public void setTranslation(float x, float y, float z) {
        translation.set(x, y, z);
        dirty = true;
    }

    /**
     * Sets this transform's translation to the given coordinates.
     *
     * @param translation - a three-component translation vector
     */
    public void setTranslation(Vector3 translation) {
        setTranslation(translation.getX(), translation.getY(), translation.getZ());
    }

    /**
     * Returns this transform's translation component.
     *
     * @return this transform's translation component
     */
    public Vector3 getTranslation() {
        return translation;
    }

    /**
     * Sets this transform's scale to the given values.
     *
     * @param x - scale value in the x-axis
     * @param y - scale value in the y-axis
     * @param z - scale value in the z-axis
     */
    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        dirty = true;
    }

    /**
     * Sets this transform's scale to the given vector.
     *
     * @param scale - a three-component scale vector
     */
    public void setScale(Vector3 scale) {
        setScale(scale.getX(), scale.getY(), scale.getZ());
    }

    /**
     * Sets all of this transform's scale to the given scalar value.
     *
     * @param scale - scale value
     */
    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    /**
     * Returns the scale vector.
     *
     * @return scale vector
     */
    public Vector3 getScale() {
        return scale;
    }

    /**
     * Combines this transform with the given input transform and stores the result into the given output transform.
     *
     * @param input - transform to combine with this transform
     * @param output - transform to store the results into
     *
     * @return the output transform
     */
    public Transform combine(Transform input, Transform output) {
        if (output == null) {
            output = new Transform();
        }

        if (input == null || input.isIdentity()) {
            return output.set(this);
        }

        if (isIdentity()) {
            return output.set(input);
        }

        output.set(this);

        Quaternion inverseRot = Pools.Quaternion.get().set(input.rotation).invert();

        output.scale.multiply(input.scale);
        output.rotation.multiply(input.rotation);
        output.translation.multiply(input.scale);

        inverseRot.transform(output.translation, output.translation).add(input.translation);

        Pools.Quaternion.put(inverseRot);

        return output;
    }

    /**
     * Combines this transform with the given input transform and stores the result to this transform.
     *
     * @param input - transform to combine with this transform
     */
    public void combine(Transform input) {
        combine(input, this);
    }

    /**
     * Sets the scale to Vector3.ONE, rotation to Quaternion.IDENTITY, and translation to Vector3.ZERO. This yields
     * an identity matrix.
     */
    public void setIdentity() {
        scale.set(Vector3.ONE);
        rotation.set(Quaternion.IDENTITY);
        translation.set(Vector3.ZERO);

        dirty = true;
    }

    /**
     * Returns whether or not this transform is an identity.
     *
     * @return true if this transform is an identity
     */
    public boolean isIdentity() {
        return scale.equals(Vector3.ONE) && translation.equals(Vector3.ZERO) && rotation.equals(Quaternion.IDENTITY);
    }

    /**
     * Constructs a 4x4 matrix out of this transform's rotation, scale, and translation vectors.
     *
     * @return the constructed 4x4 matrix
     */
    public Matrix4 toMatrix() {
        if (dirty) {
            Matrix3 rotMat = Pools.Matrix3.get();

            matrix.set(rotation.toMatrix3(rotMat).scale(scale));
            matrix.set(3, 0, translation.getX());
            matrix.set(3, 1, translation.getY());
            matrix.set(3, 2, translation.getZ());

            Pools.Matrix3.put(rotMat);
        }

        return matrix;
    }

    /**
     * Checks if this transform has been modified.
     *
     * @return true if this transform has been modified
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Resets dirty flags.
     */
    public void clean() {
        dirty = false;
    }

    /**
     * Field-by-field equality check between this and the given transform.
     *
     * @param transform - the transform to check equality with
     *
     * @return true if this transform equals the given transform field-by-field
     */
    public boolean equals(Transform transform) {
        return translation.equals(transform.translation) && rotation.equals(transform.rotation) && scale.equals(transform.scale);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "\n"
                + "Translation: " + translation + "\n"
                + "Rotation: " + rotation + "\n"
                + "Scale: " + scale;
    }
}
