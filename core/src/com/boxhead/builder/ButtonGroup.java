package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ButtonGroup extends UIElement{
    private Button[] buttons;

    public ButtonGroup(TextureRegion background, Vector2i position, Button... buttons) {
        super(background, position);
        this.buttons = buttons;
    }

    public Button clicked() {
        Button clicked = null;
        for (Button button : buttons) {
            if(button.isClicked()) {
                clicked = button;
                break;
            }
        }
        return clicked;
    }

    @Override
    public void draw(SpriteBatch batch) {
        for (Button button : buttons) {
            button.draw(batch);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        for (Button button : buttons) {
            button.setVisible(visible);
        }
    }
}
