package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Building extends GameObject implements Clickable {
    protected final String name;
    protected final BoxCollider collider;

    public Building(String name, TextureRegion texture) {
        super(texture, new Vector2i());
        this.name = name;
        collider = new BoxCollider(position, texture.getRegionWidth(), texture.getRegionHeight());
    }

    public String getName() {
        return name;
    }

    public BoxCollider getCollider() {
        return collider;
    }

    @Override
    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = BuilderGame.getGameScreen().getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            return mousePos.x >= collider.getGridPosition().x * World.TILE_SIZE && mousePos.x < (collider.getGridPosition().x * World.TILE_SIZE + collider.getWidth()) &&
                    mousePos.y >= collider.getGridPosition().y * World.TILE_SIZE && mousePos.y < (collider.getGridPosition().y * World.TILE_SIZE + collider.getHeight());
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x * World.TILE_SIZE, position.y * World.TILE_SIZE);
    }
}
