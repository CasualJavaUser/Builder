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

public class Building implements Clickable {
    protected final String name;
    protected TextureRegion texture;
    protected final Vector2i position;
    protected final BoxCollider collider;

    public Building(String name, TextureRegion texture) {
        this.name = name;
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
        position.set(gridPosition);
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
            Vector3 mousePos = GameScreen.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
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
