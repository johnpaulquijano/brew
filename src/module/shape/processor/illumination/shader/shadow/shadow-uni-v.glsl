vec4 p = vec4(coord, 1.0);
p = animate(animationEnabled, p);
gl_Position = wvpMatrix * p;
