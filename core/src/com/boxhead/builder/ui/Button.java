package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Button extends UIElement implements Clickable {

    private final OnClick onClick;

    public Button(TextureRegion texture, Vector2i position, OnClick onClick) {
        super(texture, position);
        this.onClick = onClick;
    }

    @Override
    public boolean isClicked() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseOnButton();
    }

    @Override
    public boolean isHeld() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT) && isMouseOnButton();
    }

    @Override
    public void onClick() {
        onClick.execute();
    }

    @Override
    public void onHold() {
        tint = UI.PRESSED_COLOR;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(tint);
        super.draw(batch);
        tint = UI.DEFAULT_COLOR;
        batch.setColor(tint);
    }

    private boolean isMouseOnButton() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                y >= position.y && y < (position.y + texture.getRegionHeight());
    }

    @FunctionalInterface
    public interface OnClick {
        void execute();
    }
}
