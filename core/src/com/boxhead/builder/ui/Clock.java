package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class Clock extends UIElement {

    private final UIElement minuteHand;
    private final UIElement hourHand;

    Clock(UIElement parent, Vector2i position) {
        super(Textures.get(Textures.Ui.CLOCK_FACE), parent, position, true);
        minuteHand = new UIElement(Textures.get(Textures.Ui.MINUTE_HAND), this, Vector2i.zero(), true);
        hourHand = new UIElement(Textures.get(Textures.Ui.HOUR_HAND), this, Vector2i.zero(), true);
    }

    @Override
    public void draw(SpriteBatch batch) {
        minuteHand.setLocalRotation((float) World.getTime() * 0.1f);
        hourHand.setLocalRotation((float) 360 / 43200 * World.getTime());

        super.draw(batch);
        minuteHand.draw(batch);
        hourHand.draw(batch);
    }
}
