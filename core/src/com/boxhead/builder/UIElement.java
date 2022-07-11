package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class UIElement {
    protected TextureRegion texture;
    protected Vector2i position;
    protected boolean isVisible;

    public UIElement(TextureRegion texture, Vector2i position) {
        this.texture = texture;
        this.position = position;
        isVisible = false;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public Vector2i getPosition() {
        return position;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }
}
