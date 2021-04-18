package core.event;

import core.Display;
import core.Event;

/**
 * Events fired by the display.
 */
public class DisplayEvent extends Event<Display> {
    public DisplayEvent(Display source) {
        super(source);
    }
}
