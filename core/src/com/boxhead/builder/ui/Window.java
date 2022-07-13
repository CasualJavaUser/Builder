package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Vector2i;

public class Window extends UIElement implements Clickable {

    private boolean isDragged = false;

    public Window(TextureRegion texture) {
        super(texture, new Vector2i());
    }

    public Window(TextureRegion texture, Vector2i position) {
        super(texture, position);
    }

    public Window(TextureRegion texture, Vector2i position, boolean visible) {
        super(texture, position, visible);
    }

    @Override
    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y + texture.getRegionHeight());
        }
        return false;
    }

    @Override
    public boolean isDown() {
        if(isClicked()) isDragged = true;
        if(isDragged) isDragged = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        return isDragged;
    }

    @Override
    public void onClick() {}

    @Override
    public void onDown() {
        position.x += Gdx.input.getDeltaX();
        position.y -= Gdx.input.getDeltaY();
    }
}
