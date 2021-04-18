vec4 color = texture(utilSamplers[0], fragment.texCoord);
vec4 accum = texture(utilSamplers[1], fragment.texCoord);

return color + accum * volumetricLevel;