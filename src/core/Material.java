package core;

import core.math.EngineMath;
import core.math.Vector3;
import core.utility.Buffers;
import core.utility.Colors;
import core.utility.EngineException;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates attributes that define a shape's appearance.
 *
 * @author John Paul Quijano
 */
public class Material extends CacheObject {
    public static final int DATA_SIZE = 24;
    public static final int MAX_TEXTURES = 4;
    public static final float THICKNESS_SCALE = 0.03f;

    public static final float DEFAULT_OPACITY = 1f;
    public static final float DEFAULT_LINE_WIDTH = 1f;
    public static final float DEFAULT_POINT_SIZE = 1f;
    public static final float DEFAULT_SHININESS = 0.5f;
    public static final float DEFAULT_REFRACTION_INDEX = 1.33f;
    public static final float DEFAULT_CONTOUR_THICKNESS = 1f;
    public static final int DEFAULT_SHADING_LEVEL = 0;
    public static final Vector3 DEFAULT_DIFFUSE = new Vector3(Colors.WHITE3);
    public static final Vector3 DEFAULT_SPECULAR = new Vector3(Colors.WHITE3);
    public static final Vector3 DEFAULT_AMBIENT = new Vector3(Colors.DARK_GRAY3);
    public static final Vector3 DEFAULT_EMISSIVE = new Vector3(Colors.BLACK3);
    public static final Vector3 DEFAULT_CONTOUR_COLOR = new Vector3(Colors.BLACK3);
    public static final GL.PolygonMode DEFAULT_POLYGON_MODE = GL.PolygonMode.FILL;

    private int shadingLevel;
    private float opacity;
    private float lineWidth;
    private float pointSize;
    private float shininess;
    private float refractionIndex;
    private float contourThickness;
    private boolean dirty;
    private boolean textureDirty;
    private boolean useGeomColor;
    private boolean contourEnabled;
    private boolean lightingEnabled;
    private boolean normalFlipEnabled;
    private boolean faceCullingEnabled;
    private boolean normalMapEnabled;
    private boolean specularMapEnabled;
    private boolean glowMapEnabled;
    private boolean reflectionEnabled;
    private boolean refractionEnabled;
    private Vector3 ambient;
    private Vector3 diffuse;
    private Vector3 specular;
    private Vector3 emissive;
    private Vector3 contourColor;
    private Texture glowMap;
    private Texture specularMap;
    private Texture normalMap;
    private List<Texture> textures;
    private GL.PolygonMode polygonMode;
    private IntBuffer blendModesBuffer;

    /**
     * Creates a material with default attributes.
     */
    public Material() {
        super(DATA_SIZE);

        lightingEnabled = true;
        faceCullingEnabled = true;

        polygonMode = DEFAULT_POLYGON_MODE;
        lineWidth = DEFAULT_LINE_WIDTH;
        pointSize = DEFAULT_POINT_SIZE;
        shininess = DEFAULT_SHININESS;
        opacity = DEFAULT_OPACITY;
        shadingLevel = DEFAULT_SHADING_LEVEL;
        refractionIndex = DEFAULT_REFRACTION_INDEX;
        contourThickness = DEFAULT_CONTOUR_THICKNESS;

        diffuse = new Vector3(DEFAULT_DIFFUSE);
        specular = new Vector3(DEFAULT_SPECULAR);
        ambient = new Vector3(DEFAULT_AMBIENT);
        emissive = new Vector3(DEFAULT_EMISSIVE);
        contourColor = new Vector3(DEFAULT_CONTOUR_COLOR);

        textures = new ArrayList<>();
        blendModesBuffer = Buffers.createIntBuffer(MAX_TEXTURES);

        dataBuffer.put(0, ambient.getX()).put(1, ambient.getY()).put(2, ambient.getZ());
        dataBuffer.put(3, shininess);
        dataBuffer.put(4, diffuse.getX()).put(5, diffuse.getY()).put(6, diffuse.getZ());
        dataBuffer.put(7, opacity);
        dataBuffer.put(8, specular.getX()).put(9, specular.getY()).put(10, specular.getZ());
        dataBuffer.put(11, normalFlipEnabled ? 1f : 0f);
        dataBuffer.put(12, emissive.getX()).put(13, emissive.getY()).put(14, emissive.getZ());
        dataBuffer.put(15, useGeomColor ? 1f : 0f);
        dataBuffer.put(16, reflectionEnabled ? 1f : 0f);
        dataBuffer.put(17, refractionEnabled ? 1f : 0f);
        dataBuffer.put(18, refractionIndex);
        dataBuffer.put(19, specularMap != null ? 1f : 0f);
        dataBuffer.put(20, normalMap != null ? 1f : 0f);
        dataBuffer.put(21, glowMap != null ? 1f : 0f);
        dataBuffer.put(22, shadingLevel);
    }

