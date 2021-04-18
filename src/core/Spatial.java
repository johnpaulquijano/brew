package core;

import core.math.Matrix4;
import core.utility.Buffers;

import java.nio.FloatBuffer;

/**
 * Spatial is the most basic building-block of a scenegraph.
 *
 * @author John Paul Quijano
 */
public class Spatial extends Node<Spatial> {
    protected int next;
    protected boolean enabled;
    protected boolean boundsDirty;
    protected boolean transformDirty;
    protected boolean descendantTransformDirty;
    protected boolean hierarchicalBoundsEnabled;
    protected Matrix4 worldMatrix;
    protected FloatBuffer worldBuffer;
    protected BoundingBox localBoundingBox;
    protected BoundingBox worldBoundingBox;
    protected Transform localTransform;
    protected Transform worldTransform;

    public Spatial() {
        enabled = true;
        boundsDirty = true;
        transformDirty = true;
        descendantTransformDirty = true;
        hierarchicalBoundsEnabled = true;
        worldMatrix = new Matrix4();
        worldBuffer = Buffers.createFloatBuffer(16);
        localBoundingBox = new BoundingBox();
        worldBoundingBox = new BoundingBox();
        localTransform = new Transform();
        worldTransform = new Transform();
    }

    /**
     * Sets this spatial's attributes to the given tempalte's.
     *
     * @param template - spatial to copy attributes from
     *
     * @return this spatial
     */
    public Spatial set(Spatial template) {
        setEnabled(template.enabled);
        setBounds(template.localBoundingBox);
        setTransform(template.localTransform);

        return this;
    }

    @Override
    public boolean addChild(Spatial child) {
        if (super.addChild(child)) {
            if (transformDirty) {
                child.propagateTransformDirty();
            }

            if (child.transformDirty) {
                child.escalateTransformDirty();
            }

            return true;
        }

        return false;
    }

    /**
     * Sets whether this spatial is sent to the rendering pipeline or not.
     *
     * @param enabled - if true, this spatial is sent to the rendering pipeline
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if this spatial is enabled.
     *
     * @return true if this spatial is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Spatial maintains a pointer for successive traversal of its children. This method gives the child at the pointer
     * position then advances the pointer.
     *
     * @return child at the pointer position
     */
    public Spatial next() {
        return getChild(next++);
    }

    /**
     * Checks if traversal has reached the last child.
     *
     * @return true if pointer is not on the last child
     */
    public boolean hasNext() {
        return next < numChildren();
    }

    /**
     * Resets the pointer to the first child.
     */
    public void resetNext() {
        next = 0;
    }

    /**
     * Checks if the pointer is at the first child.
     *
     * @return true if the pointer is at the first child
     */
    public boolean isReset() {
        return next == 0;
    }

    /**
     * Sets local transformation transformation to the given transform.
     *
     * @param transform - the new transformation
     */
    public void setTransform(Transform transform) {
        localTransform.set(transform);
        propagateTransformDirty();
        escalateTransformDirty();
    }

    /**
     * Gives the local transformation.
     *
     * @return local transformation
     */
    public Transform getLocalTransform() {
        return localTransform;
    }

    /**
     * Gives the transformation relative to the parent's world transformation.
     *
     * @return world transformation
     */
    public Transform getWorldTransform() {
        return worldTransform;
    }

    /**
     * Combines the local transformation with the parent's world transformation.
     */
    public void calculateWorldTransform() {
        if (transformDirty) {
            localTransform.combine(parent != null ? parent.worldTransform : null, worldTransform);
            worldMatrix.set(worldTransform.toMatrix()).toFloatBuffer(worldBuffer);
        }
    }

    /**
     * Gives the world transformation matrix.
     *
     * @return world transformation matrix
     */
    public Matrix4 getWorldTransformMatrix() {
        return worldMatrix;
    }

    /**
     * Gives the float buffer containing world transformation matrix values.
     *
     * @return float buffer containing world transformation matrix values
     */
    public FloatBuffer getWorldTransformBuffer() {
        return worldBuffer;
    }

