package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tiles {

    public enum Types {
        GRASS(1f, Textures.get(Textures.Tile.GRASS)),
        DIRT(0.8f, Textures.get(Textures.Tile.DIRT)),
        DEFAULT(1f, Textures.get(Textures.Tile.DEFAULT));

        public final float speed;
        public final TextureRegion texture;

        Types(float speed, TextureRegion texture) {
            this.speed = speed;
            this.texture = texture;
        }
    }
}
