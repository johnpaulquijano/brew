vec4 depth = texture(utilSamplers[0], fragment.texCoord);

/**
 * Reconstruct world coordinates from texture coordinates.
 */
float clipx = fragment.texCoord.x * 2.0 - 1.0;
float clipy = fragment.texCoord.y * 2.0 - 1.0;
float clipz = depth.r * 2.0 - 1.0;
vec4 world = vpMatrixInv * vec4(clipx, clipy, clipz, 1f);

world.xyz *= (1.0 / world.w);

vec3 accum = vec3(0.0);
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
    float scattering = (1.0 - squaredFactor) / (4.0 * PI * pow(1.0 + (squaredFactor) - (2.0 * volumetricFactor) * dot(-light.direction, norm), 1.5));

    /**
     * Accumulate volumetric samples.
     */
    for (int j = 0; j < volumetricSteps; j++) {
        vec4 worldSample = volumetricShadowMatrices[i] * vec4(pos, 1.0);
        vec4 shadowSample = texture(utilSamplers[i + 1], worldSample.xy);

        if (shadowSample.r > worldSample.z) {
            accum += scattering * light.color;
        }

        pos += step;
    }
}

return vec4(accum * (1.0 / volumetricSteps), 1.0);
