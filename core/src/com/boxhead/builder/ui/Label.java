package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Label extends UIElement {
    private String text;

    public Label(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        this(texture, parent, layer, position, true, null);
    }

    public Label(TextureRegion texture, UI.Layer layer, Vector2i position, String text) {
        this(texture, null, layer, position, true, text);
    }

    public Label(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, boolean visible, String text) {
        super(texture, parent, layer, position, visible);
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, getGlobalPosition().x, getGlobalPosition().y - texture.getRegionWidth());
        UI.FONT.setColor(tint);
        UI.FONT.draw(batch, text, getGlobalPosition().x + texture.getRegionWidth() + 10, getGlobalPosition().y);
        UI.FONT.setColor(UI.DEFAULT_COLOR);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
