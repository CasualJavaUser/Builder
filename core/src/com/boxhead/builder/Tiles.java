package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tiles {

    public enum Types {
        GRASS(1f, Textures.getTile("grass")),
        DIRT(0.8f, Textures.getTile("dirt")),
        DEFAULT(1f, Textures.getTile("default"));

        public float speed;
        public TextureRegion texture;

        Types(float speed, TextureRegion texture) {
            this.speed = speed;
            this.texture = texture;
        }
    }
}
