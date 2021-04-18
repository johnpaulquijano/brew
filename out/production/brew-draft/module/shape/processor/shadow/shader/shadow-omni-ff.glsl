SHADOW shadow = shadows[shadowList[index]];
LIGHT source = lights[shadowSourceList[index]];

float factor = 1f;
float clipDist = shadow.clip - SHADOW_NEAR_CLIP;
float lightToFragDist = length(lightToFragDir);
float samplingDepth = shadowOmniDepth(index, lightToFragDir, clipDist);
float attenuation = source.attenuation.x + source.attenuation.y * lightToFragDist + source.attenuation.z * lightToFragDist * lightToFragDist;
vec4 shadowCoord = vec4(lightToFragNorm, samplingDepth);

factor = texture(shadowSamplersOmni[index], shadowCoord);

if (shadow.filtered) {
    const int filterSamples = 20;

    const vec3 offsets[filterSamples] = vec3[]
    (
        vec3( 1,  1,  1), vec3( 1, -1,  1), vec3(-1, -1,  1), vec3(-1,  1,  1),
        vec3( 1,  1, -1), vec3( 1, -1, -1), vec3(-1, -1, -1), vec3(-1,  1, -1),
        vec3( 1,  1,  0), vec3( 1, -1,  0), vec3(-1, -1,  0), vec3(-1,  1,  0),
        vec3( 1,  0,  1), vec3(-1,  0,  1), vec3( 1,  0, -1), vec3(-1,  0, -1),
        vec3( 0,  1,  1), vec3( 0, -1,  1), vec3( 0, -1, -1), vec3( 0,  1, -1)
    );

    for (int i = 0; i < filterSamples; i++) {
        vec3 offsetDir = lightToFragDir + offsets[i] * shadow.filterDensity * POINT_SHADOW_FILTER_DENSITY_SCALE * pow(lightToFragDist, 4);
        samplingDepth = shadowOmniDepth(index, offsetDir, clipDist);
        shadowCoord = vec4(normalize(offsetDir), samplingDepth);

        factor += texture(shadowSamplersOmni[index], shadowCoord);
    }

    factor /= filterSamples + 1;
}

factor = factor + (1f - factor) * (1f - shadow.opacity / attenuation);

return factor;
