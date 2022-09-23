package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tile {

    public enum Type {
        GRASS(1f, Textures.get(Textures.Tile.GRASS1), Textures.get(Textures.Tile.GRASS2), Textures.get(Textures.Tile.GRASS3)),
        DIRT(0.8f, Textures.get(Textures.Tile.DIRT)),
        DEFAULT(1f, Textures.get(Textures.Tile.DEFAULT)),
        WATER(0.3f, Textures.get(Textures.Tile.WATER));

        public final float speed;
        public final TextureRegion[] textures;

        Type(float speed, TextureRegion... textures) {
            this.speed = speed;
            this.textures = textures;
        }
    }
}
