package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class DraggableWindow extends Window implements Clickable {
    protected boolean isDragged = false;
    protected Button closeButton;
    private final Vector2i prevMousePos = new Vector2i();

    public DraggableWindow(TextureRegion texture, boolean isVisible) {
        this(texture, null, new Vector2i(), isVisible);
    }

    public DraggableWindow(TextureRegion texture, UIElement parent, Vector2i position) {
        this(texture, parent, position, true);
    }

    public DraggableWindow(TextureRegion texture, UIElement parent, Vector2i position, boolean isVisible) {
        super(texture, parent, position, isVisible);
        closeButton = new Button(Textures.get(Textures.Ui.CLOSE_BUTTON), this, new Vector2i(), () -> this.setVisible(false), true);
    }

    @Override
    public Clickable onClick() {
        if (closeButton.isMouseOver()) {
            closeButton.onClick();
            return closeButton;
        }
        prevMousePos.set(Gdx.input.getX(), Gdx.input.getY());
        return this;
    }

    @Override
    public void onHold() {
        isDragged = true;
        Move(Gdx.input.getX() - prevMousePos.x, -(Gdx.input.getY() - prevMousePos.y));

        Range<Integer> rangeX = Range.between(0, Gdx.graphics.getWidth() - getWindowWidth());
        Range<Integer> rangeY = Range.between(0, Gdx.graphics.getHeight() - getWindowHeight());

        setGlobalPosition(
                rangeX.fit(getGlobalPosition().x),
                rangeY.fit(getGlobalPosition().y)
        );

        prevMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onUp() {
        isDragged = false;
    }

    @Override
    public void setTint(Color tint) {
        super.setTint(tint);
        closeButton.setTint(tint);
    }

    @Override
    public void draw(SpriteBatch batch) {
        int closeButtonX = getWindowWidth() - closeButton.getTexture().getRegionWidth();
        int closeButtonY = getWindowHeight() - closeButton.getTexture().getRegionHeight();
        closeButton.setLocalPosition(closeButtonX, closeButtonY);
        super.draw(batch);
        closeButton.draw(batch);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(!visible) isDragged = false;
    }

    @Override
    public boolean isMouseOver() {
        int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + getWindowWidth()) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + getWindowHeight());
    }
}
