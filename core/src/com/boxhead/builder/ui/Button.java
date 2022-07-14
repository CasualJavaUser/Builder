package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public abstract class Button extends UIElement implements Clickable {

    public Button(TextureRegion texture, Vector2i position) {
        super(texture, position);
    }

    @Override
    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y) + texture.getRegionHeight();
        }
        return false;
    }

    @Override
    public boolean isHeld() {
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y + texture.getRegionHeight());
        }
        return false;
    }

    @Override
    public abstract void onClick();

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
}
