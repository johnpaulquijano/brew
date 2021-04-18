package core.event.listener;

import core.EventListener;
import core.event.DisplayEvent;

/**
 * Implemented by classes that listen for display events.
 *
 * @author John Paul Quijano
 */
public interface DisplayListener extends EventListener {
    void listen(DisplayEvent event);
}
