package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Action;
import com.boxhead.builder.utils.Vector2i;

public class QuestionPopup extends Popup {
    private final Button okButton, cancelButton;
    protected Action onAccept = null;

    private static QuestionPopup instance = null;

    protected QuestionPopup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position);
        setWindowWidth(160 + 3*UI.PADDING);
        setWindowHeight(100);

        okButton = new Button(Textures.get(Textures.Ui.SMALL_BUTTON), this, UI.Layer.POPUP, new Vector2i(UI.PADDING, UI.PADDING), "OK");
        okButton.setOnUp(() -> {
            layer.setVisible(false);
            onAccept.execute();
        });

        cancelButton = new Button(Textures.get(Textures.Ui.SMALL_BUTTON), this, UI.Layer.POPUP, new Vector2i(80 + 2*UI.PADDING, UI.PADDING), "Cancel");
        cancelButton.setOnUp(() -> layer.setVisible(false));
    }

    protected static QuestionPopup getInstance() {
        if (instance == null) {
            instance = new QuestionPopup(
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

    public Button getOkButton() {
        return okButton;
    }

    @Override
    public void addToUI() {
        super.addToUI();
        okButton.addToUI();
        cancelButton.addToUI();
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        okButton.setTint(tint);
        cancelButton.setTint(tint);
    }
}
