package core.event.type;

import core.EventType;

/**
 * Enumeration of traverser event types.
 */
public enum TraverserEventType implements EventType {
    /**
     * Fired before entering the traversal loop.
     */
    INIT,
    /**
     * Fired when the entire subtree of the current branch has already been traversed.
     */
    BRANCH_DONE,
    /**
     * Fired when the subtree of the current branch still needs to be traversed.
     */
    BRANCH_NEXT,
    /**
     * Fired when a leaf node is visited.
     */
    LEAF
}
