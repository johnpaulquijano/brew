package core;

/**
 * Encapsulates display attributes.
 *
 * @author John Paul Quijano
 */
public class DisplayMode {
    private int bits;
    private int width;
    private int height;
    private int frequency;

    /**
     * Creates a  display mode with the given attributes.
     *
     * @param bits - number of bits per pixel
     * @param frequency - the refresh rate
     * @param width - display width
     * @param height - display height
     */
    DisplayMode(int bits, int frequency, int width, int height) {
        this.bits = bits;
        this.width = width;
        this.height = height;
        this.frequency = frequency;
    }

    /**
     * Gives this display mode's bits-per-pixel.
     *
     * @return bits-per-pixel
     */
    public int getBits() {
        return bits;
    }

    /**
     * Gives this display mode's display width.
     *
     * @return display width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gives this display mode's display height.
     *
     * @return display height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gives this display mode's refresh rate.
     *
     * @return refresh rate
     */
    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "\n"
                + "Width: " + width + "\n"
                + "Height: " + height + "\n"
                + "Frequency: " + frequency + "\n"
                + "Bits-per-pixel: " + bits;
    }
}
