package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Consumer;

public class InputPopup extends Window implements Clickable {
    private final Button okButton, cancelButton;
    private final String prompt;
    private TextField textField = null;
    private static final int padding = 10;

    public InputPopup(TextureRegion texture, UIElement parent, Vector2i position, String prompt, Consumer<String> onAccept) {
        super(texture, parent, position, false);
        this.prompt = prompt;
        setWindowWidth(160 + 3*padding);
        setWindowHeight(139);

        TextureRegion textFieldTexture = Textures.get(Textures.Ui.TEXT_FIELD);
        textField = new TextField(prompt, textFieldTexture, this, new Vector2i((getWindowWidth() - textFieldTexture.getRegionWidth())/2, padding*2 + 32), () -> {
            if(!textField.getText().equals("")) onAccept.accept(textField.getText());
            setVisible(false);
            textField.setText("");
        });

        okButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), this, new Vector2i(padding, padding), "OK", () -> {
            if(!textField.getText().equals("")) onAccept.accept(textField.getText());
            setVisible(false);
            textField.setText("");
        }, false);

        cancelButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), this, new Vector2i(80 + 2*padding, padding), "Cancel", () -> {
            setVisible(false);
            textField.setText("");
        }, false);
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        okButton.setTint(tint);
        cancelButton.setTint(tint);
        if(textField != null) textField.setTint(tint);
    }

    @Override
    public Clickable onClick() {
        if(okButton.isMouseOver()) return okButton.onClick();
        if(cancelButton.isMouseOver()) return cancelButton.onClick();
        if(textField.isMouseOver()) return textField.onClick();
        return this;
    }

    @Override
    public boolean isMouseOver() {
        return okButton.isMouseOver() || cancelButton.isMouseOver() || textField.isMouseOver();
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        UI.FONT.draw(batch, prompt, getGlobalPosition().x, getGlobalPosition().y + getContentHeight() - padding, getWindowWidth(), 1, false);
        textField.draw(batch);
        okButton.draw(batch);
        cancelButton.draw(batch);
    }
}
