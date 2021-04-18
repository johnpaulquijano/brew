package core;

import core.math.EngineMath;
import core.math.Matrix3;
import core.math.Matrix4;
import core.utility.Buffers;
import core.utility.EngineException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * Shape encapsulates geometry and material data.
 *
 * @author John Paul Quijano
 */
public class Shape extends Spatial implements Comparable<Shape> {
    public static final int MAX_SHADOWS = 4;
    public static final int MAX_LIGHTS = 128;
    public static final float DEFAULT_LOD_STEP = 40f;

    protected int lodIndex;
    protected int lodMaterial;
    protected int lodGeometry;
    protected int maxMatIndex;
    protected int maxGeomIndex;
    protected float lodStep;
    protected float camDistance;
    protected boolean lodEnabled;
    protected boolean sortEnabled;
    protected boolean shadowCaster;
    protected boolean shadowReceiver;
    protected boolean lightsDirty;
    protected Matrix3 normalMatrix;
    protected Matrix4 worldViewProjMatrix;
    protected IntBuffer lightsBuffer;
    protected IntBuffer shadowsBuffer;
    protected FloatBuffer normalBuffer;
    protected FloatBuffer worldViewProjBuffer;
    protected List<Material> materials;
    protected List<ShapeGeometry> geometries;
    protected List<Light> lights;
    protected List<Shadow> shadows;

    public Shape() {
        maxMatIndex = -1;
        maxGeomIndex = -1;
        sortEnabled = true;
        normalMatrix = new Matrix3();
        worldViewProjMatrix = new Matrix4();
        lightsBuffer = Buffers.createIntBuffer(MAX_LIGHTS);
        shadowsBuffer = Buffers.createIntBuffer(MAX_LIGHTS);
        normalBuffer = Buffers.createFloatBuffer(9);
        worldViewProjBuffer = Buffers.createFloatBuffer(16);
        materials = new ArrayList<>();
        geometries = new ArrayList<>();
        lights = new ArrayList<>();
        shadows = new ArrayList<>();

        lodStep = DEFAULT_LOD_STEP;
    }

    /**
     * If enabled, the rendering level-of-detail for this shape is chosen based on its distance from the camera.
     */
    public void setLevelOfDetailEnabled(boolean enabled) {
        lodEnabled = enabled;
    }

    /**
     * Checks if level of detail is enabled.
     *
     * @return true if level of detail is enabled
     */
    public boolean isLevelOfDetailEnabled() {
        return lodEnabled;
    }

    /**
     * Multiples of this value corresponds to the shape's distance from the camera when the level of detail changes.
     *
     * @param step - level of detail step
     */
    public void setLevelOfDetailStep(float step) {
        lodStep = step;
    }

    /**
     * Gives the level of detail step.
     *
     * @return level of detail step
     */
    public float getLevelOfDetailStep() {
        return lodStep;
    }

    /**
     * If enabled, opaque shapes are sorted from nearest to farthest from camera to take advantage of early depth culling
     * on the GPU.
     *
     * @param enabled - if true, this shape is sorted
     */
    public void setSortEnabled(boolean enabled) {
        sortEnabled = enabled;
    }

    /**
     * Checks if this shape is sorted.
     *
     * @return true if this shape is sorted
     */
    public boolean isSortEnabled() {
        return sortEnabled;
    }

    /**
     * Adds a material level of detail.
     *
     * @param material - material to add
     */
    public void addMaterialDetail(Material material) {
        materials.add(material);
        ++maxMatIndex;
    }

    /**
     * Removes the material at the given index.
     *
     * @param index - position of material in the list
     */
    public void removeMaterialDetail(int index) {
        if (materials.remove(index) != null) {
            --maxMatIndex;
        }
    }

    /**
     * Clears the list of materials.
     */
    public void removeAllMaterialDetails() {
        materials.clear();
        maxMatIndex = -1;
    }

    /**
     * Gives the material at the given index.
     *
     * @param index - position of material in the list
     *
     * @return material at the given index
     */
    public Material getMaterialDetail(int index) {
        return materials.get(index);
    }

    /**
     * Gives the number of material levels of detail.
     *
     * @return number of material levels of detail
     */
    public int numMaterialDetails() {
        return materials.size();
    }

    /**
     * Checks if this shape has at least one material.
     *
     * @return true if this shape has at least one material
     */
    public boolean hasMaterial() {
        return !materials.isEmpty();
    }

    /**
     * Gives the material level of detail index.
     *
     * @return material level of detail index
     */
    public int getMaterialLevel() {
        return lodMaterial;
    }

