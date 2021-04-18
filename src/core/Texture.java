package core;

public class Texture extends GraphicsObject {
    public enum Blend {
        REPLACE(0),
        MODULATE(1),
        ACCUMULATE(2),
        INTERPOLATE(3);

        private int value;

        Blend(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final Blend DEFAULT_BLEND_MODE = Blend.MODULATE;
    public static final GL.Filter DEFAULT_MIN_FILTER = GL.Filter.LINEAR;
    public static final GL.Filter DEFAULT_MAG_FILTER = GL.Filter.LINEAR;
    public static final GL.WrapMode DEFAULT_WRAP_MODE_S = GL.WrapMode.CLAMP_TO_EDGE;
    public static final GL.WrapMode DEFAULT_WRAP_MODE_T = GL.WrapMode.CLAMP_TO_EDGE;

    protected boolean dirty;
    protected boolean filterDirty;
    protected boolean wrapModeDirty;
    protected boolean blendModeDirty;
    protected boolean imageDirty;
    protected boolean compressed;
    protected boolean mipmapEnabled;
    protected GL.WrapMode wrapModeS;
    protected GL.WrapMode wrapModeT;
    protected GL.Filter minFilter;
    protected GL.Filter magFilter;
    protected Blend blendMode;
    protected Image image;

    /**
     * Creates a texture with default attributes.
     *
     * @param mipmapped - if true, mipmaps are generated
     */
    public Texture(boolean mipmapped) {
        mipmapEnabled = mipmapped;
        blendMode = DEFAULT_BLEND_MODE;
        wrapModeS = DEFAULT_WRAP_MODE_S;
        wrapModeT = DEFAULT_WRAP_MODE_T;
        minFilter = DEFAULT_MIN_FILTER;
        magFilter = DEFAULT_MAG_FILTER;
    }

    /**
     * Creates a texture from the given template.
     *
     * @param template - the texture to copy attributes from
     */
    public Texture(Texture template) {
        compressed = template.compressed;
        blendMode = template.blendMode;
        wrapModeS = template.wrapModeS;
        wrapModeT = template.wrapModeT;
        minFilter = template.minFilter;
        magFilter = template.magFilter;
        mipmapEnabled = template.mipmapEnabled;
        image = template.image;
    }

    /**
     * Sets all attributes of this texture to the given template's.
     *
     * @param template - the texture to copy attributes from
     *
     * @return this texture
     */
    public Texture set(Texture template) {
        if (template == null) {
            return this;
        }

        compressed = template.compressed;
        blendMode = template.blendMode;
        wrapModeS = template.wrapModeS;
        wrapModeT = template.wrapModeT;
        minFilter = template.minFilter;
        magFilter = template.magFilter;
        mipmapEnabled = template.mipmapEnabled;
        image = template.image;

        return this;
    }

    /**
     * Returns the mipmapping state.
     *
     * @return mipmapping state
     */
    public boolean isMipmapEnabled() {
        return mipmapEnabled;
    }

    /**
     * Sets how this texture is mixed with the existing color.
     *
     * @param mode - the blend mode
     */
    public void setBlendMode(Blend mode) {
        blendMode = mode;
        blendModeDirty = true;
        dirty = true;
    }

    /**
     * Returns the blend mode.
     *
     * @return blend mode
     */
    public Blend getBlendMode() {
        return blendMode;
    }

    /**
     * Sets how this texture horizontally wraps around objects.
     *
     * @param mode - the wrap mode
     */
    public void setWrapModeS(GL.WrapMode mode) {
        wrapModeS = mode;
        wrapModeDirty = true;
        dirty = true;
    }

    /**
     * Returns the horizontal wrap mode.
     *
     * @return the wrap mode
     */
    public GL.WrapMode getWrapModeS() {
        return wrapModeS;
    }

    /**
     * Sets how this texture vertically wraps around objects.
     *
     * @param mode - the wrap mode
     */
    public void setWrapModeT(GL.WrapMode mode) {
        wrapModeT = mode;
        wrapModeDirty = true;
        dirty = true;
    }

    /**
     * Returns the vertical wrap mode.
     *
     * @return the wrap mode
     */
    public GL.WrapMode getWrapModeT() {
        return wrapModeT;
    }

    /**
     * Sets the filter used when this texture is down-scaled.
     *
     * @param filter - the minification filter
     */
    public void setMinificationFilter(GL.Filter filter) {
        minFilter = filter;
        filterDirty = true;
        dirty = true;
    }

    /**
     * Returns the minification filter.
     *
     * @return the minification filter
     */
    public GL.Filter getMinFilter() {
        return minFilter;
    }

    /**
     * Sets the filter used when this texture is up-scaled.
     *
     * @param filter - the magnification filter
     */
    public void setMagnificationFilter(GL.Filter filter) {
        magFilter = filter;
        filterDirty = true;
        dirty = true;
    }

    /**
     * Returns the magnification filter.
     *
     * @return the magnification filter
     */
    public GL.Filter getMagFilter() {
        return magFilter;
    }

    /**
     * If set, the image data is compressed.
     */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
        imageDirty = true;
        dirty = true;
    }

    /**
     * Returns the image data compression state.
     *
     * @return image data compression state
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Sets the image data.
     *
     * @param image - the image data
     */
    public void setImage(Image image) {
        this.image = image;
        imageDirty = true;
        dirty = true;
    }

    /**
     * Returns the image data.
     *
     * @return image data
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns the dirty flag.
     *
     * @return true if this texture has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Returns the filter dirty flag.
     *
     * @return true if filter has been modified since the last frame
     */
    public boolean isFilterDirty() {
        return filterDirty;
    }

    /**
     * Returns the image dirty flag.
     *
     * @return true if image has been changed since the last frame
     */
    public boolean isImageDirty() {
        return imageDirty;
    }

    /**
     * Returns the wrap mode dirty flag.
     *
     * @return true if wrap mode has been modified since the last frame
     */
    public boolean isWrapModeDirty() {
        return wrapModeDirty;
    }

    /**
     * Returns the blend mode dirty flag.
     *
     * @return true if blend mode has been modified since the last frame
     */
    public boolean isBlendModeDirty() {
        return blendModeDirty;
    }

    /**
     * Resets all dirty flags.
     */
    public void clean() {
        if (dirty) {
            dirty = false;
            filterDirty = false;
            wrapModeDirty = false;
            blendModeDirty = false;
            imageDirty = false;
        }
    }

    /**
     * Updates this texture.
     */
    public void update() {
        if (filterDirty) {
            GL.updateTexture2DFilter(id, minFilter, magFilter);
        }

        if (wrapModeDirty) {
            GL.updateTexture2DWrapMode(id, wrapModeS, wrapModeT);
        }

        if (imageDirty) {
            GL.updateTexture2DData(id, image.getWidth(), image.getHeight(), compressed, null);
        }
    }

    @Override
    protected void build() {
        id = GL.createTexture2D(wrapModeS, wrapModeT, minFilter, magFilter);
        GL.setTexture2DData(id, image.getWidth(), image.getHeight(), compressed, image.getData());

        if (mipmapEnabled) {
            GL.generateTexture2DMipmap(id);
        }

        clean();
    }

    @Override
    protected void destroy() {
        GL.freeTexture(id);
        clean();
        id = 0;
    }
}
