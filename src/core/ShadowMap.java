package core;

import core.utility.EngineException;

/**
 * Stores depth values from shadow casters.
 */
public class ShadowMap extends GraphicsObject {
    public static final float DEFAULT_OFFSET_UNIT = 4f;
    public static final float DEFAULT_OFFSET_FACTOR = 4f;

    private int[] buffers;
    private int resolution;
    private float offsetUnits;
    private float offsetFactor;
    private boolean cube;
    private boolean resDirty;

    /**
     * Creates a shadow texture with the given dimensions.
     *
     * @param resolution - texture dimensions
     */
    public ShadowMap(int resolution, boolean cube) {
        this.cube = cube;
        this.resolution = resolution;

        offsetUnits = DEFAULT_OFFSET_UNIT;
        offsetFactor = DEFAULT_OFFSET_FACTOR;

        buffers = new int[cube ? 6 : 1];
        resDirty = true;
    }

    /**
     * Sets the resolution of this shadow texture.
     *
     * @param resolution - texture resolution
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
        resDirty = true;
    }

    /**
     * Gives this shadow texture's resolution.
     *
     * @return this shadow texture's resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Sets the polygon offset applied to casters when drawn to this shadow map to prevent shadow acne artifacts.
     *
     * @param factor - scale factor used to create variable depth offset for each polygon
     * @param units - multiplied by an implementation-specific value to create a constant depth offset
     */
    public void setOffset(int factor, int units) {
        offsetUnits = units;
        offsetFactor = factor;
    }

    /**
     * Gives the offset factor.
     *
     * @return offset factor
     */
    public float getOffsetFactor() {
        return offsetFactor;
    }

    /**
     * Gives the offset units.
     *
     * @return offset units
     */
    public float getOffsetUnits() {
        return offsetUnits;
    }

    /**
     * Updates this shadow texture.
     */
    public void update() {
        if (resDirty) {
            if (cube) {
                GL.updateShadowTextureCube(id, resolution, resolution);
            } else {
                GL.updateShadowTexture(id, resolution, resolution);
            }

            resDirty = false;
        }
    }

    /**
     * Clears shadow map content on all buffers.
     */
    public void clearBuffers() {
        for (int i = 0; i < buffers.length; i++) {
            GL.writeFrameBuffer(buffers[i]);
            GL.clearBuffer(GL.Buffer.DEPTH);
        }
    }

    /**
     * Prepares this shadow map for rendering.
     *
     * @param index - buffer index
     */
    public void initDraw(int index) {
        GL.writeFrameBuffer(buffers[index]);
        GL.setDepthClampEnabled(true);
        GL.setColorWriteEnabled(false);
        GL.setDepthWriteEnabled(true);
        GL.setFaceCullingEnabled(true);
        GL.setCullFace(GL.CullFace.BACK);
        GL.setPolygonOffsetFillEnabled(true);
        GL.setPolygonOffset(offsetFactor, offsetUnits);
        GL.setViewport(0, 0, resolution, resolution);
        GL.clearBuffer(GL.Buffer.DEPTH);
    }

    @Override
    protected void build() {
        if (cube) {
            id = GL.createShadowTextureCube(resolution, resolution);

            for (int i = 0; i < 6; i++) {
                buffers[i] = GL.createFrameBuffer();

                GL.writeFrameBuffer(buffers[i]);
                GL.attachCubeShadowTexture(id, i);

                if (!GL.isFrameBufferComplete()) {
                    throw new EngineException(GL.getFrameBufferStatus().getDescription());
                }
            }
        } else {
            id = GL.createShadowTexture(resolution, resolution);
            buffers[0] = GL.createFrameBuffer();

            GL.writeFrameBuffer(buffers[0]);
            GL.attachShadowTexture(id);

            if (!GL.isFrameBufferComplete()) {
                throw new EngineException(GL.getFrameBufferStatus().getDescription());
            }
        }
    }

    @Override
    protected void destroy() {
        GL.freeTexture(id);

        for (int i = 0; i < buffers.length; i++) {
            GL.freeFrameBuffer(buffers[i]);
            buffers[i] = 0;
        }

        id = 0;
    }
}
