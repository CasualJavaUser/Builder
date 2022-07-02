package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

public class Building {
    private Texture texture;
    private int x, y;

    public Building(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
