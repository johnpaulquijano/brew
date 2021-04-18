package core.event;

import core.Event;
import core.Traverser;

/**
 * Events fired by the traverser.
 */
public class TraverserEvent extends Event<Traverser> {
    public TraverserEvent(Traverser source) {
        super(source);
    }
}
