package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.utils.Vector2i;

public class TextField extends UIElement implements Clickable {
    private String prompt, text = "";

    public TextField(String prompt, TextureRegion background, UIElement parent, UI.Layer layer, Vector2i position) {
        super(background, parent, layer, position);
        this.prompt = prompt;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        batch.flush();

        batch.setColor(tint);
        String txt = text;
        if(text.equals("")) {
            UI.FONT.setColor(UI.PRESSED_COLOR);
            txt = prompt;
        }
        else UI.FONT.setColor(tint);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(
                getGlobalPosition().x + UI.PADDING,
                getGlobalPosition().y,
                texture.getRegionWidth() - UI.PADDING * 2,
                texture.getRegionHeight());

        UI.FONT.draw(
                batch,
                txt,
                getGlobalPosition().x + UI.PADDING,
                getGlobalPosition().y + (texture.getRegionHeight() + UI.FONT.getCapHeight())/2f - 2,
                texture.getRegionWidth() - UI.PADDING*2,
                0,
                false);
        UI.FONT.setColor(UI.DEFAULT_COLOR);

        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        batch.setColor(UI.DEFAULT_COLOR);
    }

    @Override
    public void onClick() {
        UI.setActiveTextField(this);
    }

    @Override
    public boolean isMouseOver() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + texture.getRegionWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + texture.getRegionHeight());
    }

    public void write() {
        char key = InputManager.getKeyTyped();
        if (key >= ' ' && key <= '~') text += key;
        else if (key == '\b' && text.length() > 0) {
            if (!InputManager.isKeyDown(InputManager.CONTROL)) {
                text = text.substring(0, text.length() - 1);
            }
            else {
                text = text.stripTrailing();
                if (!text.contains(" ")) text = "";
                else text = text.substring(0, text.lastIndexOf(" "));
            }
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
