vec4 depth = texture(utilSamplers[0], fragment.texCoord);

/**
 * Reconstruct world coordinates from texture coordinates.
 */
float clipx = fragment.texCoord.x * 2f - 1f;
float clipy = fragment.texCoord.y * 2f - 1f;
float clipz = depth.r * 2f - 1f;
vec4 world = vpMatrixInv * vec4(clipx, clipy, clipz, 1f);

world.xyz *= (1f / world.w);

vec3 accum = vec3(0f);
vec3 pos = cameraLoc;
vec3 dir = world.xyz - cameraLoc;
vec3 norm = normalize(dir);
vec3 step = norm * (length(dir) / volumetricSteps);

for (int i = 0; i < numVolumetricSources; i++) {
    SHADOW shadow = shadows[volumetricShadowSources[i]];
    LIGHT light = lights[volumetricLightSources[i]];

    /**
     * Henyey-Greenstein phase function calculation.
     */
    float squaredFactor = volumetricFactor * volumetricFactor;
    float scattering = (1f - squaredFactor) / (4f * PI * pow(1f + (squaredFactor) - (2f * volumetricFactor) * dot(-light.direction, norm), 1.5f));

    /**
     * Accumulate volumetric samples.
     */
    for (int j = 0; j < volumetricSteps; j++) {
        vec4 worldSample = volumetricShadowMatrices[i] * vec4(pos, 1f);
        vec4 shadowSample = texture(utilSamplers[i + 1], worldSample.xy);

        if (shadowSample.r > worldSample.z) {
            accum += scattering * light.color;
        }

        pos += step;
    }
}

return vec4(accum * (1f / volumetricSteps), 1f);
