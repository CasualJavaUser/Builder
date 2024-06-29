package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.Textures;

public class TextField extends DrawableComponent {
    private final String prompt;
    public String text = "";
    
    public TextField(String prompt, Textures.Ui background) {
        super(background);
        this.prompt = prompt;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        batch.flush();

        String displayedText = text;
        if(text.isEmpty()) {
            UI.FONT.setColor(UI.PRESSED_COLOR);
            displayedText = prompt;
        }
        else UI.FONT.setColor(getTint());

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(
                getX() + UI.PADDING,
                getY(),
                getWidth() - UI.PADDING * 2,
                getHeight());

        UI.FONT.draw(
                batch,
                displayedText,
                getX() + UI.PADDING,
                getY() + (getHeight() + UI.FONT_WIDTH)/2f,
                getWidth() - UI.PADDING*2,
                0,
                false);
        UI.FONT.setColor(UI.DEFAULT_COLOR);

        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override
    public UIComponent onClick() {
        UI.setActiveTextField(this);
        return this;
    }

    public void write() {
        char key = InputManager.getKeyTyped();
        if (key >= ' ' && key <= '~') text += key;
        else if (key == '\b' && !text.isEmpty()) {
            if (!InputManager.isKeyDown(InputManager.KeyBinding.PLACE_MULTIPLE)) {
                text = text.substring(0, text.length() - 1);
            }
            else {
                text = text.stripTrailing();
                if (!text.contains(" ")) text = "";
                else text = text.substring(0, text.lastIndexOf(" "));
            }
        }
    }
}
