vec4 color = texture(utilSamplers[0], fragment.texCoord);
vec4 depth = texture(utilSamplers[1], fragment.texCoord);
float linearDepth = linearize(depth.r, cameraClip.x, cameraClip.y);
float linearWorldDepth = linearDepth * (cameraClip.y - cameraClip.x);
float coc = clamp((linearWorldDepth - dofFocalDistance) / dofFieldRange, -1, 1);

return vec4(color.rgb, abs(coc));
