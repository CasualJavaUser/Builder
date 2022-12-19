package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Consumer;

public class InputPopup extends Popup {
    private final Button okButton, cancelButton;
    private TextField textField = null;
    protected String prompt = "";
    protected Consumer<String> onAccept = null;

    private static InputPopup instance = null;

    protected InputPopup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position);
        setWindowWidth(160 + 3*padding);
        setWindowHeight(139);

        TextureRegion textFieldTexture = Textures.get(Textures.Ui.TEXT_FIELD);
        textField = new TextField(prompt, textFieldTexture, this, UI.Layer.POPUP, new Vector2i((getWindowWidth() - textFieldTexture.getRegionWidth())/2, padding*2 + 32), () -> {
            if(!textField.getText().equals("")) {
                layer.setVisible(false);
                onAccept.accept(textField.getText());
                textField.setText("");
            }
        });

        okButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), this, UI.Layer.POPUP, new Vector2i(padding, padding), "OK", () -> {
            if(!textField.getText().equals("")) {
                layer.setVisible(false);
                onAccept.accept(textField.getText());
                textField.setText("");
                UI.setActiveTextField(null);
            }
        }, false);

        cancelButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), this, UI.Layer.POPUP, new Vector2i(80 + 2*padding, padding), "Cancel", () -> {
            layer.setVisible(false);
            textField.setText("");
            UI.setActiveTextField(null);
        }, false);
    }

    /*protected static void show(String text, String prompt, Consumer<String> onAccept) {
        getInstance();
        instance.text = text;
        instance.prompt = prompt;
        instance.onAccept = onAccept;
        instance.setTint(UI.WHITE);
        instance.setVisible(true);
    }*/

    protected static InputPopup getInstance() {
        if (instance == null) {
            instance = new InputPopup(
                    Textures.get(Textures.Ui.WINDOW),
                    UI.Anchor.CENTER.getElement(),
                    UI.Layer.POPUP,
                    new Vector2i());
            instance.setLocalPosition(-instance.getWindowWidth()/2, -instance.getWindowHeight()/2);
            instance.setTint(UI.WHITE);
            instance.addToUI();
        }
        return instance;
    }

    public static TextField getTextField() {
        return getInstance().textField;
    }

    @Override
    public void addToUI() {
        super.addToUI();
        okButton.addToUI();
        cancelButton.addToUI();
        textField.addToUI();
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        okButton.setTint(tint);
        cancelButton.setTint(tint);
        textField.setTint(tint);
    }
}
