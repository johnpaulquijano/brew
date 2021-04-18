package core.animation;

import core.Node;
import core.math.Matrix3;
import core.math.Matrix4;
import core.math.Quaternion;
import core.math.Vector3;
import core.utility.Buffers;
import core.utility.Pools;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Building block for hierarchical joints in a skeletal animation.
 *
 * @author John Paul Quijano
 */
public class Joint extends Node<Joint> {
    private Matrix4 matrix;
    private Vector3 translation;
    private Quaternion rotation;
    private FloatBuffer buffer;
    private FloatBuffer transformBuffer;
    private List<String> order;
    private List<Joint> jointList;
    private HashMap<String, Joint> jointMap;

    public Joint() {
        matrix = new Matrix4();
        translation = new Vector3();
        rotation = new Quaternion();
        order = new ArrayList<>();
        jointList = new ArrayList<>();
        jointMap = new HashMap<>();
        transformBuffer = Buffers.createFloatBuffer(16);
    }

    /**
     * Sets this joint's translation transformation then updates the transformation matrix.
     *
     * @param translation - translation transformation
     */
    public void setTranslation(Vector3 translation) {
        this.translation.set(translation);

        matrix.set(3, 0, translation.getX());
        matrix.set(3, 1, translation.getY());
        matrix.set(3, 2, translation.getZ());
    }

    /**
     * Gives this joint's local translation.
     *
     * @return this joint's local translation
     */
    public Vector3 getTranslation() {
        return translation;
    }

    /**
     * Sets this joint's rotation transformation then updates the transformation matrix.
     *
     * @param rotation - rotation transformation
     */
    public void setRotation(Quaternion rotation) {
        this.rotation.set(rotation);

        Matrix3 rotMat = Pools.Matrix3.get();
        matrix.set(rotation.toMatrix3(rotMat));
        Pools.Matrix3.put(rotMat);
    }

    /**
     * Gives this joint's local rotation.
     *
     * @return this joint's local rotation
     */
    public Quaternion getRotation() {
        return rotation;
    }

    /**
     * Sets this joint's transformation matrix then updates the translation and rotation vectors.
     *
     * @param matrix - transformation matrix
     */
    public void setTransform(Matrix4 matrix) {
        this.matrix.set(matrix);
        updateVectors();
    }

    /**
     * Gives the matrix representation of this joint's transformations.
     *
     * @return this joint's transformation matrix
     */
    public Matrix4 getMatrix() {
        return matrix;
    }

    /**
     * Recursively combines a joint's transformation with its parent's, starting from this joint.
     *
     * @return this joint
     */
    public Joint cascade() {
        if (parent != null) {
            matrix.multiply(parent.matrix);
            updateVectors();
        }

        for (Joint child : children) {
            child.cascade();
        }

        return this;
    }

    /**
     * Recursively inverts the transformation matrices in this joint hierarchy.
     */
    public void invert() {
        matrix.invert();
        updateVectors();

        for (Joint child : children) {
            child.invert();
        }
    }

    /**
     * Recursively multiplies the input joint's transformation matrix by this joint's transformation matrix.
     *
     * @return this joint
     */
    public Joint transform(Joint input) {
        Matrix4 inputMat = Pools.Matrix4.get().set(input.matrix);

        inputMat.multiply(matrix);
        matrix.set(inputMat);

        Pools.Matrix4.put(inputMat);

        for (int i = 0; i < numChildren(); i++) {
            getChild(i).transform(input.getChild(i));
        }

        return this;
    }

    /**
     * Recursively interpolates this joint's and the given input's transformations based on the given delta value.
     *
     * @param input - joint to interpolate with this joint
     * @param output - joint to store the interpolation output
     * @param delta - a value between 0 and 1, inclusively
     *
     * @return the output joint
     */
    public Joint interpolate(Joint input, Joint output, float delta) {
        if (output == null) {
            output = deepCopy().collapse();
        }

        output.rotation.set(rotation).slerp(input.rotation, delta);
        output.translation.set(translation).lerp(input.translation, delta);

        output.updateMatrix();

        for (int i = 0; i < numChildren(); i++) {
            getChild(i).interpolate(input.getChild(i), output.getChild(i), delta);
        }

        return output;
    }

