vec4 outputColor = inputColor;

for (int i = 0; i < numTextures; i++) {
    vec4 sourceColor = texture(utilSamplers[i], fragment.texCoord);

    switch (blendModes[i]) {
        case TEXTURE_BLEND_REPLACE:
            outputColor = sourceColor;
            break;
        case TEXTURE_BLEND_MODULATE:
            outputColor.rgb *= sourceColor.rgb;
            break;
        case TEXTURE_BLEND_ACCUMULATE:
            outputColor.rgb += sourceColor.rgb;
            break;
        case TEXTURE_BLEND_INTERPOLATE:
            outputColor.rgb = mix(outputColor.rgb, sourceColor.rgb, sourceColor.a);
            break;
    }
}

if (outputColor.a <= 0f) {
    discard;
}

return outputColor;
