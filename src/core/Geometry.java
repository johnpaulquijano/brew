package core;

import core.buffer.Vector2Buffer;
import core.buffer.Vector3Buffer;
import core.buffer.Vector4Buffer;
import core.math.Vector2;
import core.math.Vector3;
import core.math.Vector4;
import core.utility.Buffers;
import core.utility.Face;
import core.utility.Pools;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Encapsulates vertex attributes such as colors, indices, coordinates, and texture coordinates.
 *
 * @author John Paul Quijano
 */
public class Geometry extends GraphicsObject {
    public enum Type {
        LINES, TRIS, QUADS
    }

    protected int indexBuffer;
    protected int coordBuffer;
    protected int colorBuffer;
    protected int texCoordBuffer;
    protected int numCoords;
    protected int numIndices;
    protected boolean dirty;
    protected boolean coordDirty;
    protected boolean colorDirty;
    protected boolean indexDirty;
    protected boolean texCoordDirty;
    protected boolean colorEnabledDirty;
    protected boolean texCoordEnabledDirty;
    protected boolean colorEnabled;
    protected boolean texCoordEnabled;
    protected List<Face> faces;
    protected IntBuffer indices;
    protected IntBuffer indicesReadOnly;
    protected Vector4Buffer colors;
    protected Vector3Buffer coords;
    protected Vector2Buffer texCoords;

    private Type type;

    /**
     * Creates an empty geometry.
     *
     * @param type - geometry type, either one of TYPE_LINES, TYPE_TRIS, or TYPE_QUADS
     * @param numCoords - number of coordinates
     * @param numIndices - number of indices
     */
    public Geometry(Type type, int numCoords, int numIndices) {
        this.type = type;
        this.numCoords = numCoords;
        this.numIndices = numIndices;

        faces = new ArrayList<>();

        colors = new Vector4Buffer(numCoords);
        coords = new Vector3Buffer(numCoords);
        texCoords = new Vector2Buffer(numCoords);
        indices = Buffers.createIntBuffer(numIndices);
        indicesReadOnly = indices.asReadOnlyBuffer();

        for (int i = 0; i < numCoords; i++) {
            colors.set(i, 1f, 1f, 1f, 1f);
        }
    }

    /**
     * Creates a geometry based on the given template.
     *
     * @param template - geometry to copy attributes from
     */
    public Geometry(Geometry template) {
        faces = new ArrayList<>(template.faces);
        colors = new Vector4Buffer(template.numCoords);
        coords = new Vector3Buffer(template.numCoords);
        texCoords = new Vector2Buffer(template.numCoords);
        indices = Buffers.createIntBuffer(template.numIndices);
        indicesReadOnly = indices.asReadOnlyBuffer();

        type = template.type;
        numCoords = template.numCoords;
        numIndices = template.numIndices;
        colorEnabled = template.colorEnabled;
        texCoordEnabled = template.texCoordEnabled;

        coords.set(template.coords);
        colors.set(template.colors);
        texCoords.set(template.texCoords);

        indices.put(template.indices).flip();
        template.indices.flip();
    }

    /**
     * Sets all attributes of this geometry to the given template.
     *
     * @param template - geometry to copy attributes from
     *
     * @return this geometry
     */
    public Geometry set(Geometry template) {
        if (template == null) {
            return this;
        }

        if (numCoords != template.numCoords) {
            colors = new Vector4Buffer(template.numCoords);
            coords = new Vector3Buffer(template.numCoords);
            texCoords = new Vector2Buffer(template.numCoords);
        }

        if (numIndices != template.numIndices) {
            indices = Buffers.createIntBuffer(template.numIndices);
            indicesReadOnly = indices.asReadOnlyBuffer();
        }

        type = template.type;
        numIndices = template.numIndices;
        numCoords = template.numCoords;

        faces.addAll(template.faces);

        setColorEnabled(template.colorEnabled);
        setTexCoordEnabled(template.texCoordEnabled);

        setIndices(template.indices);
        setColors(template.colors);
        setCoordinates(template.coords);
        setTextureCoordinates(template.texCoords);

        return this;
    }