    /**
     * Creates a material from the given templates.
     *
     * @param template - material to copy attributes from
     */
    public Material(Material template) {
        super(DATA_SIZE);

        ambient = new Vector3(template.ambient);
        diffuse = new Vector3(template.diffuse);
        specular = new Vector3(template.specular);
        emissive = new Vector3(template.emissive);
        contourColor = new Vector3(template.contourColor);

        polygonMode = template.polygonMode;

        opacity = template.opacity;
        lineWidth = template.lineWidth;
        pointSize = template.pointSize;
        shininess = template.shininess;

        lightingEnabled = template.lightingEnabled;
        normalFlipEnabled = template.normalFlipEnabled;
        faceCullingEnabled = template.faceCullingEnabled;
        useGeomColor = template.useGeomColor;

        reflectionEnabled = template.reflectionEnabled;
        refractionEnabled = template.refractionEnabled;
        refractionIndex = template.refractionIndex;

        contourThickness = template.contourThickness;

        textures = new ArrayList<>();
        textures.addAll(template.textures);

        blendModesBuffer = Buffers.createIntBuffer(MAX_TEXTURES);

        dataBuffer.put(0, ambient.getX()).put(1, ambient.getY()).put(2, ambient.getZ());
        dataBuffer.put(3, shininess);
        dataBuffer.put(4, diffuse.getX()).put(5, diffuse.getY()).put(6, diffuse.getZ());
        dataBuffer.put(7, opacity);
        dataBuffer.put(8, specular.getX()).put(9, specular.getY()).put(10, specular.getZ());
        dataBuffer.put(11, normalFlipEnabled ? 1f : 0f);
        dataBuffer.put(12, emissive.getX()).put(13, emissive.getY()).put(14, emissive.getZ());
        dataBuffer.put(15, useGeomColor ? 1f : 0f);
        dataBuffer.put(16, reflectionEnabled ? 1f : 0f);
        dataBuffer.put(17, refractionEnabled ? 1f : 0f);
        dataBuffer.put(18, refractionIndex);
        dataBuffer.put(19, specularMap != null ? 1f : 0f);
        dataBuffer.put(20, normalMap != null ? 1f : 0f);
        dataBuffer.put(21, glowMap != null ? 1f : 0f);
        dataBuffer.put(22, shadingLevel);
        dataBuffer.put(23, contourEnabled ? 1f : 0f);
    }

    /**
     * Sets all attributes of this material to the given template.
     *
     * @param template - material to copy attributes from
     *
     * @return this material
     */
    public Material set(Material template) {
        if (template == null) {
            return this;
        }

        opacity = template.opacity;
        lineWidth = template.lineWidth;
        pointSize = template.pointSize;
        shininess = template.shininess;
        polygonMode = template.polygonMode;
        normalFlipEnabled = template.normalFlipEnabled;
        faceCullingEnabled = template.faceCullingEnabled;
        useGeomColor = template.useGeomColor;

        ambient.set(template.ambient);
        diffuse.set(template.diffuse);
        specular.set(template.specular);
        emissive.set(template.emissive);
        contourColor.set(template.contourColor);

        reflectionEnabled = template.reflectionEnabled;
        refractionEnabled = template.refractionEnabled;
        refractionIndex = template.refractionIndex;

        contourThickness = template.contourThickness;

        textures.addAll(template.textures);

        dataBuffer.put(0, ambient.getX()).put(1, ambient.getY()).put(2, ambient.getZ());
        dataBuffer.put(3, shininess);
        dataBuffer.put(4, diffuse.getX()).put(5, diffuse.getY()).put(6, diffuse.getZ());
        dataBuffer.put(7, opacity);
        dataBuffer.put(8, specular.getX()).put(9, specular.getY()).put(10, specular.getZ());
        dataBuffer.put(11, normalFlipEnabled ? 1f : 0f);
        dataBuffer.put(12, emissive.getX()).put(13, emissive.getY()).put(14, emissive.getZ());
        dataBuffer.put(15, useGeomColor ? 1f : 0f);
        dataBuffer.put(16, reflectionEnabled ? 1f : 0f);
        dataBuffer.put(17, refractionEnabled ? 1f : 0f);
        dataBuffer.put(18, refractionIndex);
        dataBuffer.put(19, specularMap != null ? 1f : 0f);
        dataBuffer.put(20, normalMap != null ? 1f : 0f);
        dataBuffer.put(21, glowMap != null ? 1f : 0f);
        dataBuffer.put(22, shadingLevel);
        dataBuffer.put(23, contourEnabled ? 1f : 0f);

        return this;
    }

