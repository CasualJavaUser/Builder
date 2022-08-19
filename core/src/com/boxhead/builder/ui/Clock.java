package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class Clock extends UIElement {

    private final UIElement minuteHand;
    private final UIElement hourHand;

    Clock(Vector2i position) {
        super(Textures.get(Textures.Ui.CLOCK_FACE), position, true);
        minuteHand = new UIElement(Textures.get(Textures.Ui.MINUTE_HAND), position, true);
        hourHand = new UIElement(Textures.get(Textures.Ui.HOUR_HAND), position, true);
    }

    @Override
    public void draw(SpriteBatch batch) {
        minuteHand.setRotation((float) World.getTime() * 0.1f);
        hourHand.setRotation((float) 360 / 43200 * World.getTime());

        super.draw(batch);
        minuteHand.draw(batch);
        hourHand.draw(batch);
    }
}
