float a = 12.9898f;
float b = 78.233f;
float c = 43758.5453f;
float dt= dot(seed.xy, vec2(a, b));
float sn= mod(dt, 3.14f);

return fract(sin(sn) * c);