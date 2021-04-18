package core.event.listener;

import core.EventListener;
import core.event.EngineEvent;

/**
 * Implemented by classes that listen for engine events.
 *
 * @author John Paul Quijano
 */
public interface EngineListener extends EventListener {
    boolean listen(EngineEvent event);
}
