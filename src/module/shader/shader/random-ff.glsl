float a = 12.9898;
float b = 78.233;
float c = 43758.5453;
float dt= dot(seed.xy, vec2(a, b));
float sn= mod(dt, 3.14);

return fract(sin(sn) * c);