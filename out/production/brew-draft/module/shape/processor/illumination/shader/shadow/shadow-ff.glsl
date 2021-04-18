float factor = 1f;
bool drawn = false;

for (int i = 0; i < numShadows; i++) {
    SHADOW shadow = shadows[shadowList[i]];
    LIGHT source = lights[shadowSourceList[i]];
    vec3 normal = normalize(fragment.normal);

    switch (int(source.type)) {
        case LIGHT_DISTANT: {
            if (shadow.cascadeIndex > -1) {
                float depth = linearize(gl_FragCoord.z, cameraClip.x, cameraClip.y);

                if (drawn || depth > cascadeSplits[int(shadow.cascadeIndex)]) {
                    break;
                }

                drawn = true;
            }

            vec3 lightDir = normalize(source.direction);
            float nDotL = max(dot(normal, -lightDir), 0f);

            if (nDotL > 0f) {
                factor *= mix(1f, shadowUni(i, 0f, 1f), nDotL);
            }

            break;
        }
        case LIGHT_SPOT: {
            vec3 lightToFragDir = fragment.wCoord.xyz - source.location;
            vec3 lightToFragNorm = normalize(lightToFragDir);
            float nDotL = max(dot(normal, -lightToFragNorm), 0f);

            if (nDotL > 0f) {
                float spotEffect = dot(normalize(source.direction), lightToFragNorm);
                float cosCutoff = cos(clamp(source.cutoff, 0f, HALF_PI));
                float lightToFragDist = length(lightToFragDir);
                float attenuation = source.attenuation.x + source.attenuation.y * lightToFragDist + source.attenuation.z * lightToFragDist * lightToFragDist;
                float baseFactor = 1f - shadow.opacity / attenuation;

                if (spotEffect > cosCutoff) {
                    float attenuationInv = pow(spotEffect, source.exponent) / attenuation;
                    float cosInnerCutoff = cos(clamp(source.cutoff - lightToFragDist / attenuationInv * SPOT_SHADOW_PENUMBRA_SCALE, 0f, HALF_PI));
                    float penumbra = smoothstep(cosCutoff, cosInnerCutoff, spotEffect);
                    float shadowFactor = mix(1f, shadowUni(i, lightToFragDist, attenuation), nDotL);

                    factor *= mix(baseFactor, shadowFactor, penumbra);
                } else {
                    factor *= baseFactor;
                }
            }

            break;
        }
        case LIGHT_POINT: {
            vec3 lightToFragDir = fragment.wCoord.xyz - source.location;
            vec3 lightToFragNorm = normalize(lightToFragDir);
            float nDotL = max(dot(normal, -lightToFragNorm), 0f);

            factor *= mix(1f, shadowOmni(i, lightToFragNorm, lightToFragDir), nDotL);

            break;
        }
    }
}

return factor;