    /**
     * Gives this geometry's type.
     *
     * @return this geometry's type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the index value at the given buffer index.
     *
     * @param i buffer index
     * @param value input value
     */
    public void setIndex(int i, int value) {
        indices.put(i, value);
        indexDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given array to this geometry's index buffer.
     *
     * @param indices - array of indices
     */
    public void setIndices(int[] indices) {
        this.indices.clear();
        this.indices.put(indices);
        this.indices.flip();

        indexDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's index buffer.
     *
     * @param indices - buffer of indices
     */
    public void setIndices(IntBuffer indices) {
        indices.clear();
        this.indices.clear();
        this.indices.put(indices);
        this.indices.flip();
        indices.flip();

        indexDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's index buffer.
     *
     * @param indices - list of indices
     */
    public void setIndices(List<Integer> indices) {
        this.indices.clear();

        for (int i = 0; i < indices.size(); i++) {
            this.indices.put(i, indices.get(i));
        }

        indexDirty = true;
        dirty = true;
    }

    /**
     * Gives the geometry index at the given list index.
     *
     * @param index - list index
     *
     * @return geometry index at the given list index.
     */
    public int getIndex(int index) {
        return indices.get(index);
    }

    /**
     * Gives the immutable buffer containing this geometry's indices.
     *
     * @return immutable buffer containing this geometry's indices
     */
    public IntBuffer getIndexBuffer() {
        return indicesReadOnly;
    }

    /**
     * Sets the coordinate at the given buffer index.
     *
     * @param i buffer index
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setCoordinate(int i, float x, float y, float z) {
        coords.set(i, x, y, z);
        coordDirty = true;
        dirty = true;
    }

    /**
     * Sets the coordinate at the given buffer index.
     *
     * @param i buffer index
     * @param coord - three-element vector coordinate
     */
    public void setCoordinate(int i, Vector3 coord) {
        setCoordinate(i, coord.getX(), coord.getY(), coord.getZ());
    }

    /**
     * Copies the contents of the given array to this geometry's coordinate buffer.
     *
     * @param coords - array of three-element vector coordinates
     */
    public void setCoordinates(Vector3[] coords) {
        this.coords.set(coords);
        coordDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's coordinate buffer.
     *
     * @param coords - buffer of three-element vector coordinates
     */
    public void setCoordinates(Vector3Buffer coords) {
        this.coords.set(coords);
        coordDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's coordinate buffer.
     *
     * @param coords - list of three-element vector coordinates
     */
    public void setCoordinates(List<Vector3> coords) {
        this.coords.set(coords);
        coordDirty = true;
        dirty = true;
    }

    /**
     * Stores the coordinate at the given index into the given output vector.
     *
     * @param index - buffer index
     * @param output - output vector
     *
     * @return the output vector
     */
    public Vector3 getCoordinate(int index, Vector3 output) {
        if (output == null) {
            output = new Vector3();
        }

        return coords.get(index, output);
    }

    /**
     * Gives the vector buffer containing the coordinates.
     *
     * @return the vector buffer containing the coordinates
     */
    public Vector3Buffer getCoordinates() {
        return coords;
    }

    /**
     * Gives the float buffer containing the coordinates.
     *
     * @return the float buffer containing the coordinates
     */
    public FloatBuffer getCoordinateBuffer() {
        return coords.toFloatBuffer();
    }

    /**
     * Sets the color at the given buffer index.
     *
     * @param index buffer index
     * @param r red component
     * @param g green component
     * @param b blue component
     * @param a alpha component
     */
    public void setColor(int index, float r, float g, float b, float a) {
        colors.set(index, r, g, b, a);
        colorDirty = true;
        dirty = true;
    }

    /**
     * Sets the color at the given buffer index.
     *
     * @param i buffer index
     * @param color - four-element vector color
     */
    public void setColor(int i, Vector4 color) {
        setColor(i, color.getX(), color.getY(), color.getZ(), color.getW());
    }

    /**
     * Copies the contents of the given array to this geometry's color buffer.
     *
     * @param colors - array of three-element vector colors
     */
    public void setColors(Vector4[] colors) {
        this.colors.set(colors);
        colorDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's color buffer.
     *
     * @param colors - buffer of three-element vector colors
     */
    public void setColors(Vector4Buffer colors) {
        this.colors.set(colors);
        colorDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's color buffer.
     *
     * @param colors - list of three-element vector colors
     */
    public void setColors(List<Vector4> colors) {
        this.colors.set(colors);
        colorDirty = true;
        dirty = true;
    }

    /**
     * Sets all colors to the given RGBA values.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     * @param a - alpha color component
     */
    public void setAllColors(float r, float g, float b, float a) {
        for (int i = 0; i < numCoords; i++) {
            colors.set(i, r, g, b, a);
        }

        colorDirty = true;
        dirty = true;
    }

    /**
     * Sets all colors to the given four-element color vector.
     *
     * @param color - four-element color vector
     */
    public void setAllColors(Vector4 color) {
        setAllColors(color.getX(), color.getY(), color.getZ(), color.getW());
    }

    /**
     * Stores the color at the given index into the given output vector.
     *
     * @param index - buffer index
     * @param output - output vector
     *
     * @return the output vector
     */
    public Vector4 getColor(int index, Vector4 output) {
        return colors.get(index, output);
    }

    /**
     * Gives the vector buffer containing the colors.
     *
     * @return the vector buffer containing the color
     */
    public Vector4Buffer getColors() {
        return colors;
    }

    /**
     * Gives the float buffer containing the colors.
     *
     * @return the float buffer containing the colors
     */
    public FloatBuffer getColorBuffer() {
        return colors.toFloatBuffer();
    }

    /**
     * Sets the texture coordinate at the given buffer index.
     *
     * @param coordIndex buffer index
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setTextureCoordinate(int coordIndex, float x, float y) {
        texCoords.set(coordIndex, x, y);
        texCoordDirty = true;
        dirty = true;
    }

    /**
     * Sets the texture coordinate at the given buffer index.
     *
     * @param i buffer index
     * @param coord - two-element vector texture coordinate
     */
    public void setTextureCoordinate(int i, Vector2 coord) {
        setTextureCoordinate(i, coord.getX(), coord.getY());
    }

    /**
     * Copies the contents of the given array to this geometry's texture coordinate buffer.
     *
     * @param texCoords - array of two-element vector texture coordinates
     */
    public void setTextureCoordinates(Vector2[] texCoords) {
        this.texCoords.set(texCoords);
        texCoordDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's texture coordinate buffer.
     *
     * @param texCoords - buffer of two-element vector texture coordinates
     */
    public void setTextureCoordinates(Vector2Buffer texCoords) {
        this.texCoords.set(texCoords);
        texCoordDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's texture coordinates buffer.
     *
     * @param texCoords - list of two-element vector texture coordinates
     */
    public void setTextureCoordinates(List<Vector2> texCoords) {
        this.texCoords.set(texCoords);
        texCoordDirty = true;
        dirty = true;
    }

    /**
     * Stores the texture coordinate at the given index into the given output vector.
     *
     * @param index - buffer index
     * @param output - output vector
     *
     * @return the output vector
     */
    public Vector2 getTextureCoordinate(int index, Vector2 output) {
        if (output == null) {
            output = new Vector2();
        }

        return texCoords.get(index, output);
    }

    /**
     * Gives the vector buffer containing the texture coordinates.
     *
     * @return the vector buffer containing the texture coordinates
     */
    public Vector2Buffer getTextureCoordinates() {
        return texCoords;
    }

    /**
     * Gives the float buffer containing the texture coordinates.
     *
     * @return the float buffer containing the texture coordinates
     */
    public FloatBuffer getTextureCoordinateBuffer() {
        return texCoords.toFloatBuffer();
    }

    /**
     * If set, color attribute is used.
     *
     * @param enabled - color attribute state
     */
    public void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
        colorEnabledDirty = true;
        dirty = true;
    }

    /**
     * Gives the state of the color attribute.
     *
     * @return color attribute state
     */
    public boolean isColorEnabled() {
        return colorEnabled;
    }

    /**
     * If set, texture coordinate attribute is used.
     *
     * @param enabled - texture coordinate attribute state
     */
    public void setTexCoordEnabled(boolean enabled) {
        texCoordEnabled = enabled;
        texCoordEnabledDirty = true;
        dirty = true;
    }

    /**
     * Gives the state of the texture coordinate attribute.
     *
     * @return texture coordinate attribute state
     */
    public boolean isTexCoordEnabled() {
        return texCoordEnabled;
    }

    /**
     * Gives the number of coordinate indices.
     *
     * @return number of coordinate indices
     */
    public int numIndices() {
        return numIndices;
    }

    /**
     * Gives the number of vertex coordinates.
     *
     * @return number of vertex coordinates
     */
    public int numCoordinates() {
        return numCoords;
    }

    /**
     * Resets all dirty flags.
     */
    public void clean() {
        if (dirty) {
            dirty = false;
            indexDirty = false;
            coordDirty = false;
            colorDirty = false;
            texCoordDirty = false;
            colorEnabledDirty = false;
            texCoordEnabledDirty = false;
        }
    }

    /**
     * Gives the dirty flag.
     *
     * @return true if this geometry has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Gives the index dirty flag.
     *
     * @return true if this geometry's indices have been modified since the last frame
     */
    public boolean isIndexDirty() {
        return indexDirty;
    }

    /**
     * Gives the coordinate dirty flag.
     *
     * @return true if this geometry's coordinates have been modified since the last frame
     */
    public boolean isCoordinateDirty() {
        return coordDirty;
    }

    /**
     * Gives the color dirty flag.
     *
     * @return true if this geometry's colors have been modified since the last frame
     */
    public boolean isColorDirty() {
        return colorDirty;
    }

    /**
     * Gives the texture coordinate dirty flag.
     *
     * @return true if this geometry's texture coordinates have been modified since the last frame
     */
    public boolean isTexCoordDirty() {
        return texCoordDirty;
    }

    /**
     * Gives the color enabled dirty flag.
     *
     * @return true if this geometry's color enabled state has been modified since the last frame
     */
    public boolean isColorEnabledDirty() {
        return colorEnabledDirty;
    }

    /**
     * Gives the texture coordinate enabled dirty flag.
     *
     * @return true if this geometry's coordinate enabled state has been modified since the last frame
     */
    public boolean isTexCoordEnabledDirty() {
        return texCoordEnabledDirty;
    }

    /**
     * Writes the given geometry to the currently active framebuffer.
     */
    public void draw() {
        switch (type) {
            case TRIS:
                GL.drawTris(id, numIndices);
                break;
            case QUADS:
                GL.drawQuads(id, numIndices);
                break;
            case LINES:
                GL.drawLines(id, numIndices);
                break;
        }
    }

    /**
     * Applies changes to a geometry instance in the graphics processor.
     */
    public void update() {
        GL.bindVertexArray(id);

        if (colorEnabledDirty) {
            GL.setAttributeEnabled(Renderer.VertexAttribute.COLOR.getIdentifier(), colorEnabled);
        }

        if (texCoordEnabledDirty) {
            GL.setAttributeEnabled(Renderer.VertexAttribute.TEXCOORD.getIdentifier(), texCoordEnabled);
        }

        if (indexDirty) {
            GL.updateIndexBuffer(indexBuffer, indices);
        }

        if (coordDirty) {
            GL.updateVertexBuffer(coordBuffer, coords.toFloatBuffer());
        }

        if (colorDirty) {
            GL.updateVertexBuffer(colorBuffer, colors.toFloatBuffer());
        }

        if (texCoordDirty) {
            GL.updateVertexBuffer(texCoordBuffer, texCoords.toFloatBuffer());
        }
    }

    /**
     * Frees all graphics resources associated with this geometry.
     */
    @Override
    protected void destroy() {
        GL.freeVertexArray(id);
        GL.freeBuffer(indexBuffer);
        GL.freeBuffer(coordBuffer);
        GL.freeBuffer(colorBuffer);
        GL.freeBuffer(texCoordBuffer);

        id = 0;
        coordBuffer = 0;
        indexBuffer = 0;
        colorBuffer = 0;
        texCoordBuffer = 0;

        clean();
    }

    /**
     * Creates an instance of the given geometry in the graphics processor.
     */
    @Override
    protected void build() {
        id = GL.createVertexArray();
        GL.bindVertexArray(id);

        indexBuffer = GL.createVertexBuffer();
        coordBuffer = GL.createVertexBuffer();
        colorBuffer = GL.createVertexBuffer();
        texCoordBuffer = GL.createVertexBuffer();

        GL.fillIndexBuffer(indexBuffer, indices);
        GL.fillVertexBuffer(coordBuffer, 3, Renderer.VertexAttribute.COORD.getIdentifier(), coords.toFloatBuffer());
        GL.fillVertexBuffer(colorBuffer, 4, Renderer.VertexAttribute.COLOR.getIdentifier(), colors.toFloatBuffer());
        GL.fillVertexBuffer(texCoordBuffer, 2, Renderer.VertexAttribute.TEXCOORD.getIdentifier(), texCoords.toFloatBuffer());

        GL.setAttributeEnabled(Renderer.VertexAttribute.COORD.getIdentifier(), true);
        GL.setAttributeEnabled(Renderer.VertexAttribute.COLOR.getIdentifier(), colorEnabled);
        GL.setAttributeEnabled(Renderer.VertexAttribute.TEXCOORD.getIdentifier(), texCoordEnabled);

        clean();
    }

    /**
     * Generates faces from the given geometry.
     *
     * @param geom - geometry to be the source of data
     * @param output - storage for the result
     *
     * @return output list of faces
     */
    public static List<Face> generateFaces(Geometry geom, List<Face> output) {
        if (output == null) {
            output = new ArrayList<>();
        }

        output.clear();

        int index = 0;
        IntBuffer indices = geom.getIndexBuffer();

        Vector3 vertex0 = Pools.Vector3.get();
        Vector3 vertex1 = Pools.Vector3.get();
        Vector3 vertex2 = Pools.Vector3.get();

        while (index < indices.limit()) {
            Face face = new Face();

            int index0 = indices.get(index++);
            int index1 = indices.get(index++);
            int index2 = indices.get(index++);

            geom.getCoordinate(index0, vertex0);
            geom.getCoordinate(index1, vertex1);
            geom.getCoordinate(index2, vertex2);

            face.setIndices(index0, index1, index2);
            face.setVertices(vertex0, vertex1, vertex2);
            face.calculateCenter();
            face.calculateNormal();

            output.add(face);
        }

        Pools.Vector3.put(vertex0);
        Pools.Vector3.put(vertex1);
        Pools.Vector3.put(vertex2);

        return output;
    }

    /**
     * Sorts all triangles from farthest to nearest to the given camera.
     *
     * @param camera - the main camera
     */
    public void sortFaces(Camera camera) {
        /**
         * Sort faces.
         */
        for (Face face : faces) {
            face.calculateDistance(camera);
        }

        Collections.sort(faces, Comparator.reverseOrder());

        indices.clear();

        for (Face face : faces) {
            indices.put(face.getIndex(0));
            indices.put(face.getIndex(1));
            indices.put(face.getIndex(2));
        }

        indices.flip();

        dirty = true;
        indexDirty = true;
    }

    /**
     * Generates faces from this geometry.
     */
    public void generateFaces() {
        faces.clear();
        generateFaces(this, faces);
    }

    /**
     * Gives the list of faces.
     *
     * @return list of faces
     */
    public List<Face> getFaces() {
        return faces;
    }
}
