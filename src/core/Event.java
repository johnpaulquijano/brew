package core;

/**
 * Base class for all events.
 *
 * @param <S> - source of event
 */
public abstract class Event<S> {
    protected S source;
    protected EventType type;

    public Event(S s) {
        source = s;
    }

    public S getSource() {
        return source;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}