    /**
     * Sets and initializes the local and world bounding volumes to the given boundingBox.
     */
    public void setBounds(BoundingBox boundingBox) {
        localBoundingBox.set(boundingBox);
        worldBoundingBox.set(localBoundingBox);
        boundsDirty = true;
    }

    /**
     * Gives the local bounding volume.
     *
     * @return local bounding volume
     */
    public BoundingBox getLocalBounds() {
        return localBoundingBox;
    }

    /**
     * Gives the world bounding volume.
     *
     * @return world bounding volume
     */
    public BoundingBox getWorldBounds() {
        return worldBoundingBox;
    }

    /**
     * Sets hierarchical bounds enabled state. This is enabled by default.
     *
     * @param enable - if true, this spatial contributes to hierarchical bounds calculation
     */
    public void setHierarchicalBoundsEnabled(boolean enable) {
        hierarchicalBoundsEnabled = enable;
    }

    /**
     * Gives hierarchical bounds enabled state.
     *
     * @return hierarchical bounds enabled state
     */
    public boolean isHierarchicalBoundsEnabled() {
        return hierarchicalBoundsEnabled;
    }

    /**
     * Transforms the local bounding volume by the world transformation.
     */
    public void calculateWorldBounds() {
        if (transformDirty || boundsDirty) {
            localBoundingBox.transform(worldTransform, worldBoundingBox);
        }
    }

    /**
     * Calculates a bounding volume which completely encloses all the children. This stores the result in the world
     * bounds, which means that in creating a bounding volume hierarchy, only the leaf nodes need to be transformed
     * to world coordinates.
     */
    public void calculateHierarchicalBounds() {
        if (hierarchicalBoundsEnabled) {
            if (parent != null) {
                if (transformDirty || descendantTransformDirty) {
                    worldBoundingBox.set(BoundingBox.ZERO);

                    for (Spatial child : children) {
                        worldBoundingBox.combine(child.worldBoundingBox);
                    }
                }
            }
        }
    }

    /**
     * Gives the bounds modification state.
     *
     * @return true if the bounds has been changed
     */
    public boolean isBoundsDirty() {
        return boundsDirty;
    }

    /**
     * Gives the transform modification state.
     *
     * @return true if the transformation has been changed
     */
    public boolean isTransformDirty() {
        return transformDirty;
    }

    /**
     * Gives the bounds modification state.
     *
     * @return true if the descendant transformation has been changed
     */
    public boolean isDescendantTransformDirty() {
        return descendantTransformDirty;
    }

    /**
     * Resets this spatial's dirty flags.
     */
    public void clean() {
        worldBoundingBox.clean();
        localBoundingBox.clean();
        worldTransform.clean();
        localTransform.clean();

        boundsDirty = false;
        transformDirty = false;
        descendantTransformDirty = false;
    }

    /**
     * Sets the ancestors' descendant transform dirty flag.
     */
    private void escalateTransformDirty() {
        Spatial current = parent;

        while (current != null) {
            current.descendantTransformDirty = true;
            current = current.parent;
        }
    }

    /**
     * Recursively sets the descendants' transform dirty flag.
     */
    private void propagateTransformDirty() {
        transformDirty = true;

        for (Spatial child : children) {
            child.propagateTransformDirty();
        }
    }

    /**
     * Creates a copy of this spatial but not its children.
     *
     * @return clone
     */
    public Spatial copy() {
        Spatial clone = new Spatial();

        clone.set(this);
        clone.parent = null;
        clone.boundsDirty = true;
        clone.transformDirty = true;

        return clone;
    }

    /**
     * Creates a copy of this spatial and all its descendants.
     *
     * @return the clone
     */
    public Spatial deepCopy() {
        Spatial clone = copy();

        for (Spatial child : children) {
            clone.addChild(child.deepCopy());
        }

        return clone;
    }
}
