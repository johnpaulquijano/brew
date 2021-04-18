vec3 n = normal;
vec4 p = vec4(coord, 1f);

p = animate(animationEnabled, p);
n = animate_normal(animationEnabled, n);

gl_Position = wvpMatrix * vec4(p.xyz + n * contourThickness, p.w) ;
