package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class Clock extends UIElement {

    private final UIElement minuteHand;
    private final UIElement hourHand;

    Clock(UIElement parent, UI.Layer layer, Vector2i position) {
        super(Textures.get(Textures.Ui.CLOCK_FACE), parent, layer, position, true);
        minuteHand = new UIElement(Textures.get(Textures.Ui.MINUTE_HAND), this, layer, Vector2i.zero(), true);
        hourHand = new UIElement(Textures.get(Textures.Ui.HOUR_HAND), this, layer, Vector2i.zero(), true);
        minuteHand.setOriginToCenter();
        hourHand.setOriginToCenter();
    }

    @Override
    public void draw(SpriteBatch batch) {
        minuteHand.setLocalRotation((float) World.getTime() * 0.1f);
        hourHand.setLocalRotation((float) 360 / 43200 * World.getTime());
        super.draw(batch);
    }

    @Override
    public void addToUI() {
        super.addToUI();
        minuteHand.addToUI();
        hourHand.addToUI();
    }
}
