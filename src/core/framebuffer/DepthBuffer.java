package core.framebuffer;

import core.GL;
import core.GraphicsObject;

/**
 * A two-dimensional buffer that holds depth values.
 *
 * @author John Paul Quijano
 */
public class DepthBuffer extends GraphicsObject {
    public enum Type {
        RENDERBUFFER,
        TEXTURE
    }

    protected int width;
    protected int height;
    protected Type type;

    /**
     * Creates a depth buffer with the given type.
     *
     * @param type - depth buffer type
     */
    public DepthBuffer(Type type) {
        this.type = type;
    }

    /**
     * Gives this depth buffer's type.
     *
     * @return this depth buffer's type
     */
    public Type getType() {
        return type;
    }

    @Override
    protected void build() {
        if (type == Type.RENDERBUFFER) {
            id = GL.createDepthRenderBuffer(width, height);
        } else if (type == Type.TEXTURE) {
            id = GL.createDepthTexture(width, height);
        }
    }

    @Override
    protected void destroy() {
        if (type == Type.RENDERBUFFER) {
            GL.freeRenderBuffer(id);
        } else if (type == Type.TEXTURE) {
            GL.freeTexture(id);
        }

        id = 0;
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
            if (type == Type.RENDERBUFFER) {
                GL.updateDepthRenderBuffer(id, width, height);
            } else if (type == Type.TEXTURE) {
                GL.updateDepthTexture(id, width, height);
            }
        }
    }
}
