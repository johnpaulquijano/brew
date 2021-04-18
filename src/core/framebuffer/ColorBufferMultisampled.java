package core.framebuffer;

import core.GL;

import java.nio.FloatBuffer;

/**
 * Multi-sampled color buffer.
 *
 * @author John Paul Quijano
 */
public class ColorBufferMultisampled extends ColorBuffer {
    private int samples;

    /**
     * Creates a multi-sampled color buffer with the given parameters.
     *
     * @param type - type of color buffer
     * @param filtered - if true, color values are interpolated
     */
    public ColorBufferMultisampled(Type type, boolean filtered, int samples) {
        super(type, filtered);

        this.samples = samples;
    }

    /**
     * Gives the number of samples.
     *
     * @return number of samples
     */
    public int getSamples() {
        return samples;
    }

    @Override
    protected void build() {
        id = GL.createColorBufferMultisampled(type.getComponents(), samples, width, height, filterEnabled);
    }

    /**
     * Updates the numCached of this color buffer.
     *
     * @param width - new width
     * @param height - new height
     */
    void resize(int width, int height) {
        this.width = width;
        this.height = height;

        if (isBuilt()) {
            GL.updateColorBufferMultisampled(id, type.getComponents(), samples, width, height);
        }
    }
}
