vec3 pose_normal = normal;

if (enabled) {
    vec3 accum = vec3(0);

    for (int i = 0; i < JOINTS_PER_VERTEX; i++) {
        if (joint[i] >= 0) {
            accum += mat3(joints[joint[i]].transform) * normal * weight[i];
        }
    }

    pose_normal = accum;
}

return pose_normal;
