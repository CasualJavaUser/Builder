package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class UIElement {
    protected TextureRegion texture;
    protected Vector2i position;
    protected float rotation;
    protected boolean isVisible;

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

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void draw(SpriteBatch batch) {
        //batch.draw(texture, position.x, position.y);
        batch.draw(texture, position.x, position.y, (float)texture.getRegionWidth()/2, (float)texture.getRegionHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, -rotation);
    }
}
