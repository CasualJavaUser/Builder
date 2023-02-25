package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.GameScreen;
import com.boxhead.builder.World;
import com.boxhead.builder.WorldObject;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

public class GameObject implements WorldObject, Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    public static final Comparator<GameObject> gridPositionComparator = Comparator.comparingLong(go -> go.getGridPosition().gridHash());

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
        int x = gridPosition.x * World.TILE_SIZE;
        int y = gridPosition.y * World.TILE_SIZE;
        Vector2 pos = GameScreen.worldToScreenPosition(x, y);
        if (pos.x + texture.getRegionWidth() / GameScreen.camera.zoom > 0 && pos.x < Gdx.graphics.getWidth() &&
                pos.y + texture.getRegionHeight() / GameScreen.camera.zoom > 0 && pos.y < Gdx.graphics.getHeight())
            batch.draw(texture, x, y);
    }

    protected BoxCollider getDefaultCollider() {
        return new BoxCollider(gridPosition, texture.getRegionWidth() / World.TILE_SIZE, texture.getRegionHeight() / World.TILE_SIZE);
    }
}
