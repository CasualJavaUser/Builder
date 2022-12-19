package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.World;
import com.boxhead.builder.WorldObject;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serial;
import java.io.Serializable;

public abstract class GameObject implements WorldObject, Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    protected final Vector2i gridPosition;
    protected transient TextureRegion texture;

    public GameObject(TextureRegion texture, Vector2i gridPosition) {
        this.gridPosition = gridPosition;
        this.texture = texture;
    }

    public GameObject(TextureRegion texture) {
        this.texture = texture;
        gridPosition = new Vector2i();
    }

    @Override
    public Vector2i getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(Vector2i gridPosition) {
        this.gridPosition.set(gridPosition);
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }

    protected BoxCollider getDefaultCollider() {
        return new BoxCollider(gridPosition, texture.getRegionWidth() / World.TILE_SIZE, texture.getRegionHeight() / World.TILE_SIZE);
    }
}
