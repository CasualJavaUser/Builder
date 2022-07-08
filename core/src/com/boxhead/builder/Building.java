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

    public int getGridX() {
        return position.x;
    }

    public int getGridY() {
        return position.y;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(int gridX, int gridY) {
        if (position == null) position = new Vector2i();
        position.x = gridX;
        position.y = gridY;
    }

    public void setPosition(Vector2i gridPosition) {
        if (position == null) position = new Vector2i();
        position = gridPosition.clone();
    }
}