    /**
     * If set, back-facing polygons are culled. This feature is enabled by default.
     *
     * @param enabled - back facing polygons are culled if true
     */
    public void setFaceCullingEnabled(boolean enabled) {
        faceCullingEnabled = enabled;
    }

    /**
     * Gives the face culling state.
     *
     * @return face culling state
     */
    public boolean isFaceCullingEnabled() {
        return faceCullingEnabled;
    }

    /**
     * Sets whether polygons are rendered as triangles, lines, or points.
     *
     * @param mode - the polygon mode
     */
    public void setPolygonMode(GL.PolygonMode mode) {
        polygonMode = mode;
    }

    /**
     * Gives the polygon rendering mode.
     *
     * @return polygon rendering mode
     */
    public GL.PolygonMode getPolygonMode() {
        return polygonMode;
    }

    /**
     * Sets the line thickness when rendering polygons as lines.
     *
     * @param width - line thickness
     */
    public void setLineWidth(float width) {
        lineWidth = width;
    }

    /**
     * Gives the line thickness.
     *
     * @return line thickness
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the point numCached when rendering polygons as points.
     *
     * @param size - point thickness
     */
    public void setPointSize(float size) {
        pointSize = size;
    }

    /**
     * Gives the point thickness.
     *
     * @return point thickness
     */
    public float getPointSize() {
        return pointSize;
    }

