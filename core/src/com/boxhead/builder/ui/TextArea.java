package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.utils.Vector2i;

public class TextArea extends UIElement {
    private String text;
    private int targetWidth, halign;

    public TextArea(String text, UIElement parent, Vector2i position) {
        this(text, parent, position, 0, false);
    }

    public TextArea(String text, UIElement parent, Vector2i position, int targetWidth, boolean center) {
        super(null, parent, position);
        this.text = text;
        this.targetWidth = targetWidth;
        halign = center ? 1 : 0;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(tint);
        UI.FONT.draw(batch, text, getGlobalPosition().x, getGlobalPosition().y, targetWidth, halign, false);
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
