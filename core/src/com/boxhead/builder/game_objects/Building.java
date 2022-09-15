package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Inventory;
import com.boxhead.builder.World;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Building extends GameObject implements Clickable {
    protected final String name;
    protected final BoxCollider collider;
    private final Inventory inventory = new Inventory(200);

    public Building(String name, TextureRegion texture) {
        super(texture, new Vector2i());
        this.name = name;
        collider = new BoxCollider(gridPosition, texture.getRegionWidth(), texture.getRegionHeight());
    }

    public String getName() {
        return name;
    }

    public BoxCollider getCollider() {
        return collider;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isClicked() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = BuilderGame.getGameScreen().getMousePosition();
            int colliderX = collider.getGridPosition().x;
            int colliderY = collider.getGridPosition().y;

            return mousePos.x >= colliderX * World.TILE_SIZE && mousePos.x < (colliderX * World.TILE_SIZE + collider.getWidth())
                    && mousePos.y >= colliderY * World.TILE_SIZE && mousePos.y < (colliderY * World.TILE_SIZE + collider.getHeight());
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }
}
