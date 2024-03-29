package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.ui.Window;
import com.boxhead.builder.utils.Vector2i;

public abstract class Popup extends Window {
    protected String text = "";

    protected Popup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position, true);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        UI.FONT.setColor(tint);
        UI.FONT.draw(batch, text, getGlobalPosition().x, getGlobalPosition().y + getWindowHeight() - getEdgeWidth() - UI.PADDING, getWindowWidth(), 1, false);
        UI.FONT.setColor(UI.DEFAULT_COLOR);
    }
}
