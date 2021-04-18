package core.shadow;

import core.Camera;
import core.light.PointLight;
import core.math.EngineMath;
import core.math.Vector3;
import core.Shadow;
import core.ShadowMap;

/**
 * Shadow produced by a point light.
 */
public class PointShadow extends Shadow<PointLight> {
    public PointShadow() {
        super(6);

        shadowMap = new ShadowMap(resolution, true);

        for (int i= 0; i < 6; i++) {
            cameras[i].setProjection(Camera.Projection.PERSPECTIVE);
            cameras[i].setPerspectiveFrustum(EngineMath.HALF_PI, 1f, NEAR_CLIP, DEFAULT_CLIP);
        }

        cameras[0].lookAlong(Vector3.UNIT_X, Vector3.UNIT_Y);
        cameras[1].lookAlong(Vector3.NEG_UNIT_X, Vector3.UNIT_Y);
        cameras[2].lookAlong(Vector3.UNIT_Y, Vector3.NEG_UNIT_Z);
        cameras[3].lookAlong(Vector3.NEG_UNIT_Y, Vector3.UNIT_Z);
        cameras[4].lookAlong(Vector3.UNIT_Z, Vector3.UNIT_Y);
        cameras[5].lookAlong(Vector3.NEG_UNIT_Z, Vector3.UNIT_Y);
    }

    @Override
    public void setClip(float clip) {
        super.setClip(clip);

        for (int i= 1; i < 6; i++) {
            cameras[i].setFarClipDistance(clip);
        }
    }

    @Override
    public void updateFrustum() {
        if (source.isLocationDirty()) {
            for (int i = 0; i < 6; i++) {
                cameras[i].setLocation(source.getLocation());
            }
        }

        for (int i = 0; i < 6; i++) {
            if (cameras[i].isDirty()) {
                cameras[i].updateViewProjection();
            }
        }
    }
}
