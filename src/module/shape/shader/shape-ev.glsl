vec3 n = normal;
vec3 t = tangent;
vec4 c = color;
vec2 tc = texCoord;
vec4 p = vec4(coord, 1f);

p = animate(animationEnabled, p);

vertex.color = c;
vertex.coord = p;
vertex.normal = n;
vertex.texCoord = tc;
