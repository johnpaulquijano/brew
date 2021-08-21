vec4 sourceColor = texture(sampler, texCoord);
float luminance = luminance(sourceColor.rgb);
return vec4(sourceColor.rgb * sign(max(luminance - threshold, 0)), sourceColor.a);
