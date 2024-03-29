package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

public class InfoPopup extends Popup {
    private static InfoPopup instance = null;

    protected InfoPopup(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        super(texture, parent, layer, position);
        setWindowWidth(160 + UI.PADDING * 3);
        setContentHeight((int)(UI.PADDING * 2 + UI.FONT.getCapHeight()));
    }

    protected static InfoPopup getInstance() {
        if(instance == null) {
            instance = new InfoPopup(
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

    @Override
    public void setText(String text) {
        super.setText(text);
        setContentWidth(UI.PADDING * 2 + text.length() * 8);
        instance.setLocalPosition(-instance.getWindowWidth()/2, -instance.getWindowHeight()/2);
    }
}
