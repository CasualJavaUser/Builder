package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

public class WarningPopup extends Popup {
    private Button okButton;

    private static WarningPopup instance = null;

    protected WarningPopup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position);
        setWindowWidth(160 + 3*UI.PADDING);
        setWindowHeight(100);

        okButton = new Button(Textures.get(Textures.Ui.SMALL_BUTTON), this, UI.Layer.POPUP, new Vector2i(), "Ok");
        okButton.setOnUp(() -> layer.setVisible(false));
        okButton.setLocalPosition(getWindowWidth()/2 - okButton.getTexture().getRegionWidth()/2, UI.PADDING);
    }

    protected static WarningPopup getInstance() {
        if(instance == null) {
            instance = new WarningPopup(
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
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        okButton.setTint(tint);
    }
}
