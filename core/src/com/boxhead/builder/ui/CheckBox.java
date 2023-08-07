package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Consumer;

public class CheckBox extends UIElement implements Clickable {
    private boolean value;
    private Consumer<Boolean> onUp = (b) -> {};

    public CheckBox(UIElement parent, UI.Layer layer, Vector2i position) {
        this (parent, layer, position, false);
    }

    public CheckBox(UIElement parent, UI.Layer layer, Vector2i position, boolean value) {
        super(Textures.get(Textures.Ui.CHECK_BOX), parent, layer, position);
        this.value = value;
    }

    /**
     * Only use when loading a save.
     */
    public void setValue(boolean value) {
        this.value = value;
    }

    public void setOnUp(Consumer<Boolean> onUp) {
        this.onUp = onUp;
    }

    @Override
    public void onUp() {
        value = !value;
        onUp.accept(value);
    }

    @Override
    public boolean isMouseOver() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + texture.getRegionWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + texture.getRegionHeight());
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (value)
            batch.draw(Textures.get(Textures.Ui.CHECK), getGlobalPosition().x, getGlobalPosition().y);
    }
}
