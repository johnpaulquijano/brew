vec4 color = mix(vec4(materials[material].diffuse, materials[material].opacity), fragment.color, materials[material].geomColorUsed);
vec3 normal = normalize(fragment.normal);
float shadow = 1f;

if (materials[material].normalMapEnabled) {
    normal = normalize(fragment.tbnMatrix * (texture(normalMap, fragment.texCoord).rgb * 2f - 1f));
}

normal = mix(normal, -normal, bool(materials[material].normalFlipped) && !gl_FrontFacing);