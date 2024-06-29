package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
public class TextArea extends DrawableComponent {
    public String text;
    private int width;
    private Align hAlign;

    public enum Align {
        LEFT(-1),
        CENTER(1),
        RIGHT(0);

        private final int value;

        Align(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private TextArea(Textures.Ui texture, String text, int width, Align hAlign) {
        super(texture);
        this.text = text;
        this.width = width;
        this.hAlign = hAlign;
    }

    public TextArea(Textures.Ui texture, String text, Align hAlign) {
        this(texture, text, Textures.get(texture).getRegionWidth(), hAlign);
    }

    public TextArea(String text, int width, Align hAlign) {
        this(null, text, width, hAlign);
    }

    public TextArea(String text) {
        this(null, text, (int) (text.length() * UI.FONT_WIDTH), Align.CENTER);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public UIComponent onClick() {
        return null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        if (texture != null)
            return texture.getRegionHeight();
        return text.split("\n").length * UI.FONT_HEIGHT;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        UI.FONT.setColor(getTint());
        UI.FONT.draw(batch, text, getX(), getY() + text.split("\n").length * UI.FONT_HEIGHT - 5, width, hAlign.getValue(), false);
    }
}
