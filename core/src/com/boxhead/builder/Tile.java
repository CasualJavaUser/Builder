package com.boxhead.builder;

import static com.boxhead.builder.Textures.Tile.*;

public enum Tile {

    GRASS(1f, GRASS1, GRASS2, GRASS3),
    DIRT(0.8f, DIRT1, DIRT2, DIRT3),
    DEFAULT(1f, Textures.Tile.DEFAULT),
    WATER(0.3f, WATER1, WATER2),
    FARMLAND(0.8f, Textures.Tile.FARMLAND),
    PATH(1.3f, PATH_CROSS, PATH_T_T, PATH_T_R, PATH_T_B, PATH_T_L, PATH_TR, PATH_BR, PATH_BL, PATH_TL, PATH_LR, PATH_TB);

    public static float minDistanceModifier;
    public final float speed;
    public final Textures.Tile[] textures;

    Tile(float speed, Textures.Tile... textures) {
        this.speed = speed;
        this.textures = textures;
    }

    public static void init() {
        float maxSpeed = 0f;
        for (Tile tile : Tile.values()) {
            if (tile.speed > maxSpeed) maxSpeed = tile.speed;
        }
        minDistanceModifier = 1f / maxSpeed;
    }
}
