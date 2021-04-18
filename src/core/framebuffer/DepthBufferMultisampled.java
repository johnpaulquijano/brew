package core.framebuffer;

import core.GL;

/**
 * Multi-sampled depth buffer.
 *
 * @author John Paul Quijano
 */
public class DepthBufferMultisampled extends DepthBuffer {
    private int samples;

    /**
     * Creates a multi-sampled depth buffer with the given number of samples.
     *
     * @param samples - number of samples
     */
    public DepthBufferMultisampled(int samples) {
        super(Type.RENDERBUFFER);

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
        id = GL.createDepthRenderBufferMultisampled(samples, width, height);
    }

    /**
     * Updates the numCached of this depth buffer.
     *
     * @param width - new width
     * @param height - new height
     */
    void resize(int width, int height) {
        this.width = width;
        this.height = height;

        if (isBuilt()) {
            GL.updateDepthRenderBufferMultisampled(id, samples, width, height);
        }
    }
}
