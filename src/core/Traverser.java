package core;

import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Visits every node in the scenegraph.
 *
 * @author John Paul Quijano
 */
public final class Traverser {
    private Spatial current;
    private TraverserEvent event;
    private List<TraverserListener> listeners;

    Traverser() {
        event = new TraverserEvent(this);
        listeners = new ArrayList<>();
    }

    /**
     * Gives the spatial that is currently being visited.
     *
     * @return spatial that is currently being visited
     */
    public Spatial getCurrent() {
        return current;
    }

    /**
     * Adds a listener for events fired by this traverser.
     *
     * @param listener - listener to add
     */
    public void addListener(TraverserListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Gives the listener at the given index.
     *
     * @param listener - listener to remove
     */
    public void removeListener(TraverserListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gives this traverser's list of listeners.
     */
    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Traverses the given scene.
     */
    void traverse(Spatial scene) {
        current = scene;

        event.setType(TraverserEventType.INIT);

        for (TraverserListener listener : listeners) {
            listener.listen(event);
        }

        LOOP:
        while (current !=  null) {
            if (!current.isEnabled()) {
                current = current.getParent();
                continue;
            }

            if (!current.isLeaf()) {
                if (!current.hasNext()) {
                    event.setType(TraverserEventType.BRANCH_DONE);

                    for (TraverserListener listener : listeners) {
                        listener.listen(event);
                    }

                    current.resetNext();
                    current = current.getParent();
                } else {
                    event.setType(TraverserEventType.BRANCH_NEXT);

                    for (TraverserListener listener : listeners) {
                        listener.listen(event);

                        if (listener.listen(event)) {
                            current = current.getParent();
                            continue LOOP;
                        }
                    }

                    current = current.next();
                }
            } else {
                event.setType(TraverserEventType.LEAF);

                for (TraverserListener listener : listeners) {
                    if (listener.listen(event)) {
                        break;
                    }
                }

                current = current.getParent();
            }
        }
    }
}
