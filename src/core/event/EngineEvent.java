package core.event;

import core.Engine;
import core.Event;

/**
 * Events fired by the engine.
 */
public class EngineEvent extends Event<Engine> {
    public EngineEvent(Engine source) {
        super(source);
    }
}
