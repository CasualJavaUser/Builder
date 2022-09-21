package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    protected final Inventory inventory = new Inventory(200);
    protected BoxCollider collider;

    public Building(String name, TextureRegion texture, Vector2i gridPosition) {
        super(texture, gridPosition);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BoxCollider getCollider() {
        if (collider == null)
            collider = getDefaultCollider();
        return collider;
    }

    public void setCollider(BoxCollider collider) {
        this.collider = collider;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isClicked() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = BuilderGame.getGameScreen().getMousePosition();
            int colliderX = getCollider().getGridPosition().x;
            int colliderY = getCollider().getGridPosition().y;

            return mousePos.x >= colliderX * World.TILE_SIZE && mousePos.x < (colliderX * World.TILE_SIZE + collider.getWidth() * World.TILE_SIZE)
                    && mousePos.y >= colliderY * World.TILE_SIZE && mousePos.y < (colliderY * World.TILE_SIZE + collider.getHeight() * World.TILE_SIZE);
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }
}
