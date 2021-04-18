package core;

import core.math.EngineMath;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all influencing shadows.
 */
public abstract class Shadow<S extends Light> extends CacheObject {
    public static final int DATA_SIZE = 8;

    public static final int MIN_RESOLUTION = 128;
    public static final int MAX_RESOLUTION = 4096;
    public static final int MIN_FILTER_SAMPLES = 4;
    public static final int MAX_FILTER_SAMPLES = 64;
    public static final float NEAR_CLIP = 0.1f;

    public static final int DEFAULT_RESOLUTION = 1024;
    public static final int DEFAULT_FILTER_SAMPLES = 32;
    public static final float DEFAULT_OPACITY = 0.5f;
    public static final float DEFAULT_FILTER_DENSITY = 1f;
    public static final float DEFAULT_CLIP = Camera.DEFAULT_FAR_CLIP;

    protected int numBuffers;
    protected int resolution;
    protected int filterSamples;
    protected float clip;
    protected float opacity;
    protected float filterDensity;
    protected boolean dirty;
    protected boolean resDirty;
    protected boolean enabled;
    protected boolean filterEnabled;
    protected Camera[] cameras;
    protected ShadowMap shadowMap;
    protected S source;

    /**
     * Creates a shadow with default attributes.
     */
    public Shadow(int numBuffers) {
        super(DATA_SIZE);

        this.numBuffers = numBuffers;

        enabled = true;
        clip = DEFAULT_CLIP;
        opacity = DEFAULT_OPACITY;
        resolution = DEFAULT_RESOLUTION;
        filterSamples = DEFAULT_FILTER_SAMPLES;
        filterDensity = DEFAULT_FILTER_DENSITY;
        cameras = new Camera[numBuffers];

        for (int i = 0; i < cameras.length; i++) {
            cameras[i] = new Camera();
            cameras[i].setFarClipDistance(NEAR_CLIP);
        }

        dataBuffer.put(0, opacity);
        dataBuffer.put(1, clip);
        dataBuffer.put(2, filterEnabled ? 1f : 0f);
        dataBuffer.put(3, filterSamples);
        dataBuffer.put(4, filterDensity);
        dataBuffer.put(5, 1f / (float) resolution);
    }

    /**
     * Turns this shadow on or off.
     *
     * @param enabled - true to enable this light
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        dirty = true;
    }

    /**
     * Checks if this shadow is enabled.
     *
     * @return - true if this shadow is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the distance beyond which shadows are clipped.
     *
     * @param clip - clipping distance
     */
    public void setClip(float clip) {
        this.clip = clip;
        cameras[0].setFarClipDistance(clip);
        dataBuffer.put(1, clip);
    }

    /**
     * Gives the distance beyond which shadows are clipped.
     *
     * @return clipping distance
     */
    public float getClip() {
        return clip;
    }

    /**
     * Sets a value between 0 and 1, inclusively, with 0 being fully transparent and 1 being fully opaque.
     *
     * @param opacity - value between 0 and 1, inclusively
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
        dataBuffer.put(0, opacity);
        dirty = true;
    }

    /**
     * Gives the opacity value.
     *
     * @return opacity value
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets the resolution of the shadow map. Larger values give smoother shadows. Values are clamped between MIN_RESOLUTION
     * and MAX_RESOLUTION, inclusively.
     *
     * @param resolution - shadow resolution
     */
    public void setResolution(int resolution) {
        this.resolution = EngineMath.clamp(resolution, MIN_RESOLUTION, MAX_RESOLUTION);

        shadowMap.setResolution(resolution);
        dataBuffer.put(5, 1f / (float) this.resolution);

        resDirty = true;
        dirty = true;
    }

    /**
     * Gives this shadow's resolution.
     *
     * @return shadow's resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Sets whether or not a filtering algorithm is applied to this shadow to smoothen the edges.
     *
     * @param enabled - if true, a filtering algorithm is applied to shadows to smoothen the edges
     */
    public void setFilterEnabled(boolean enabled) {
        filterEnabled = enabled;
        dataBuffer.put(2, filterEnabled ? 1f : 0f);
        dirty = true;
    }

    /**
     * Checks if filtering is applied to this shadow.
     *
     * @return true if filtering is applied to this shadow
     */
    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    /**
     * Sets the number of samples used in the filtering algorithm. Higher values give smoother shadow edges, but this
     * comes with a performance cost. Input is clamped between MIN_FILTER_SAMPLES and MAX_FILTER_SAMPLES inclusively.
     *
     * @param samples - number of samples
     */
    public void setFilterSamples(int samples) {
        filterSamples = EngineMath.clamp(samples, MIN_FILTER_SAMPLES, MAX_FILTER_SAMPLES);
        dataBuffer.put(3, filterSamples);
        dirty = true;
    }

    /**
     * Gives the number of samples used in shadow filtering.
     *
     * @return number of samples used in shadow filtering
     */
    public int getFilterSamples() {
        return filterSamples;
    }

    /**
     * Sets the sampling density used in the shadow filtering algorithm. Lower values give subtler blending around
     * shadow edges but thinner penumbra.
     *
     * @param density - "closeness" factor between samples
     */
    public void setFilterDensity(float density) {
        filterDensity = density;
        dataBuffer.put(4, filterDensity);
        dirty = true;
    }

    /**
     * Gives the sampling density used in the shadow filtering algorithm.
     *
     * @return sampling density used in the shadow filtering algorithm
     */
    public float getFilterDensity() {
        return filterDensity;
    }

    /**
     * Gives the cameras used to probe the environment from the source light's perspective.
     *
     * @return cameras used to probe the environment from the source light's perspective
     */
    public Camera[] getCameras() {
        return cameras;
    }

    /**
     * Gives the shadow map which contains depth data.
     *
     * @return - shadow map
     */
    public ShadowMap getShadowMap() {
        return shadowMap;
    }

    /**
     * Gives the number of frame buffers allocated for this shadow.
     *
     * @return number of frame buffers allocated for this shadow
     */
    public int numBuffers() {
        return numBuffers;
    }

    /**
     * Checks if this shadow has been modified since the last frame.
     *
     * @return true if this shadow has been modified since the last frame
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Checks if this shadow's resolution has been modified since the last frame.
     *
     * @return true if this shadow's resolution has been modified since the last frame
     */
    public boolean isResolutionDirty() {
        return resDirty;
    }

    /**
     * Resets this shadow's dirty flags.
     */
    public void clean() {
        for (Camera camera : cameras) {
            camera.clean();
        }

        dirty = false;
        resDirty = false;
    }

    /**
     * Gives this shadow's light source.
     *
     * @return light source
     */
    public S getSource() {
        return source;
    }

    /**
     * Called by the light source to which this shadow has been added.
     *
     * @param source - light source
     */
    public void setNotify(S source) {
        this.source = source;
    }

    /**
     * Called by the light source from which this shadow has been removed.
     */
    public void removeNotify() {
        this.source = null;
    }

    /**
     * Updates the light view-projection frustum.
     */
    public abstract void updateFrustum();
}
