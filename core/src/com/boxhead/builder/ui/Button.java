package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;
import com.boxhead.builder.utils.Action;

public class Button extends UIElement implements Clickable {

    private Action onClick = () -> {};
    private Action onUp = () -> {};
    private String text;
    private Color defaultTint;

    public Button(TextureRegion texture, UI.Layer layer, Vector2i position, Action action, boolean onDown) {
        this(texture, null, layer, position, null, action, onDown);
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, Action action) {
        this(texture, parent, layer, position, null, action, false);
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, String text, Action action) {
        this(texture, parent, layer, position, text, action, false);
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, Action action, boolean onDown) {
        this(texture, parent, layer, position, null, action, onDown);
    }

    public Button(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, String text, Action action, boolean onDown) {
        super(texture, parent, layer, position);
        if(onDown) onClick = action;
        else onUp = action;
        this.text = text;
        defaultTint = tint;
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
                    getGlobalPosition().x,
                    getGlobalPosition().y + (int)(texture.getRegionHeight()/2 + UI.FONT_SIZE/2),
                    texture.getRegionWidth(),
                    1,
                    false);
            UI.FONT.setColor(UI.DEFAULT_COLOR);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
