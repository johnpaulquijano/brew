vec4 p = vec4(coord, 1.0);

p = animate(animationEnabled, p);
p = wvpMatrix * p;
p.xy = -p.xy;

gl_Position = p;
