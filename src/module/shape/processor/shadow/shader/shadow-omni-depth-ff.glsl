SHADOW shadow = shadows[shadowList[index]];
vec3 absDir = abs(lightToFragDir);
float side = max(absDir.x, max(absDir.y, absDir.z));
float depth = (shadow.clip + SHADOW_NEAR_CLIP) / clipDist - (2f * shadow.clip * SHADOW_NEAR_CLIP) / clipDist / side;

return (depth + 1.0) * 0.5 - POINT_SHADOW_DEPTH_BIAS;
