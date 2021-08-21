vec4 blur = texture(sampler, texCoord) * weights[0];

for (int i = 1; i < kernel; i++) {
    vec2 offset = vec2(0);

    if (direction == BLUR_HORIZONTAL) {
        offset.x = offsets[i] * fragSize.x;
    } else if (direction == BLUR_VERTICAL) {
        offset.y = offsets[i] * fragSize.y;
    }

    blur += texture(sampler, texCoord + offset) * weights[i];
    blur += texture(sampler, texCoord - offset) * weights[i];
}

return blur;
