package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Building extends GameObject implements Clickable {
    protected final String name;
    protected BoxCollider collider;
    protected final Inventory inventory = new Inventory(200);

    public Building(String name, TextureRegion texture, Vector2i gridPosition, BoxCollider collider) {
        super(texture, gridPosition);
        this.name = name;
        this.collider = collider;
    }

    public Building(Buildings.Type type, Vector2i gridPosition) {
        this(type.name, type.getTexture(), gridPosition,
                type.getRelativeCollider().cloneAndTranslate(gridPosition));
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
    public boolean isMouseOver() {
        int colliderX = collider.getGridPosition().x;
        int colliderY = collider.getGridPosition().y;
        Vector3 mousePos = GameScreen.getMouseWorldPosition();

        return mousePos.x >= colliderX * World.TILE_SIZE && mousePos.x < (colliderX * World.TILE_SIZE + collider.getWidth() * World.TILE_SIZE)
                && mousePos.y >= colliderY * World.TILE_SIZE && mousePos.y < (colliderY * World.TILE_SIZE + collider.getHeight() * World.TILE_SIZE);
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }
}
