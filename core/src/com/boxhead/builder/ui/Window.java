package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Window extends UIElement implements Clickable {

    protected boolean isDragged = false;

    private final Vector2i mouseOnClick = new Vector2i();

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
    public boolean isHeld() {
        if(isClicked()) isDragged = true;
        if(isDragged) isDragged = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        return isDragged;
    }

    @Override
    public void onClick() {
        mouseOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onHold() {
        position.x += Gdx.input.getX() - mouseOnClick.x;
        position.y -= Gdx.input.getY() - mouseOnClick.y;
        mouseOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }
}
