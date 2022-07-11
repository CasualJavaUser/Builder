package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Button extends UIElement {

    public Button(TextureRegion texture, Vector2i position) {
        super(texture, position);
    }

    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y) + texture.getRegionHeight();
        }
        return false;
    }

    public boolean isDown() {
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y) + texture.getRegionHeight();
        }
        return false;
    }

    abstract void onClick();

    @Override
    public void draw(SpriteBatch batch) {
        if(!isDown()) super.draw(batch);
        else {
            batch.setColor(UI.PRESSED_COLOR);
            super.draw(batch);
            batch.setColor(UI.DEFAULT_COLOR);
        }
    }
}
