package core;

import core.utility.EngineException;

import java.util.*;

/**
 * A generic tree node.
 *
 * @author John Paul Quijano
 */
public class Node<T extends Node> implements Iterable<T> {
    protected T root;
    protected T parent;
    protected List<T> children;
    protected Set<T> hashedChildren;
    protected String name;

    /**
     * Creates a node with default attributes.
     */
    public Node() {
        root = (T) this;
        name = getClass().getSimpleName();
        children = new ArrayList<>();
        hashedChildren = new HashSet<>();
    }

    /**
     * Creates a node with the given name.
     *
     * @param name - name of this node
     */
    public Node(String name) {
        root = (T) this;
        this.name = name;
        children = new ArrayList<>();
        hashedChildren = new HashSet<>();
    }

    /**
     * Adds the given node to this node's list of children.
     *
     * @param node - node to add as a child
     *
     * @return true if child does not exist and has been successfully added
     */
    public boolean addChild(T node) {
        if (node.parent != null) {
            throw new EngineException("Node cannot have multiple parents.");
        }

        if (node.ancestorOf(this)) {
            throw new EngineException("Cannot add an ancestor or itself as a child.");
        }

        if (!hashedChildren.contains(node)) {
            hashedChildren.add(node);
            children.add(node);

            node.parent = this;
            node.root = root;
            node.propagateRoot();

            return true;
        }

        return false;
    }

    /**
     * Gives the child node at the given index in tis node's list of children.
     *
     * @param index - an index in this node's list of children
     *
     * @return child node at the given index
     */
    public T getChild(int index) {
        return children.get(index);
    }

    /**
     * Removes the child node at the given index.
     *
     * @param index - an index in this node's list of children
     *
     * @return true if child exists and has been successfully removed
     */
    public boolean removeChild(int index) {
        T child = children.remove(index);

        if (child != null) {
            hashedChildren.remove(child);
            child.parent = null;
            child.root = child;
            child.propagateRoot();

            return true;
        }

        return false;
    }

    /**
     * Removes the given node from this node's list of children.
     *
     * @param node - node to remove
     */
    public boolean removeChild(T node) {
        if (children.remove(node)) {
            hashedChildren.remove(node);
            node.parent = null;
            node.root = node;
            node.propagateRoot();

            return true;
        }

        return false;
    }

    /**
     * Clears this node's list of children.
     */
    public void removeAllChildren() {
        for (T child : children) {
            child.parent = null;
            child.root = child;
            child.propagateRoot();
        }

        children.clear();
        hashedChildren.clear();
    }

    /**
     * Checks if the given node is one of this node's children.
     *
     * @param node - node to check for containment
     *
     * @return true if the given node is one of this node's children
     */
    public boolean hasChild(T node) {
        return hashedChildren.contains(node);
    }

    /**
     * Gives this node's number of children.
     *
     * @return this node's number of children
     */
    public int numChildren() {
        return children.size();
    }

    /**
     * Gives the parent of this node.
     *
     * @return parent of this node
     */
    public T getParent() {
        return parent;
    }

    /**
     * Gives the root node of the tree which this node is a member of.
     *
     * @return root node of the tree which this node is a member of
     */
    public T getRoot() {
        return root;
    }

    /**
     * Checks if this node has no parent.
     *
     * @return true if this node has no parent
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Checks if this node has no children.
     *
     * @return true if this has no children
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Checks if this node is an ancestor of the given node.
     *
     * @param node - node to check against
     *
     * @return true if this node is an ancestor of the given node
     */
    public boolean ancestorOf(T node) {
        T current = node;

        while (current != null) {
            if (current == this) {
                return true;
            } else {
                current = (T) current.parent;
            }
        }

        return false;
    }

    /**
     * Sets the name of this node.
     *
     * @param name - new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gives the name of this node.
     *
     * @return name of this node
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Iterator<T> iterator() {
        return children.iterator();
    }

    /**
     * Sets the root node of all descendants to this node's root.
     */
    protected void propagateRoot() {
        for (T child : children) {
            child.root = root;
            child.propagateRoot();
        }
    }
}
