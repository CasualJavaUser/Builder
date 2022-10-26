package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;
import com.boxhead.builder.utils.Action;

public class Button extends UIElement implements Clickable {

    private Action onClick = () -> {};
    private Action onUp = () -> {};
    private boolean isPrevHeld = false;

    public Button(TextureRegion texture, Vector2i position, Action action, boolean onDown) {
        this(texture, null, position, action, onDown);
    }

    public Button(TextureRegion texture, UIElement parent, Vector2i position, Action action, boolean onDown) {
        super(texture, parent, position);
        if(onDown) onClick = action;
        else onUp = action;
    }

    @Override
    public boolean isClicked() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseOnButton();
    }

    @Override
    public boolean isHeld() {
        isPrevHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT) && isMouseOnButton();
        return isPrevHeld;
    }

    @Override
    public boolean isUp() {
        boolean b = isPrevHeld && !isHeld();
        isPrevHeld = false;
        return b;
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
    public void onUp() {
        onUp.execute();
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

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + texture.getRegionWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + texture.getRegionHeight());
    }
}
