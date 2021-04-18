package core;

/**
 * Enumeration of types of light.
 */
public enum LightType {
    AMBIENT(0),
    DISTANT(1),
    SPOT(2),
    POINT(3);

    private int id;

    LightType(int id) {
        this.id = id;
    }

    /**
     * Gives the unique integer identifier for the light type.
     *
     * @return unique integer identifier for the light type
     */
    public int getID() {
        return id;
    }
}
