package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Action;

public class Button extends DrawableComponent {
    private String text;
    private boolean pressed = false;
    private final TextArea.Align hAlign;
    private Action onClick = () -> {};
    private Action onUp = () -> {};

    public Button(Textures.Ui texture, String text, TextArea.Align hAlign) {
        super(texture);
        this.text = text;
        this.hAlign = hAlign;
    }
    
    public Button(Textures.Ui texture, String text) {
        this(texture, text, TextArea.Align.CENTER);
    }
    
    public Button(Textures.Ui textures) {
        this(textures, "", TextArea.Align.CENTER);
    }

    @Override
    public UIComponent onClick() {
        onClick.execute();
        return this;
    }

    @Override
    public void onHold() {
        pressed = true;
    }

    @Override
    public void onUp() {
        onUp.execute();
        pressed = false;
    }
    
    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(pressed ? UI.PRESSED_COLOR : getTint());
        super.draw(batch, batch.getColor());
        if(text != null) {
            UI.FONT.setColor(getTint());
            UI.FONT.draw(
                    batch,
                    text,
                    getX() + UI.PADDING,
                    getY() + getHeight()/2f + UI.FONT_HEIGHT/2f - 5,
                    texture.getRegionWidth() - 2 * UI.PADDING,
                    hAlign.getValue(),
                    false);
        }
    }

    public void setOnClick(Action onClick) {
        this.onClick = onClick;
    }

    public void setOnUp(Action onUp) {
        this.onUp = onUp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}