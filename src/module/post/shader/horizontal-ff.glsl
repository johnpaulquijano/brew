vec4 blur = texture(sampler, texCoord) * weights[0];

for (int i = 1; i < kernel; i++) {
    vec2 offset = vec2(0f);

    offset.x = offsets[i] * fragSize.x;

    blur += texture(sampler, texCoord + offset) * weights[i];
    blur += texture(sampler, texCoord - offset) * weights[i];
}

return blur;
