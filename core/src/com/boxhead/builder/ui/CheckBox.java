package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;

import java.util.function.Consumer;

public class CheckBox extends DrawableComponent {
    private boolean value;
    private Consumer<Boolean> onUp = (b) -> {};

    public CheckBox() {
        this (false);
    }

    public CheckBox(boolean value) {
        super(Textures.Ui.CHECK_BOX);
        this.value = value;
    }

    public CheckBox(boolean value, Consumer<Boolean> onUp) {
        super(Textures.Ui.CHECK_BOX);
        this.value = value;
        this.onUp = onUp;
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
    public UIComponent onClick() {
        return this;
    }

    @Override
    public void onUp() {
        value = !value;
        onUp.accept(value);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (value) {
            batch.setColor(getTint());
            batch.draw(Textures.get(Textures.Ui.CHECK), getX(), getY());
        }
    }
}
