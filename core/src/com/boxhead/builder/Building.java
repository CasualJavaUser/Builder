package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Building {
    protected TextureRegion texture;
    protected final Vector2i position;
    protected final BoxCollider collider;

    public Building(TextureRegion texture) {
        this.texture = texture;
        position = new Vector2i();
        collider = new BoxCollider(position, texture.getRegionWidth(), texture.getRegionHeight());
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
        position.set(gridX, gridY);
    }

    public void setPosition(Vector2i gridPosition) {
        //position = gridPosition.clone();
        position.set(gridPosition);
    }
}
