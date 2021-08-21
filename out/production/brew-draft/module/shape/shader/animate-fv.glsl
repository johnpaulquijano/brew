vec4 pose = position;

if (enabled) {
    vec4 accum = vec4(0);

    for (int i = 0; i < JOINTS_PER_VERTEX; i++) {
        if (joint[i] >= 0) {
            accum += joints[joint[i]].transform * position * weight[i];
        }
    }

    pose = accum;
}

return pose;
