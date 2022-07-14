package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class UIElement {
    protected TextureRegion texture;
    protected Vector2i position;
    protected float rotation;
    protected boolean isVisible;
    protected Color tint;

    public UIElement(TextureRegion texture, Vector2i position) {
        this(texture, position, 0, false);
    }

    public UIElement(TextureRegion texture, Vector2i position, boolean visible) {
        this(texture, position, 0, visible);
    }

    public UIElement(TextureRegion texture, Vector2i position, float rotation, boolean visible) {
        this.texture = texture;
        this.position = position;
        this.rotation = rotation;
        isVisible = visible;
        tint = UI.DEFAULT_COLOR;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Color getTint() {
        return tint;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, (float)texture.getRegionWidth()/2, (float)texture.getRegionHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, -rotation);
    }
}
