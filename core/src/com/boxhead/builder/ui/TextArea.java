package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class TextArea extends UIElement {
    private String text;
    private int targetWidth;
    private Align hAlign;

    public enum Align {
        LEFT(-1),
        CENTER(1),
        RIGHT(0);

        private int num;

        Align(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }
    }

    public TextArea(TextureRegion texture, String text, UIElement parent, UI.Layer layer, Vector2i position, Align align) {
        this(texture, text, parent, layer, position, texture.getRegionWidth(), align);
    }

    public TextArea(String text, UIElement parent, UI.Layer layer, Vector2i position, int targetWidth, Align align) {
        this(null, text, parent, layer, position, targetWidth, align);
    }

    private TextArea(TextureRegion texture, String text, UIElement parent, UI.Layer layer, Vector2i position, int targetWidth, Align align) {
        super(texture, parent, layer, position);
        this.text = text;
        this.targetWidth = targetWidth;
        hAlign = align;
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
        UI.FONT.draw(batch, text, getGlobalPosition().x, y, targetWidth, hAlign.getNum(), false);
    }
}
