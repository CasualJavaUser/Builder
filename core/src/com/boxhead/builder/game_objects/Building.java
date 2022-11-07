package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Inventory;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Building extends GameObject implements Clickable {
    protected final String name;
    protected BoxCollider collider;
    protected UIElement indicator;
    private final Inventory inventory = new Inventory(200);

    public Building(String name, TextureRegion texture, Vector2i gridPosition, BoxCollider collider) {
        super(texture, gridPosition);
        this.name = name;
        this.collider = collider;
        indicator = new UIElement(null, new Vector2i(texture.getRegionWidth() / 2 - 8, texture.getRegionHeight() + 10));
    }

    public Building(String name, Buildings.Type type, Vector2i gridPosition) {
        this(name, type.getTexture(), gridPosition,
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
    public boolean isClicked() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = BuilderGame.getGameScreen().getMousePosition();
            int colliderX = collider.getGridPosition().x;
            int colliderY = collider.getGridPosition().y;

            return mousePos.x >= colliderX * World.TILE_SIZE && mousePos.x < (colliderX * World.TILE_SIZE + collider.getWidth() * World.TILE_SIZE)
                    && mousePos.y >= colliderY * World.TILE_SIZE && mousePos.y < (colliderY * World.TILE_SIZE + collider.getHeight() * World.TILE_SIZE);
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if(inventory.isFull()) {
            indicator.setTexture(Textures.get(Textures.Ui.FULL_STORAGE));
            indicator.setVisible(true);
        } else {
            indicator.setVisible(false);
        }
        if (indicator.isVisible()) {
            batch.draw(indicator.getTexture(), gridPosition.x * World.TILE_SIZE + indicator.getLocalPosition().x,
                    gridPosition.y * World.TILE_SIZE + indicator.getLocalPosition().y);
        }
    }
}
