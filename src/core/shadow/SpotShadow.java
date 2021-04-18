package core.shadow;

import core.Camera;
import core.math.Vector3;
import core.Shadow;
import core.ShadowMap;
import core.light.SpotLight;

/**
 * Shadow produced by a spot light.
 */
public class SpotShadow extends Shadow<SpotLight> {
    public SpotShadow() {
        super(1);

        shadowMap = new ShadowMap(resolution, false);

        cameras[0].setProjection(Camera.Projection.PERSPECTIVE);
        cameras[0].setPerspectiveFrustum(SpotLight.DEFAULT_CUTOFF, 1f, NEAR_CLIP, DEFAULT_CLIP);
    }

    public void updateFrustum() {
        if (source.isDirty()) {
            if (source.isLocationDirty()) {
                cameras[0].setLocation(source.getLocation());
            }

            if (source.isDirectionDirty()) {
                cameras[0].lookAlong(source.getDirection(), Vector3.UNIT_Y);
            }

            if (source.isCutoffDirty()) {
                cameras[0].setPerspectiveFrustum(source.getCutoff(), 1f, NEAR_CLIP, clip);
            }
        }

        if (cameras[0].isDirty()) {
            cameras[0].updateViewProjection();
        }
    }
}
