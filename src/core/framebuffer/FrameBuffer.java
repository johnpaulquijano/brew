package core.framebuffer;

import core.GL;
import core.GraphicsObject;
import core.utility.EngineException;

/**
 * Frame buffer object serves as a render target (drawing surface). It may contain several color buffers and a depth buffer.
 *
 * @author John Paul Quijano
 */
public class FrameBuffer extends GraphicsObject {
    public static final int MAX_COLOR_ATTACHMENTS = 8;

    protected int width;
    protected int height;
    protected float fragWidth;
    protected float fragHeight;
    protected DepthBuffer depthBuffer;
    protected ColorBuffer[] colorBuffers;

    /**
     * Creates a frame buffer object with the given dimensions and attachments.
     *
     * @param width - horizontal dimension
     * @param height - vertical dimension
     * @param depthBuffer - depth buffer attachment
     * @param colorBuffers - array of color buffer attachments
     */
    public FrameBuffer(int width, int height, DepthBuffer depthBuffer, ColorBuffer... colorBuffers) {
        if (colorBuffers.length > MAX_COLOR_ATTACHMENTS) {
            throw new EngineException("Maximum number of color attachments exceeded.");
        }

        this.width = width;
        this.height = height;
        this.depthBuffer = depthBuffer;
        this.colorBuffers = colorBuffers;
        fragWidth = 1f / width;
        fragHeight = 1f / height;
    }

    /**
     * Gives this frame buffer's width.
     *
     * @return this frame buffer's width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gives this frame buffer's height.
     *
     * @return this frame buffer's height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gives inverse of this frame buffer's width.
     *
     * @return inverse of this frame buffer's width
     */
    public float getFragmentWidth() {
        return fragWidth;
    }

    /**
     * Gives inverse of this frame buffer's height.
     *
     * @return inverse of this frame buffer's height
     */
    public float getFragmentHeight() {
        return fragHeight;
    }

    /**
     * Gives the depth buffer.
     *
     * @return depth buffer
     */
    public DepthBuffer getDepthBuffer() {
        return depthBuffer;
    }

    /**
     * Gives the color buffer at the given index.
     *
     * @param index - color buffer array index
     *
     * @return color buffer at the given index
     */
    public ColorBuffer getColorBuffer(int index) {
        return colorBuffers[index];
    }

    /**
     * Gives the number of attached color buffers.
     *
     * @return number of attached color buffers
     */
    public int numColorBuffers() {
        return colorBuffers.length;
    }

    /**
     * Updates the dimensions of this frame buffer.
     *
     * @param width - new width
     * @param height - new height
     */
    public void resize(int width, int height) {
        if (!isBuilt()) {
            return;
        }

        this.width = width;
        this.height = height;

        fragWidth = 1f / width;
        fragHeight = 1f / height;

        if (depthBuffer != null) {
            depthBuffer.resize(width, height);
        }

        for (ColorBuffer colorBuffer : colorBuffers) {
            colorBuffer.resize(width, height);
        }
    }

    /**
     * Copies the given framebuffer's color and depth buffers to this framebuffer's color and depth buffers, respectively.
     *
     * @param source - source framebuffer
     * @param srcIndex - source color buffer index
     * @param dstIndex - destination color buffer index
     * @param filtered - if true, linear filtering is applied
     */
    public void copy(FrameBuffer source, int srcIndex, int dstIndex, boolean filtered) {
        GL.writeFrameBuffer(id);
        GL.writeAttachment(GL.Attachment.COLOR_0.getValue() + dstIndex);
        GL.readFrameBuffer(source.getID());
        GL.readAttachment(GL.Attachment.COLOR_0.getValue() + srcIndex);
        GL.copyBuffers(0, 0, source.width, source.height, 0, 0, width, height, filtered);
    }

    /**
     * Copies the given framebuffer's depth buffer to this framebuffer's depth buffer.
     *
     * @param source - source frame buffer
     */
    public void copyDepth(FrameBuffer source) {
        GL.writeFrameBuffer(id);
        GL.readFrameBuffer(source.getID());
        GL.copyDepthBuffer(0, 0, source.width, source.height, 0, 0, width, height, false);
    }

    /**
     * Copies the given framebuffer's color buffer to this framebuffer's color buffer.
     *
     * @param source - source framebuffer
     * @param srcIndex - source color buffer index
     * @param dstIndex - destination color buffer index
     * @param filtered - if true, linear filtering is applied
     */
    public void copyColor(FrameBuffer source, int srcIndex, int dstIndex, boolean filtered) {
        GL.writeFrameBuffer(id);
        GL.writeAttachment(GL.Attachment.COLOR_0.getValue() + dstIndex);
        GL.readFrameBuffer(source.getID());
        GL.readAttachment(GL.Attachment.COLOR_0.getValue() + srcIndex);
        GL.copyColorBuffer(0, 0, source.width, source.height, 0, 0, width, height, filtered);
    }

    /**
     * Writes the contents of the default screen buffer onto the color buffer at the given index.
     *
     * @param index - index of the color buffer
     * @param screenWidth - width of the screen
     * @param screenHeight - height of the screen
     * @param filter - if true, linear filtering is applied
     */
    public void fromScreen(int index, int screenWidth, int screenHeight, boolean filter) {
        GL.readFrameBuffer(0);
        GL.writeFrameBuffer(id);
        GL.writeAttachment(GL.Attachment.COLOR_0.getValue() + index);
        GL.copyColorBuffer(0, 0, screenWidth, screenHeight, 0, 0, width, height, filter);
    }

    /**
     * Writes the contents of the color buffer at the given index onto the screen.
     *
     * @param index - index of the color buffer
     * @param screenWidth - width of the screen
     * @param screenHeight - height of the screen
     * @param filter - if true, linear filtering is applied
     */
    public void toScreen(int index, int screenWidth, int screenHeight, boolean filter) {
        GL.writeFrameBuffer(0);
        GL.readFrameBuffer(id);
        GL.readAttachment(GL.Attachment.COLOR_0.getValue() + index);
        GL.copyColorBuffer(0, 0, width, height, 0, 0, screenWidth, screenHeight, filter);
    }

    @Override
    protected void build() {
        id = GL.createFrameBuffer();

        GL.writeFrameBuffer(id);

        for (int i = 0; i < colorBuffers.length; i++) {
            ColorBuffer colorBuffer = colorBuffers[i];

            colorBuffer.build();
            colorBuffer.resize(width, height);

            GL.attachColorTexture(colorBuffer.getID(), i, colorBuffer instanceof ColorBufferMultisampled);
        }

        if (depthBuffer != null) {
            depthBuffer.build();
            depthBuffer.resize(width, height);

            if (depthBuffer.getType() == DepthBuffer.Type.RENDERBUFFER) {
                GL.attachDepthRenderBuffer(depthBuffer.getID());
            } else if (depthBuffer.getType() == DepthBuffer.Type.TEXTURE) {
                GL.attachDepthTexture(depthBuffer.getID());
            }
        }

        if (!GL.isFrameBufferComplete()) {
            throw new EngineException(GL.getFrameBufferStatus().getDescription());
        }
    }

    @Override
    protected void destroy() {
        if (depthBuffer != null) {
            depthBuffer.destroy();
        }

        for (ColorBuffer colorBuffer : colorBuffers) {
            colorBuffer.destroy();
        }

        GL.freeFrameBuffer(id);
        id = 0;
    }
}
