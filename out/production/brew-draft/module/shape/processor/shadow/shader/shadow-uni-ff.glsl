float factor = 1f;
SHADOW shadow = shadows[shadowList[index]];
LIGHT source = lights[shadowSourceList[index]];
float filterDensity = shadow.filterDensity;
float clipDist = shadow.clip - SHADOW_NEAR_CLIP;

if (source.type == LIGHT_DISTANT) {
    filterDensity *= DISTANT_SHADOW_FILTER_DENSITY_SCALE;
} else if (source.type == LIGHT_SPOT) {
    filterDensity *= SPOT_SHADOW_FILTER_DENSITY_SCALE * pow(lightAttenuation, 4);
}

factor = textureProj(shadowSamplersUni[index], fragment.sCoord[index]);

if (shadow.filtered) {
    for (int i = 0; i < shadow.filterSamples; i++) {
        vec4 coord = fragment.sCoord[index];
        coord.xy += shadow.fragmentSize * filterDensity * poissonDisk[i];
        factor += textureProj(shadowSamplersUni[index], coord);
    }

    factor /= shadow.filterSamples + 1;
}

factor = factor + (1f - factor) * (1f - shadow.opacity / lightAttenuation);

return factor;