    /**
     * Adds a geometry level of detail.
     *
     * @param geometry - geometry to add
     */
    public void addGeometryDetail(ShapeGeometry geometry) {
        geometries.add(geometry);
        ++maxGeomIndex;
    }

    /**
     * Removes the geometry at the given index.
     *
     * @param index - position of geometry in the list
     */
    public void removeGeometryDetail(int index) {
        if (geometries.remove(index) != null) {
            --maxGeomIndex;
        }
    }

    /**
     * Clears the list of geometries.
     */
    public void removeAllGeometryDetails() {
        geometries.clear();
        maxGeomIndex = -1;
    }

    /**
     * Gives the geometry at the given index.
     *
     * @param index - position of geometry in the list
     *
     * @return geometry at the given index
     */
    public ShapeGeometry getGeometryDetail(int index) {
        return geometries.get(index);
    }

    /**
     * Gives the number of geometry levels of detail.
     *
     * @return number of geometry levels of detail
     */
    public int numGeometryDetails() {
        return geometries.size();
    }

    /**
     * Checks if this shape has at least one geometry.
     *
     * @return true if this shape has at least one geometry
     */
    public boolean hasGeometry() {
        return !geometries.isEmpty();
    }

    /**
     * Gives the geometry level of detail index.
     *
     * @return geometry level of detail index
     */
    public int getGeometryLevel() {
        return lodGeometry;
    }

    /**
     * Updates the bounds to tightly contain the given geometry.
     *
     * @param geometry - geometry to calculate bounds from
     */
    public void calculateBounds(ShapeGeometry geometry) {
        localBoundingBox.fromCoordinates(geometry.getCoordinates());
        boundsDirty = true;
    }

    /**
     * Calculates the level-of-detail based on the distance from the given camera.
     *
     * @param camera - camera to calculate distance from
     */
    public void calculateLevelOfDetail(Camera camera) {
        camDistance = worldBoundingBox.edgeDistance(camera.getLocation());

        if (lodEnabled) {
            lodIndex = (int) (camDistance / lodStep);

            lodMaterial = EngineMath.clamp(lodIndex, 0, maxMatIndex);
            lodGeometry = EngineMath.clamp(lodIndex, 0, maxGeomIndex);
        } else {
            lodMaterial = 0;
            lodGeometry = 0;
        }
    }

    /**
     * Calculates this shape's world, normal, and world-view-projection transformations.
     *
     * @param camera - main viewing camera
     */
    public void calculateTransforms(Camera camera) {
        if (transformDirty) {
            try {
                worldMatrix.toMatrix3(normalMatrix).invert().transpose().toFloatBuffer(normalBuffer);
            } catch (EngineException ex) {
                /** Ignore exception when matrix cannot be inverted. */
            }
        }

        if (camera.isDirty() || transformDirty) {
            worldViewProjMatrix.set(worldMatrix).multiply(camera.getViewProjectionMatrix()).toFloatBuffer(worldViewProjBuffer);
        }
    }

    /**
     * Gives the normal transformation matrix.
     *
     * @return normal transformation matrix
     */
    public Matrix3 getNormalTransformMatrix() {
        return normalMatrix;
    }

    /**
     * Gives the world-view-projection transformation matrix.
     *
     * @return world-view-projection transformation matrix
     */
    public Matrix4 getWorldViewProjectionTransformMatrix() {
        return worldViewProjMatrix;
    }

    /**
     * Gives the float buffer containing the world-view-projection transformation matrix values.
     *
     * @return float buffer containing the world-view-projection transformation matrix values
     */
    public FloatBuffer getWorldViewProjectionTransformBuffer() {
        return worldViewProjBuffer;
    }

    /**
     * Gives the float buffer containing normal transformation matrix values.
     *
     * @return float buffer containing normal transformation matrix values
     */
    public FloatBuffer getNormalTransformBuffer() {
        return normalBuffer;
    }

    /**
     * Adds an influencing light.
     *
     * @param light - influencing light
     */
    public void addLight(Light light) {
        if (light != null) {
            lights.add(light);

            if (lights.size() > MAX_LIGHTS) {
                throw new EngineException("Maximum number of lights per shape exceeded.");
            }

            if (light.getShadow() != null) {
                shadows.add(light.getShadow());
            }

            lightsDirty = true;
        }
    }

    /**
     * Removes the given influencing light.
     *
     * @param light - light to remove
     */
    public void removeLight(Light light) {
        lightsDirty = lights.remove(light);
    }

    /**
     * Gives the set of influencing lights.
     *
     * @return set of influencing lights
     */
    public List<Light> getLights() {
        return lights;
    }

