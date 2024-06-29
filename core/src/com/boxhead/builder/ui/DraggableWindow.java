package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Range;
import com.boxhead.builder.utils.Vector2i;

public class DraggableWindow extends Window {
    protected boolean isDragged = false;
    protected Button closeButton;
    private final Vector2i prevMousePos = new Vector2i();

    public DraggableWindow(Style style, Pane pane) {
        super(style, pane);
        closeButton = new Button(Textures.Ui.CLOSE_BUTTON);
        closeButton.setOnClick(this::close);
    }

    @Override
    public UIComponent onClick() {
        prevMousePos.set(Gdx.input.getX(), Gdx.input.getY());
        if (closeButton.isMouseOver()) {
            closeButton.onClick();
            return closeButton;
        }
        if (tabs[0].onClick() == null)
            return this;
        else
            return tabs[0].onClick();
    }

    @Override
    public void onHold() {
        isDragged = true;
        Range<Integer> rangeX = Range.between(-getX(), Gdx.graphics.getWidth() - getX() - getWidth());
        Range<Integer> rangeY = Range.between(-getY(), Gdx.graphics.getHeight() - getY() - getHeight());
        move(rangeX.fit(Gdx.input.getX() - prevMousePos.x), rangeY.fit(-(Gdx.input.getY() - prevMousePos.y)));

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
        closeButton.setPosition(getX() + getWidth() - 40, getY() + getHeight() - 16);
        super.draw(batch);
        closeButton.draw(batch);
    }

    @Override
    public void close() {
        super.close();
        isDragged = false;
    }

    @Override
    public int getHeight() {
        return super.getHeight() + 12;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        tabs[0].move(x, y);
    }
}
