package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;

public class Clock extends DrawableComponent {
    private final DrawableComponent minuteHand, hourHand;

    public Clock() {
        super(Textures.Ui.CLOCK_FACE);
        minuteHand = new DrawableComponent(Textures.Ui.MINUTE_HAND);
        hourHand = new DrawableComponent(Textures.Ui.HOUR_HAND);
        minuteHand.setOriginToCenter();
        hourHand.setOriginToCenter();
    }

    @Override
    public void draw(SpriteBatch batch) {
        minuteHand.setRotation((float) World.getTime() * 0.1f);
        hourHand.setRotation((float) 360 / 43200 * World.getTime());
        super.draw(batch);
        hourHand.draw(batch);
        minuteHand.draw(batch);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        minuteHand.setPosition(x, y);
        hourHand.setPosition(x, y);
    }
}
