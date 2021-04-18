package core.event.listener;

import core.EventListener;
import core.event.MouseEvent;

/**
 * Implemented by classes that listen for mouse events.
 *
 * @author John Paul Quijano
 */
public interface MouseListener extends EventListener {
    void listen(MouseEvent event);
}
