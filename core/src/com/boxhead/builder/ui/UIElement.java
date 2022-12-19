package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class UIElement {
    protected TextureRegion texture;
    protected UIElement parent;
    private final Vector2i position;
    protected float rotation;
    protected boolean isVisible;
    protected Color tint;
    protected UI.Layer layer;
    private Rectangle scissors = null;

    public UIElement() {
        this(null, null, null, new Vector2i(), 0, true);
    }

    public UIElement(UIElement parent, UI.Layer layer, Vector2i position, boolean visible) {
        this(null, parent, layer, position, 0, visible);
    }

    public UIElement(TextureRegion texture, UI.Layer layer, Vector2i position, boolean visible) {
        this(texture, null, layer, position, 0, visible);
    }

    public UIElement(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        this(texture, parent, layer, position, 0, true);
    }

    public UIElement(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, boolean visible) {
        this(texture, parent, layer, position, 0, visible);
    }

    public UIElement(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, float rotation, boolean visible) {
        this.texture = texture;
        this.parent = parent;
        this.layer = layer;
        this.position = position;
        this.rotation = rotation;
        isVisible = visible;
        tint = UI.DEFAULT_COLOR;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public int getWidth() {
        if (texture != null) return texture.getRegionWidth();
        return 0;
    }

    public int getHeight() {
        if (texture != null) return texture.getRegionHeight();
        return 0;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public void setParent(UIElement parent) {
        this.parent = parent;
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

    public void Move(int x, int y) {
        position.set(position.x + x, position.y + y);
    }

    public boolean isVisible() {
        if(parent == null) return isVisible;
        else return isVisible && parent.isVisible() && layer.isVisible();
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
    }

    public UIElement getParent() {
        return parent;
    }

    public UI.Layer getLayer() {
        return layer;
    }

    public void addToUI() {
        if (!layer.getElements().contains(this)) layer.addElement(this);
    }

    public void setScissors(int x, int y, int width, int height) {
        scissors = new Rectangle(x, y, width, height);
    }

    public void setScissors(Rectangle rectangle) {
        scissors = rectangle;
    }

    public void removeScissors() {
        scissors = null;
    }

    protected void enableScissors(SpriteBatch batch) {
        if(scissors != null) {
            batch.flush();
            Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
            Gdx.gl.glScissor(
                    (int)scissors.getX(),
                    (int)scissors.getY(),
                    (int)scissors.getWidth(),
                    (int)scissors.getHeight());
        }
    }

    protected void disableScissors(SpriteBatch batch) {
        if(scissors != null) {
            batch.flush();
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }
    }

    public Rectangle getScissors() {
        return scissors;
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.setColor(tint);
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
}
