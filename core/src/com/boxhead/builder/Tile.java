package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum Tile {

    GRASS(1f, Textures.Tile.GRASS1, Textures.Tile.GRASS2, Textures.Tile.GRASS3),
    DIRT(0.8f, Textures.Tile.DIRT),
    DEFAULT(1f, Textures.Tile.DEFAULT),
    WATER(0.3f, Textures.Tile.WATER1, Textures.Tile.WATER2),
    FARMLAND(0.8f, Textures.Tile.DIRT);

    public final float speed;
    public final TextureRegion[] textures;

    Tile(float speed, Textures.Tile... textures) {
        this.speed = speed;
        this.textures = new TextureRegion[textures.length];
        for (int i = 0; i < textures.length; i++) {
            this.textures[i] = Textures.get(textures[i]);
        }
    }
}
