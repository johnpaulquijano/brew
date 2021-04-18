package core;

import core.utility.EngineException;

/**
 * A cube texture containing an image for each of the sides.
 *
 * @author John Paul Quijano
 */
public class TextureCube extends Texture {
    public static final GL.WrapMode DEFAULT_WRAP_MODE_R = GL.WrapMode.CLAMP_TO_EDGE;

    protected Image[] images;
    protected GL.WrapMode wrapModeR;

    public TextureCube(boolean mipmapped) {
        super(mipmapped);
        wrapModeR = DEFAULT_WRAP_MODE_R;
    }

    public TextureCube(TextureCube template) {
        super(template);

        images = template.images;
        wrapModeR = template.wrapModeR;
    }

    public TextureCube(Image[] images) {
        super(true);
        this.images = images;
    }

    @Override
    public Texture set(Texture template) {
        super.set(template);

        if (template instanceof TextureCube) {
            TextureCube cubeTemplate = (TextureCube) template;

            images = cubeTemplate.images;
            wrapModeR = cubeTemplate.wrapModeR;
        }

        return this;
    }

    /**
     * Sets the array of images.
     *
     * @param images - array of images
     */
    public void setImages(Image[] images) {
        this.images = images;
        imageDirty = true;
        dirty = true;
    }

    /**
     * Gives the array of images.
     *
     * @return array of images
     */
    public Image[] getImages() {
        return images;
    }

    /**
     * Sets how this texture orthogonally wraps around objects.
     *
     * @param mode - the wrap mode
     */
    public void setWrapModeR(GL.WrapMode mode) {
        wrapModeR = mode;
        wrapModeDirty = true;
        dirty = true;
    }

    /**
     * Returns the horizontal wrap mode.
     *
     * @return the wrap mode
     */
    public GL.WrapMode getWrapModeR() {
        return wrapModeR;
    }

    @Override
    public void setImage(Image image) {
        throw new EngineException("Use setImages method instead.");
    }

    @Override
    public void update() {
        if (filterDirty) {
            GL.updateTextureCubeFilter(id, minFilter, magFilter);
        }

        if (wrapModeDirty) {
            GL.updateTextureCubeWrapMode(id, wrapModeS, wrapModeT, wrapModeR);
        }

        if (imageDirty) {
            for (int i = 0; i < 6; i++) {
                GL.setTextureCubeData(i, id, images[i].getWidth(), images[i].getHeight(), false, images[i].getData());
            }
        }
    }

    @Override
    public void build() {
        id = GL.createTextureCube(wrapModeS, wrapModeT, wrapModeR, minFilter, magFilter);

        for (int i = 0; i < 6; i++) {
            GL.setTextureCubeData(i, id, images[i].getWidth(), images[i].getHeight(), false, images[i].getData());
        }
    }
}