vertex.coord = vec4(coord, 1f);
gl_Position = wvpMatrix * vertex.coord;