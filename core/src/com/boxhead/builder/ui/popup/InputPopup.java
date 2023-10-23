package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.TextField;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Consumer;

public class InputPopup extends Popup {
    private final Button okButton, cancelButton;
    private TextField textField;
    protected String prompt = "";
    protected Consumer<String> onAccept = null;

    private static InputPopup instance = null;

    protected InputPopup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position);
        setWindowWidth(160 + UI.PADDING * 3);
        setContentHeight(UI.PADDING * 4 + 64 + (int)UI.FONT.getCapHeight());

        textField = new TextField(prompt, Textures.get(Textures.Ui.TEXT_FIELD), this, UI.Layer.POPUP, new Vector2i());
        textField.setLocalPosition((getWindowWidth() - textField.getWidth())/2, UI.PADDING * 2 + 32);

        okButton = new Button(Textures.get(Textures.Ui.SMALL_BUTTON), this, UI.Layer.POPUP, new Vector2i(UI.PADDING, UI.PADDING), "OK");
        okButton.setOnUp(() -> {
            if(!textField.getText().equals("")) {
                layer.setVisible(false);
                onAccept.accept(textField.getText());
                textField.setText("");
                UI.setActiveTextField(null);
            }
        });

        cancelButton = new Button(Textures.get(Textures.Ui.SMALL_BUTTON), this, UI.Layer.POPUP, new Vector2i(80 + 2*UI.PADDING, UI.PADDING), "Cancel");
        cancelButton.setOnUp(() -> {
            layer.setVisible(false);
            UI.setActiveTextField(null);
        });
    }

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

    public TextField getTextField() {
        return getInstance().textField;
    }

    public Button getOkButton() {
        return okButton;
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
