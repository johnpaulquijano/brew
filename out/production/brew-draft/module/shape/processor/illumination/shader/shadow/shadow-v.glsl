if (shadowEnabled) {
    for (int i = 0; i < numShadows; i++) {
        SHADOW shadow = shadows[shadowList[i]];
        LIGHT source = lights[shadowSourceList[i]];

        if (source.type != LIGHT_POINT) {
            vertex.sCoord[i] = biasedShadowMatrix[i] * p;
        }
    }
}
