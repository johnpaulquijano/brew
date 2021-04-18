vec3 camToFragDir = normalize(fragment.wCoord.xyz - cameraLoc);
vec3 reflectionVec = reflect(camToFragDir, inputNormal);
vec3 reflection = texture(environmentSampler, reflectionVec).rgb;
float fresnel = 1f - dot(-camToFragDir, inputNormal);

return vec4(mix(inputColor.rgb, reflection, fresnel), inputColor.a);