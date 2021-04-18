if (lightEnabled) {
    vertex.normal = normalize(nMatrix * normal);
    vertex.wCoord = wMatrix * p;

    if (materials[material].normalMapEnabled) {
        vec3 tan = normalize(nMatrix * (tangent - dot(tangent, normal) * normal));
        vec3 bitan = normalize(nMatrix * cross(normal, tangent));
        vertex.tbnMatrix = mat3(tan, bitan, vertex.normal);

        t = animate_normal(animationEnabled, t);
    }

    n = animate_normal(animationEnabled, n);
}
