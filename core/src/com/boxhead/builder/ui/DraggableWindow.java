package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;
import com.boxhead.builder.utils.Range;

public class DraggableWindow extends Window implements Clickable {
    protected boolean isDragged = false;
    protected Button closeButton;
    private final Vector2i prevMousePos = new Vector2i();

    public DraggableWindow(TextureRegion texture, UI.Layer layer, boolean isVisible) {
        this(texture, null, layer, new Vector2i(), isVisible);
    }

    public DraggableWindow(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        this(texture, parent, layer, position, true);
    }

    public DraggableWindow(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, boolean isVisible) {
        super(texture, parent, layer, position, isVisible);
        closeButton = new Button(Textures.get(Textures.Ui.CLOSE_BUTTON), this, layer, new Vector2i());
        closeButton.setOnClick(() -> this.setVisible(false));
    }

    @Override
    public void onClick() {
        prevMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onHold() {
        isDragged = true;
        move(Gdx.input.getX() - prevMousePos.x, -(Gdx.input.getY() - prevMousePos.y));

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
    public void setWindowWidth(int width) {
        super.setWindowWidth(width);
        closeButton.setLocalPosition(
                getWindowWidth() - 40,
                closeButton.getLocalPosition().y);
    }

    @Override
    public void setWindowHeight(int height) {
        super.setWindowHeight(height);
        closeButton.setLocalPosition(
                closeButton.getLocalPosition().x,
                getWindowHeight() - 4);
    }

    @Override
    public void setContentWidth(int width) {
        super.setContentWidth(width);
        closeButton.setLocalPosition(
                getWindowWidth() - 40,
                closeButton.getLocalPosition().y);
    }

    @Override
    public void setContentHeight(int height) {
        super.setContentHeight(height);
        closeButton.setLocalPosition(
                closeButton.getLocalPosition().x,
                getWindowHeight() - 4);
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

    @Override
    public void addToUI() {
        super.addToUI();
        closeButton.addToUI();
    }
}
