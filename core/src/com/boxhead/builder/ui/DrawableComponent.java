package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;

public class DrawableComponent extends UIComponent {
    public TextureRegion texture = null;
    private Color tint = UI.DEFAULT_UI_COLOR;
    private float rotation;
    private float scale;
    private float originX = 0, originY = 0;

    public DrawableComponent(TextureRegion texture, float rotation, float scale) {
        if (texture != null)
            this.texture = texture;
        this.rotation = rotation;
        this.scale = scale;
    }

    public DrawableComponent(Textures.Ui texture) {
        this(texture == null ? null : Textures.get(texture), 0, 1);
    }

    public DrawableComponent(TextureRegion texture) {
        this(texture, 0, 1);
    }

    @Override
    public int getWidth() {
        if (texture != null) return (int)(texture.getRegionWidth() * scale);
        return 0;
    }

    @Override
    public int getHeight() {
        if (texture != null) return (int)(texture.getRegionHeight() * scale);
        return 0;
    }

    public void setOriginToCenter() {
        originX = (float) getWidth() / 2;
        originY = (float) getHeight() / 2;
    }

    @Override
    public void draw(SpriteBatch batch) {
        draw(batch, tint);
    }

    public void draw(SpriteBatch batch, Color tint) {
        if (texture != null) {
            batch.setColor(tint);
            batch.draw(
                    texture,
                    getX(),
                    getY(),
                    originX,
                    originY,
                    texture.getRegionWidth(),
                    texture.getRegionHeight(),
                    scale,
                    scale,
                    -rotation
            );
        }
    }

    public Color getTint() {
        return tint;
    }

    public void setTint(Color tint) {
        this.tint = tint;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
