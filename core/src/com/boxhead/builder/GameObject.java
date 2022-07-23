package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class GameObject {
    protected final Vector2i position;
    protected TextureRegion texture;

    public GameObject(TextureRegion texture, Vector2i position) {
        this.position = position;
        this.texture = texture;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        this.position.set(x, y);
    }

    public void setPosition(Vector2i position) {
        this.position.set(position);
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }
}
