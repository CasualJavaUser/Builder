package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Building {
    protected TextureRegion texture;
    protected Vector2i position = null;

    public Building(TextureRegion texture) {
        this.texture = texture;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        if(position == null) position = new Vector2i();
        position.x = x;
        position.y = y;
    }
}
