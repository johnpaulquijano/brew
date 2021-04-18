vec4 base = texture(utilSamplers[0], fragment.texCoord);
vec4 blur = texture(utilSamplers[1], fragment.texCoord);

return base + blur * level;
