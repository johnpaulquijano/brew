package core.event.listener;

import core.event.TraverserEvent;

/**
 * Implemented by classes that listen for traverser events.
 *
 * @author John Paul Quijano
 */
public interface TraverserListener {
    boolean listen(TraverserEvent event);
}
