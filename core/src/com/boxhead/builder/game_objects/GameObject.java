package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.WorldObject;
import com.boxhead.builder.utils.Vector2i;

public abstract class GameObject implements WorldObject {
    protected final Vector2i gridPosition;
    protected TextureRegion texture;

    public GameObject(TextureRegion texture, Vector2i gridPosition) {
        this.gridPosition = gridPosition;
        this.texture = texture;
    }

    @Override
    public Vector2i getGridPosition() {
        return gridPosition;
    }

    public void setPosition(int x, int y) {
        this.gridPosition.set(x, y);
    }

    public void setGridPosition(Vector2i gridPosition) {
        this.gridPosition.set(gridPosition);
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }
}
