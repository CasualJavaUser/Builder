package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class ButtonGroup extends UIElement implements Clickable {
    private Button[] buttons;

    public ButtonGroup(TextureRegion background, Vector2i position, Button... buttons) {
        super(background, position);
        this.buttons = buttons;
    }

    private Button clickedButton() {
        Button clicked = null;
        for (Button button : buttons) {
            if(button.isClicked()) {
                clicked = button;
                break;
            }
        }
        return clicked;
    }

    private Button buttonHeld() {
        Button held = null;
        for (Button button : buttons) {
            if(button.isHeld()) {
                held = button;
                break;
            }
        }
        return held;
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

    @Override
    public boolean isClicked() {
        for (Button button : buttons) {
            if (button.isClicked()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isHeld() {
        for (Button button : buttons) {
            if (button.isHeld()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick() {
        if (clickedButton() != null) {
            clickedButton().onClick();
        }
    }

    @Override
    public void onHold() {
        if (buttonHeld() != null) {
            buttonHeld().onHold();
        }
    }
}
