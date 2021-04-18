vec4 base = texture(utilSamplers[0], fragment.texCoord);
float depth = texture(utilSamplers[1], fragment.texCoord).r;
float worldDepth = linearize(depth, cameraClip.x, cameraClip.y) * (cameraClip.y - cameraClip.x);
float factor = exp(-density * density * worldDepth * worldDepth);

return vec4(mix(color, base.rgb, factor), base.a);
