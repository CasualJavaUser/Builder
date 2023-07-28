package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class TextArea extends UIElement {
    private String text;
    private int targetWidth, halign;

    public TextArea(TextureRegion texture, String text, UIElement parent, UI.Layer layer, Vector2i position, boolean center) {
        this(texture, text, parent, layer, position, texture.getRegionWidth(), center);
    }

    public TextArea(String text, UIElement parent, UI.Layer layer, Vector2i position, int targetWidth, boolean center) {
        this(null, text, parent, layer, position, targetWidth, center);
    }

    private TextArea(TextureRegion texture, String text, UIElement parent, UI.Layer layer, Vector2i position, int targetWidth, boolean center) {
        super(texture, parent, layer, position);
        this.text = text;
        this.targetWidth = targetWidth;
        halign = center ? 1 : -1;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        int y = getGlobalPosition().y;
        if (texture != null) y = (int)(getGlobalPosition().y + texture.getRegionHeight()/2 + UI.FONT.getXHeight());
        UI.FONT.setColor(tint);
        UI.FONT.draw(batch, text, getGlobalPosition().x, y, targetWidth, halign, false);
    }
}
