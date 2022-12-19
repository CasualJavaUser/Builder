package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.utils.Action;
import com.boxhead.builder.utils.Vector2i;

public class TextField extends UIElement implements Clickable {
    private String prompt, text = "";
    private final int padding = 10;
    private Action onEnter;

    public TextField(String prompt, TextureRegion background, UIElement parent, UI.Layer layer, Vector2i position, Action onEnter) {
        super(background, parent, layer, position);
        this.prompt = prompt;
        this.onEnter = onEnter;
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
                getGlobalPosition().x * 2 + padding*2,
                getGlobalPosition().y * 2,
                texture.getRegionWidth()*2 - padding*4,
                texture.getRegionHeight()*2);

        UI.FONT.draw(
                batch,
                txt,
                getGlobalPosition().x + padding,
                getGlobalPosition().y + (texture.getRegionHeight() + UI.FONT_SIZE)/2f - 2,
                texture.getRegionWidth() - padding*2,
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
        else if (key == '\b' && text.length() > 0) text = text.substring(0, text.length()-1);
        else if (key == '\n') onEnter.execute();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
