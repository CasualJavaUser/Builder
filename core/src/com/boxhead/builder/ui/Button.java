package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Action;
import com.boxhead.builder.utils.Vector2i;

public class Button extends UIElement implements Clickable {

    private Action onClick = () -> {};
    private Action onUp = () -> {};
    private String text;
    private Color defaultTint;
    private final TextArea.Align hAlign;

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, String text, TextArea.Align align) {
        super(texture, parent, layer, position);
        this.text = text;
        defaultTint = tint;
        hAlign = align;
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, String text) {
        this(texture, parent, layer, position, text, TextArea.Align.CENTER);
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        this(texture, parent, layer, position, null);
    }

    public Button(TextureRegion texture, UI.Layer layer, Vector2i position) {
        this(texture, null, layer, position, null);
    }

    public void setOnClick(Action action) {
        onClick = action;
    }

    public void setOnUp(Action onUp) {
        this.onUp = onUp;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        defaultTint = tint;
    }

    @Override
    public boolean isMouseOver() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + texture.getRegionWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + texture.getRegionHeight());
    }

    @Override
    public void onClick() {
        onClick.execute();
    }

    @Override
    public void onHold() {
        Clickable.super.onHold();
        tint = UI.PRESSED_COLOR;
    }

    @Override
    public void onUp() {
        onUp.execute();
        tint = defaultTint;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        batch.setColor(tint);
        if(text != null) {
            UI.FONT.setColor(tint);
            UI.FONT.draw(
                    batch,
                    text,
                    getGlobalPosition().x + UI.PADDING,
                    getGlobalPosition().y + (int)(texture.getRegionHeight()/2 + UI.FONT.getCapHeight()/2),
                    texture.getRegionWidth() - 2 * UI.PADDING,
                    hAlign.getNum(),
                    false);
            UI.FONT.setColor(UI.DEFAULT_COLOR);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
