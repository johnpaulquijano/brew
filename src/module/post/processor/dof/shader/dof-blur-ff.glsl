vec4 base = texture(utilSamplers[0], fragment.texCoord);
vec4 coc = texture(utilSamplers[1], fragment.texCoord);
vec4 blend = mix(base, coc, coc.a);
float cocFactor = coc.a;

if (dofDiskBlurEnabled) {
    blend.rgb *= blend.a;

    for (int i = 0; i < int(dofDiskBlurSamples * cocFactor); i++) {
        vec2 tapCoord = fragment.texCoord + poissonDisk[i] * fragmentSize * cocFactor * dofDiskBlurRadius;
        vec4 tapBase = texture(utilSamplers[0], tapCoord);
        vec4 tapBlur = texture(utilSamplers[1], tapCoord);
        float tapFactor = tapBlur.a;

        vec4 tap = mix(tapBase, tapBlur, tapFactor);
        tap.a = mix(tap.a, 1f, tap.a >= cocFactor);

        blend.rgb += tap.rgb * tap.a;
        blend.a += tap.a;
    }

    blend /= blend.a;
}

return blend;
