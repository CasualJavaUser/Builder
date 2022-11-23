package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class UIElement {
    protected TextureRegion texture;
    protected UIElement parent;
    protected final Vector2i position;
    protected float rotation;
    protected boolean isVisible;
    protected Color tint, currentTint;

    public UIElement(TextureRegion texture, Vector2i position) {
        this(texture, null, position, 0, false);
    }

    public UIElement(TextureRegion texture, Vector2i position, boolean visible) {
        this(texture, null, position, 0, visible);
    }

    public UIElement(TextureRegion texture, UIElement parent, Vector2i position) {
        this(texture, parent, position, 0, true);
    }

    public UIElement(TextureRegion texture, UIElement parent, Vector2i position, boolean visible) {
        this(texture, parent, position, 0, visible);
    }

    public UIElement(TextureRegion texture, UIElement parent, Vector2i position, float rotation, boolean visible) {
        this.texture = texture;
        this.parent = parent;
        this.position = position;
        this.rotation = rotation;
        isVisible = visible;
        tint = UI.DEFAULT_COLOR;
        currentTint = UI.DEFAULT_COLOR;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public Vector2i getLocalPosition() {
        return position;
    }

    public Vector2i getGlobalPosition() {
        if (parent != null)
            return new Vector2i(parent.getGlobalPosition().x + position.x,
                    parent.getGlobalPosition().y + position.y);
        else
            return position;
    }

    public void setLocalPosition(int x, int y) {
        position.set(x, y);
    }

    public void setGlobalPosition(int x, int y) {
        if (parent != null)
            position.set(x - parent.getGlobalPosition().x, y - parent.getGlobalPosition().y);
        else
            position.set(x, y);
    }

    public boolean isVisible() {
        if(parent == null) return isVisible;
        else return isVisible && parent.isVisible();
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setLocalRotation(float rotation) {
        this.rotation = rotation;
    }

    public void setGlobalRotation(float rotation) {
        if (parent != null) this.rotation = rotation - parent.getGlobalRotation();
        else this.rotation = rotation;
    }

    public float getLocalRotation() {
        return rotation;
    }

    public float getGlobalRotation() {
        if (parent != null) return parent.getGlobalRotation() + rotation;
        else return rotation;
    }

    public void setTint(Color tint) {
        this.tint = tint;
        currentTint = tint;
    }

    public UIElement getParent() {
        return parent;
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.setColor(currentTint);
            batch.draw(
                    texture,
                    getGlobalPosition().x,
                    getGlobalPosition().y,
                    (float) texture.getRegionWidth() / 2,
                    (float) texture.getRegionHeight() / 2,
                    texture.getRegionWidth(), texture.getRegionHeight(),
                    1,
                    1,
                    -getGlobalRotation());
            batch.setColor(UI.DEFAULT_COLOR);
        }
    }

    protected boolean isMouseOnElement() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + texture.getRegionWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + texture.getRegionHeight());
    }
}
