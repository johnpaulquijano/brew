vec4 p = vec4(coord, 1f);
p = animate(animationEnabled, p);
gl_Position = wvpMatrix * p;
