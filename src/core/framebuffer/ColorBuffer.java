package core.framebuffer;

import core.GL;
import core.GraphicsObject;

import java.nio.FloatBuffer;

/**
 * A two-dimensional buffer that holds color values.
 *
 * @author John Paul Quijano
 */
public class ColorBuffer extends GraphicsObject {
    public enum Type {
        R(1),
        RG(2),
        RGB(3),
        RGBA(4);

        private int components;

        Type(int components) {
            this.components = components;
        }

        public int getComponents() {
            return components;
        }
    }

    protected int width;
    protected int height;
    protected boolean clearEnabled;
    protected boolean writeEnabled;
    protected Type type;
    protected boolean filterEnabled;

    /**
     * Creates a color buffer with the given parameters.
     *
     * @param type - type of color buffer
     * @param filtered - if true, color values are interpolated
     */
    public ColorBuffer(Type type, boolean filtered) {
        this.type = type;
        filterEnabled = filtered;
        width = GL.DEFAULT_VIEWPORT_WIDTH;
        height = GL.DEFAULT_VIEWPORT_HEIGHT;
    }

    /**
     * Gives the type of color buffer.
     *
     * @return type of color buffer
     */
    public Type getType() {
        return type;
    }

    /**
     * Checks if this color buffer is filtered.
     *
     * @return true if filtered
     */
    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    /**
     * Sets the background clearing state.
     *
     * @param enabled - true to enable background clearing
     */
    public void setClearEnabled(boolean enabled) {
        clearEnabled = enabled;
    }

    /**
     * Gives the background clearing state.
     *
     * @return background clearing state
     */
    public boolean isClearEnabled() {
        return clearEnabled;
    }

    /**
     * Sets the write state.
     *
     * @param enabled - true to enable writing
     */
    public void setWriteEnabled(boolean enabled) {
        writeEnabled = enabled;
    }

    /**
     * Gives the write state.
     *
     * @return write state
     */
    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    @Override
    protected void build() {
        id = GL.createColorBuffer(type.getComponents(), width, height, filterEnabled, (FloatBuffer) null);
    }

    @Override
    protected void destroy() {
        GL.freeTexture(id);
        id = 0;
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
            GL.updateColorBuffer(id, type.getComponents(), width, height, (FloatBuffer) null);
        }
    }
}