    /**
     * Gives the set of influencing shadows.
     *
     * @return set of influencing shadows
     */
    public List<Shadow> getShadows() {
        return shadows;
    }

    /**
     * Gives the buffer containing cache light indices.
     *
     * @return buffer containing cache light indices
     */
    public IntBuffer getLightsBuffer() {
        if (lightsDirty) {
            lightsBuffer.clear();

            for (Light light : lights) {
                lightsBuffer.put(light.getIndex());
            }

            lightsBuffer.flip();
        }

        return lightsBuffer;
    }

    /**
     * Gives the buffer containing cache shadow indices.
     *
     * @return buffer containing cache shadow indices
     */
    public IntBuffer getShadowsBuffer() {
        if (lightsDirty) {
            shadowsBuffer.clear();

            for (Light light : lights) {
                if (light.getShadow() != null) {
                    shadowsBuffer.put(light.getShadow().getIndex());
                } else {
                    shadowsBuffer.put(-1);
                }
            }

            shadowsBuffer.flip();
        }

        return shadowsBuffer;
    }

    /**
     * Sets if this shape should cast shadow on shadow receivers.
     */
    public void setShadowCaster(boolean caster) {
        shadowCaster = caster;
    }

    /**
     * Checks if this shape casts shadow on shadow receivers.
     *
     * @return true if this shape casts shadow on shadow receivers
     */
    public boolean isShadowCaster() {
        return shadowCaster;
    }

    /**
     * Sets this shape should be shadowed by shadow casters.
     */
    public void setShadowReceiver(boolean receiver) {
        shadowReceiver = receiver;
    }

    /**
     * Checks this shape should be shadowed by shadow casters.
     *
     * @return true if this shape should be shadowed by shadow casters
     */
    public boolean isShadowReceiver() {
        return shadowReceiver;
    }

    /**
     * Checks if this shape has all the prerequisites for lighting. These prerequisites are:
     *     - shape contains material
     *     - shape contains lights
     *     - lighting enabled in material
     *     - normals enabled in geometry
     *
     * @return true if this shape has all the prerequisites for lighting
     */
    public boolean isLightingReady() {
        return !materials.isEmpty() && !lights.isEmpty() && materials.get(lodMaterial).isLightingEnabled() && geometries.get(lodGeometry).isNormalEnabled();
    }

    /**
     * Checks if this shape has all the prerequisites for texturing. These prerequisites are:
     *     - material has textures
     *     - texture coordinates enabled in geometry
     *
     * @return true if this shape has all the prerequisites for texturing
     */
    public boolean isTexturingReady() {
        return materials.get(lodMaterial).getTextures().size() > 0 && geometries.get(lodGeometry).isTexCoordEnabled();
    }

    /**
     * Checks if this shape has all the prerequisites for normal mapping. These prerequisites are:
     *     - lighting ready
     *     - material contains normal map
     *     - texture coordinates enabled in geometry
     *     - tangents enabled in geometry
     *
     * @return true if this shape has all the prerequisites for normal mapping
     */
    public boolean isNormalMappingReady() {
        return isLightingReady() && materials.get(lodMaterial).getNormalMap() != null && geometries.get(lodGeometry).isTexCoordEnabled() && geometries.get(lodGeometry).isTangentEnabled();
    }

    /**
     * Checks if this shape has all the prerequisites for animation. These prerequisites are:
     *     - geometry contains animation
     *     - joints enabled in geometry
     *
     * @return true if this shape is being animated
     */
    public boolean isAnimationReady() {
        ShapeGeometry geometry = geometries.get(lodGeometry);
        return geometry.getAnimation() != null && geometry.isJointEnabled();
    }

    @Override
    public Shape copy() {
        Shape clone = new Shape();

        clone.set(this);
        clone.parent = null;

        clone.lodStep = lodStep;
        clone.lodEnabled = lodEnabled;
        clone.sortEnabled = sortEnabled;

        clone.maxMatIndex = maxMatIndex;
        clone.maxGeomIndex = maxGeomIndex;

        clone.lights.addAll(lights);
        clone.materials.addAll(materials);
        clone.geometries.addAll(geometries);

        return clone;
    }

    @Override
    public Shape deepCopy() {
        return copy();
    }

    @Override
    public void clean() {
        super.clean();

        lightsDirty = false;
    }

    @Override
    public int compareTo(Shape shape) {
        if (camDistance > shape.camDistance) {
            return 1;
        }

        if (camDistance < shape.camDistance) {
            return -1;
        }

        return 0;
    }

    @Override
    public boolean addChild(Spatial child) {
        throw new EngineException("Shape can only be a leaf node.");
    }
}
