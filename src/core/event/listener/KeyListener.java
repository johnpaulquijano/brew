package core.event.listener;

import core.EventListener;
import core.event.KeyEvent;

/**
 * Implemented by classes that listen for keyboard events.
 *
 * @author John Paul Quijano
 */
public interface KeyListener extends EventListener {
    void listen(KeyEvent event);
}
