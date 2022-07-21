package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Label extends UIElement {
    String text;

    public Label(TextureRegion texture, Vector2i position) {
        this(texture, position, false, null);
    }

    public Label(TextureRegion texture, Vector2i position, String text) {
        this(texture, position, false, text);
    }

    public Label(TextureRegion texture, Vector2i position, boolean visible, String text) {
        super(texture, position, visible);
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y - texture.getRegionWidth());
        UI.FONT.draw(batch, text, position.x + texture.getRegionWidth() + 10, position.y);
    }

    public void setText(String text) {
        this.text = text;
    }
}