    /**
     * Sets a constant surface color.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setAmbientColor(float r, float g, float b) {
        ambient.set(r, g, b);
        dataBuffer.put(0, r).put(1, g).put(2, b);
        dirty = true;
    }

    /**
     * Sets a constant surface color.
     *
     * @param color - a three-component color vector
     */
    public void setAmbientColor(Vector3 color) {
        setAmbientColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the ambient color.
     *
     * @return a three-component color vector
     */
    public Vector3 getAmbientColor() {
        return ambient;
    }

    /**
     * Sets the color of the object's surface.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setDiffuseColor(float r, float g, float b) {
        diffuse.set(r, g, b);
        dataBuffer.put(4, r).put(5, g).put(6, b);
        dirty = true;
    }

    /**
     * Sets the color of the object's surface.
     *
     * @param color - a three-component color vector
     */
    public void setDiffuseColor(Vector3 color) {
        setDiffuseColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the diffuse color.
     *
     * @return a three-component color vector
     */
    public Vector3 getDiffuseColor() {
        return diffuse;
    }

    /**
     * Sets the color of the specular reflection.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setSpecularColor(float r, float g, float b) {
        specular.set(r, g, b);
        dataBuffer.put(8, r).put(9, g).put(10, b);
        dirty = true;
    }

    /**
     * Sets the color of the specular reflection.
     *
     * @param color - a three-component color vector
     */
    public void setSpecularColor(Vector3 color) {
        setSpecularColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the specular color.
     *
     * @return a three-component color vector
     */
    public Vector3 getSpecularColor() {
        return specular;
    }

    /**
     * Sets the color radiating from light emitting objects.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setEmissiveColor(float r, float g, float b) {
        emissive.set(r, g, b);
        dataBuffer.put(12, r).put(13, g).put(14, b);
        dirty = true;
    }

    /**
     * Sets the color radiating from light emitting objects.
     *
     * @param color - a three-component color vector
     */
    public void setEmissiveColor(Vector3 color) {
        setEmissiveColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the emissive color.
     *
     * @return a three-component color vector
     */
    public Vector3 getEmissiveColor() {
        return emissive;
    }

    /**
     * A value between 0 and 1, inclusive, which determines the numCached of the specular reflection. Higher values give
     * smaller specular reflection.
     *
     * @param shininess - a value between 0 and 1, inclusive
     */
    public void setShininess(float shininess) {
        this.shininess = EngineMath.clamp(shininess, EngineMath.EPSILON, 1f);
        dataBuffer.put(3, this.shininess);
        dirty = true;
    }

    /**
     * Gives the shininess value.
     *
     * @return shininess value
     */
    public float getShininess() {
        return shininess;
    }

    /**
     * If set, polygons get lit given all other lighting prerequisites are met.
     *
     * @param enabled - if true, objects are lit
     */
    public void setLightingEnabled(boolean enabled) {
        lightingEnabled = enabled;
    }

    /**
     * Gives the lighting enabled state.
     *
     * @return lighting enabled state
     */
    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    /**
     * If set, back facing polygons are lit.
     *
     * @param enabled - if true, object's back faces are lit
     */
    public void setNormalFlipEnabled(boolean enabled) {
        normalFlipEnabled = enabled;
        dataBuffer.put(11, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Sets the normal flip state.
     *
     * @return normal flip state
     */
    public boolean isNormalFlipEnabled() {
        return normalFlipEnabled;
    }

    /**
     * If enabled, vertex colors are used as the diffuse color.
     *
     * @param use - if true, vertex colors are used as the diffuse color
     */
    public void useGeometryColor(boolean use) {
        useGeomColor = use;
        dataBuffer.put(15, use ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives the geometry color usage state.
     *
     * @return geometry color usage state
     */
    public boolean isGeometryColorUsed() {
        return useGeomColor;
    }

    /**
     * A value between 0 and 1 inclusive, 1 being opaque and 0 being transparent.
     *
     * @param opacity - a value between 0 and 1, inclusive
     */
    public void setOpacity(float opacity) {
        this.opacity = EngineMath.clamp(opacity, 0f, 1f);
        dataBuffer.put(7, this.opacity);
        dirty = true;
    }

    /**
     * Gives the opacity.
     *
     * @return the opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets normal map enabled state.
     *
     * @param enabled - normal map enabled state
     */
    public void setNormalMapEnabled(boolean enabled) {
        normalMapEnabled = enabled;
        dataBuffer.put(20, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives normal map enabled state.
     *
     * @return normal map enabled state
     */
    public boolean isNormalMapEnabled() {
        return normalMapEnabled;
    }

    /**
     * Sets specular map enabled state.
     *
     * @param enabled - specular map enabled state
     */
    public void setSpecularMapEnabled(boolean enabled) {
        specularMapEnabled = enabled;
        dataBuffer.put(19, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives specular map enabled state.
     *
     * @return specular map enabled state
     */
    public boolean isSpecularMapEnabled() {
        return specularMapEnabled;
    }

    /**
     * Sets glow map enabled state.
     *
     * @param enabled - glow map enabled state
     */
    public void setGlowMapEnabled(boolean enabled) {
        glowMapEnabled = enabled;
        dataBuffer.put(21, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives glow map enabled state.
     *
     * @return glow map enabled state
     */
    public boolean isGlowMapEnabled() {
        return glowMapEnabled;
    }

    /**
     * Emulates a reflective surface. Shininess affects the appearance of the rendered reflection.
     *
     * @param enabled - if true, reflection is rendered on the object's surface
     */
    public void setReflectionEnabled(boolean enabled) {
        reflectionEnabled = enabled;
        dataBuffer.put(16, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives the reflection enabled state.
     *
     * @return reflection enabled state
     */
    public boolean isReflectionEnabled() {
        return reflectionEnabled;
    }

    /**
     * Emulates a refractive surface. Opacity affects the appearance of the rendered refraction.
     *
     * @param enabled - if true, refraction is rendered on the object's surface
     */
    public void setRefractionEnabled(boolean enabled) {
        refractionEnabled = enabled;
        dataBuffer.put(17, enabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Gives the refraction enabled state.
     *
     * @return refraction enabled state
     */
    public boolean isRefractionEnabled() {
        return refractionEnabled;
    }

    /**
     * Determines the degree of light bending when it passes through an object.
     *
     * @index - the refraction index
     */
    public void setRefractionIndex(float index) {
        refractionIndex = index;
        dataBuffer.put(18, index);
        dirty = true;
    }

    /**
     * Gives the refraction index.
     *
     * @return refraction index
     */
    public float getRefractionIndex() {
        return refractionIndex;
    }

    /**
     * Adds the given texture to this material's list of textures.
     *
     * @param texture - texture to add
     */
    public void addTexture(Texture texture) {
        if (textures.size() == MAX_TEXTURES) {
            throw new EngineException("Maximum number of texture reached: " + MAX_TEXTURES);
        }

        if (!textures.contains(texture)) {
            textures.add(texture);
            textureDirty = true;
            dirty = true;
        }
    }

    /**
     * Removes the given texture from this material's list of texture.
     *
     * @param texture - texture to remove
     */
    public void removeTexture(Texture texture) {
        textureDirty = textures.remove(texture);
        dirty = textureDirty;
    }

    /**
     * Removes the texture at the given index from this material's list of texture.
     *
     * @param index - index of the texture to remove
     */
    public void removeTexture(int index) {
        textureDirty = textures.remove(index) != null;
        dirty = textureDirty;
    }

    /**
     * Clears this material's list of textures.
     */
    public void removeAllTextures() {
        textures.clear();
    }

    /**
     * Gives the number of textures in this material.
     *
     * @return number of textures
     */
    public List<Texture> getTextures() {
        return textures;
    }

    /**
     * Updates then returns the buffer containing blend modes data.
     */
    public IntBuffer getBlendModesBuffer() {
        if (textureDirty) {
            blendModesBuffer.clear();

            for (Texture texture : textures) {
                blendModesBuffer.put(texture.getBlendMode().getValue());
            }

            blendModesBuffer.flip();
        }

        return blendModesBuffer;
    }

    /**
     * Sets a specular texture which enables variable regions of the object to have specular reflections.
     *
     * @param map - the specular texture
     */
    public void setSpecularMap(Texture map) {
        specularMap = map;
    }

    /**
     * Gives the specual texture.
     *
     * @return specualar texture
     */
    public Texture getSpecularMap() {
        return specularMap;
    }

    /**
     * Sets a normal texture which replaces the per-vertex normals to emulate fine surface details or bumps.
     *
     * @param map - the normal texture
     */
    public void setNormalMap(Texture map) {
        normalMap = map;
    }

    /**
     * Gives the normal texture.
     *
     * @return normal texture
     */
    public Texture getNormalMap() {
        return normalMap;
    }

    /**
     * Sets a texture encoded with parts of the object that should glow.
     *
     * @param map - the glow texture
     */
    public void setGlowMap(Texture map) {
        glowMap = map;
    }

    /**
     * Gives the glow texture.
     *
     * @return glow texture
     */
    public Texture getGlowMap() {
        return glowMap;
    }

    /**
     * Sets the shading smoothness. A shading level of 0 yields smooth gradient which is the default. Values greater
     * than 0 yield course gradient where the number of hues used is the given value.
     *
     * @param level - the number of hues used in the shading
     */
    public void setShadingLevel(int level) {
        shadingLevel = level;
        dataBuffer.put(22, shadingLevel);
        dirty = true;
    }

    /**
     * Gives the number of hues used in shading an object.
     *
     * @return number of hues used in shading an object
     */
    public float getShadingLevel() {
        return shadingLevel;
    }

    /**
     * Sets the contour rendering state.
     *
     * @param enabled - if true, contour is drawn on shapes
     */
    public void setContourEnabled(boolean enabled) {
        contourEnabled = enabled;
        dataBuffer.put(23, contourEnabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Checks if contour rendering is enabled.
     *
     * @return true if contour rendering is enabled
     */
    public boolean isContourEnabled() {
        return contourEnabled;
    }

    /**
     * Sets the contour color.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setContourColor(float r, float g, float b) {
        contourColor.set(r, g, b);
    }

    /**
     * Sets the contour color.
     *
     * @param color - 3-component color vector
     */
    public void setContourColor(Vector3 color) {
        setContourColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the contour color.
     *
     * @return contour color
     */
    public Vector3 getContourColor() {
        return contourColor;
    }

    /**
     * Sets the thickness of the contour lines.
     *
     * @param thickness - thickness of the contour lines
     */
    public void setContourThickness(float thickness) {
        contourThickness = thickness;
    }

    /**
     * Gives the thickness of the contour lines.
     *
     * @return thickness of the contour lines
     */
    public float getContourThickness() {
        return contourThickness;
    }

    /**
     * Gives this material's dirty flag.
     *
     * @return dirty flag
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Resets this material's dirty flag.
     */
    public void clean() {
        dirty = false;
        textureDirty = false;
    }
}
