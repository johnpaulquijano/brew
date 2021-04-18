package core;

import core.utility.Buffers;
import core.utility.EngineException;
import core.utility.PNGDecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Loads, converts, and stores supported image file formats to a format that is
 * meaningful to the native graphics API.
 *
 * NOTE: Only PNG files with RGBA pixel format are currently supported.
 *
 * @author John Paul Quijano
 */
public class Image {
    private int width;
    private int height;
    private ByteBuffer data;

    /**
     * Creates an empty image object.
     */
    public Image() {}

    /**
     * Creates an image object from the given file.
     *
     * @param path - image file location
     * @param flip - if true, data decoding is reversed
     */
    public Image(String path, boolean flip) {
        setSource(path, flip);
    }

    /**
     * Reverses the orientation of the given buffered image.
     *
     * @param image - buffered image to flip
     * @return the flipped buffered image
     */
    public static BufferedImage flip(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage output = new BufferedImage(w, h, image.getType());
        Graphics2D g = output.createGraphics();

        g.drawImage(image, 0, 0, w, h, 0, w, h, 0, null);
        g.dispose();

        return output;
    }

    /**
     * Returns the buffer containing this image's pixel data.
     *
     * @return buffer containing the image data
     */
    public ByteBuffer getData() {
        return data;
    }

    /**
     * Returns the image width.
     *
     * @return image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the image height.
     *
     * @return image height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the image source to decode.
     *
     * @param path - image file location
     * @param flip - if true, data decoding is reversed
     */
    public final void setSource(String path, boolean flip) {
        try {
            PNGDecoder decoder = new PNGDecoder(new FileInputStream(path));

            width = decoder.getWidth();
            height = decoder.getHeight();

            data = Buffers.createByteBuffer(width * height * 4);

            if (flip) {
                decoder.decodeFlipped(data, width * 4, PNGDecoder.RGBA);
            } else {
                decoder.decode(data, height * 4, PNGDecoder.RGBA);
            }

            data.flip();
        } catch (IOException ex) {
            throw new EngineException("Failed to load image: " + ex.getMessage());
        }
    }
}
