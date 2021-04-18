vec3 diffuseMaterial = inputColor.rgb;
vec3 specularMaterial = materials[material].specular;
vec3 camToFragDir = normalize(fragment.wCoord.xyz - cameraLoc);

vec3 specularTerm = vec3(0f);
vec3 diffuseTerm = vec3(0f);
vec3 ambientTerm = vec3(0f);

if (materials[material].reflectionEnabled) {
    specularMaterial = vec3(0f);
}

float fresnel = 1f - dot(-camToFragDir, inputNormal);

for (int i = 0; i < numLights; i++) {
    LIGHT light = lights[lightList[i]];

    if (light.enabled) {
        switch (int(light.type)) {
            case LIGHT_AMBIENT: {
                ambientTerm += materials[material].ambient * light.color;
                break;
            }
            case LIGHT_DISTANT: {
                vec3 lightDir = normalize(light.direction);
                float nDotL = max(dot(inputNormal, -lightDir), 0f);

                if (nDotL > 0f) {
                    float shininess = materials[material].shininess;
                    vec3 halfVec = normalize(normalize(cameraLoc - fragment.wCoord.xyz) - lightDir);
                    float nDotHv = max(dot(inputNormal, halfVec), 0f);
                    float specFactor = pow(nDotHv, shininess * 128f);
                    float invLevels = 1f / materials[material].shadingLevel;
                    float diffShadingLevel = nDotL;
                    float specShadingLevel = specFactor;

                    if (materials[material].shadingLevel > 0f) {
                        diffShadingLevel = floor(nDotL * materials[material].shadingLevel) * invLevels;
                        specShadingLevel = floor(specFactor * materials[material].shadingLevel) * invLevels;
                    }

                    diffuseTerm += diffuseMaterial * light.color * diffShadingLevel;
                    specularTerm += specularMaterial * shininess * specShadingLevel * fresnel;
                }

                break;
            }
            case LIGHT_SPOT: {
                vec3 lightToFragDir = fragment.wCoord.xyz - light.location;
                vec3 lightToFragNorm = normalize(lightToFragDir);
                float nDotL = max(dot(inputNormal, -lightToFragNorm), 0f);

                if (nDotL > 0) {
                    vec3 halfVec = normalize(normalize(cameraLoc - fragment.wCoord.xyz) - lightToFragNorm);
                    float nDotHv = max(dot(inputNormal, halfVec), 0f);
                    float spotEffect = dot(normalize(light.direction), lightToFragNorm);
                    float cosCutoff = cos(clamp(light.cutoff, 0f, HALF_PI));

                    if (spotEffect > cosCutoff) {
                        float shininess = materials[material].shininess;
                        float lightToFragDist = length(lightToFragDir);
                        float attenuation = pow(spotEffect, light.exponent) / (light.attenuation.x + light.attenuation.y * lightToFragDist + light.attenuation.z * lightToFragDist * lightToFragDist);
                        float cosInnerCutoff = cos(clamp(light.cutoff - lightToFragDist / attenuation, 0f, HALF_PI));
                        float penumbra = smoothstep(cosCutoff, cosInnerCutoff, spotEffect);
                        float specFactor = pow(nDotHv, shininess * 128f);
                        float invLevels = 1f / materials[material].shadingLevel;
                        float diffShadingLevel = nDotL;
                        float specShadingLevel = specFactor;

                        if (materials[material].shadingLevel > 0f) {
                            diffShadingLevel = floor(nDotL * materials[material].shadingLevel) * invLevels;
                            specShadingLevel = floor(specFactor * materials[material].shadingLevel) * invLevels;
                        }

                        diffuseTerm += diffuseMaterial * light.color * attenuation * penumbra * diffShadingLevel;
                        specularTerm += specularMaterial * shininess * attenuation * penumbra * specShadingLevel * fresnel;
                    }
                }

                break;
            }
            case LIGHT_POINT: {
                vec3 lightToFragDir = fragment.wCoord.xyz - light.location;
                vec3 lightToFragNorm = normalize(lightToFragDir);
                float nDotL = max(dot(inputNormal, -lightToFragNorm), 0f);

                if (nDotL > 0f) {
                    float shininess = materials[material].shininess;
                    vec3 halfVec = normalize(normalize(cameraLoc - fragment.wCoord.xyz) - lightToFragNorm);
                    float nDotHv = max(dot(inputNormal, halfVec), 0f);
                    float lightToFragDist = length(lightToFragDir);
                    float attenuation = 1f / (light.attenuation.x + light.attenuation.y * lightToFragDist + light.attenuation.z * lightToFragDist * lightToFragDist);
                    float specFactor = pow(nDotHv, shininess * 128f);
                    float invLevels = 1f / materials[material].shadingLevel;
                    float diffShadingLevel = nDotL;
                    float specShadingLevel = specFactor;

                    if (materials[material].shadingLevel > 0f) {
                        diffShadingLevel = floor(nDotL * materials[material].shadingLevel) * invLevels;
                        specShadingLevel = floor(specFactor * materials[material].shadingLevel) * invLevels;
                    }

                    diffuseTerm += diffuseMaterial * light.color * attenuation * diffShadingLevel;
                    specularTerm += specularMaterial * shininess * attenuation * specShadingLevel * fresnel;
                }

                break;
            }
        }
    }
}

if (materials[material].specularMapEnabled) {
    specularTerm *= texture(specularMap, fragment.texCoord).r;
}

return vec4(ambientTerm + diffuseTerm + specularTerm + materials[material].emissive, inputColor.a);
