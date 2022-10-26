package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Label extends UIElement {
    String text;

    public Label(TextureRegion texture, UIElement parent, Vector2i position) {
        this(texture, parent, position, false, null);
    }

    public Label(TextureRegion texture, Vector2i position, String text) {
        this(texture, null, position, false, text);
    }

    public Label(TextureRegion texture, UIElement parent, Vector2i position, boolean visible, String text) {
        super(texture, parent, position, visible);
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, getGlobalPosition().x, getGlobalPosition().y - texture.getRegionWidth());
        UI.FONT.draw(batch, text, getGlobalPosition().x + texture.getRegionWidth() + 10, getGlobalPosition().y);
    }

    public void setText(String text) {
        this.text = text;
    }
}