    /**
     * Creates a list of all the joints in this hierarchy based on the set order.
     *
     * @return this joint
     */
    public Joint collapse() {
        jointMap.clear();
        jointList.clear();

        hashJoints(jointMap);

        for (String key : order) {
            jointList.add(jointMap.get(key));
        }

        buffer = Buffers.createFloatBuffer(jointList.size() * 16);

        return this;
    }

    /**
     * Writes the transformations of all joints in the collapsed joint list to a buffer.
     *
     * @return this Joint
     */
    public Joint resolve() {
        buffer.clear();

        for (Joint joint : jointList) {
            buffer.put(joint.matrix.toFloatBuffer(transformBuffer));
        }

        buffer.flip();

        return this;
    }

    /**
     * Sets the list of joint names which defines the joint indices.
     */
    public void setOrder(List<String> order) {
        this.order = order;
    }

    /**
     * Returns the joint at the given index in this joint's collapsed list.
     *
     * @param index - index of the joint
     *
     * @return the joint at the given index in the collapsed list
     */
    public Joint getJoint(int index) {
        return jointList.get(index);
    }

    /**
     * Gives the joint with the given name in this joint's collapsed list.
     *
     * @param name - name of the joint
     *
     * @return joint with the given name in this joint's collapsed list
     */
    public Joint getJoint(String name) {
        return jointMap.get(name);
    }

    /**
     * Gives the number of joints in this joint's collapsed list.
     *
     * @return number of joints in this joint's collapsed list
     */
    public int numJoints() {
        return jointList.size();
    }

    /**
     * Creates a copy of this joint but not its descendants.
     *
     * @return copy of this joint
     */
    public Joint copy() {
        Joint copy = new Joint();

        copy.name = name;
        copy.order.addAll(order);
        copy.matrix.set(matrix);
        copy.rotation.set(rotation);
        copy.translation.set(translation);

        return copy;
    }

    /**
     * Creates a copy of this joint and all its descendants.
     *
     * @return copy of this joint and its descendants
     */
    public Joint deepCopy() {
        Joint copy = copy();

        for (Joint child : children) {
            copy.addChild(child.deepCopy());
        }

        return copy;
    }

    /**
     * Returns the buffer containing all transformations from this joint's collapsed joint list.
     *
     * @return buffer containing all transformations from this joint's collapsed joint list
     */
    public FloatBuffer getBuffer() {
        return buffer;
    }

    /**
     * Recursively places this joint and its descendants in the given hashmap with the joint's name as the key.
     */
    private void hashJoints(HashMap map) {
        map.put(name, this);

        for (Joint child : children) {
            child.hashJoints(map);
        }
    }

    /**
     * Updates the translation and rotation vectors from the transformation matrix.
     */
    private void updateVectors() {
        translation.set(matrix.get(3, 0), matrix.get(3, 1), matrix.get(3, 2));

        rotation.fromMatrix3(
                matrix.get(0, 0), matrix.get(0, 1), matrix.get(0, 2),
                matrix.get(1, 0), matrix.get(1, 1), matrix.get(1, 2),
                matrix.get(2, 0), matrix.get(2, 1), matrix.get(2, 2)
        );
    }

    /**
     * Updates the transformation matrix from the translation and rotation vectors.
     */
    private void updateMatrix() {
        Matrix3 rotMat = Pools.Matrix3.get();
        matrix.set(rotation.toMatrix3(rotMat));
        Pools.Matrix3.put(rotMat);

        matrix.set(3, 0, translation.getX());
        matrix.set(3, 1, translation.getY());
        matrix.set(3, 2, translation.getZ());
    }
}
