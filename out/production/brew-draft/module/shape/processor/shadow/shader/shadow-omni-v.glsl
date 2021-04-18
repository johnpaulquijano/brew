vec4 p = vec4(coord, 1f);

p = animate(animationEnabled, p);
p = wvpMatrix * p;
p.xy = -p.xy;

gl_Position = p;
