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
    public void onClick() {
        if (closeButton.isMouseOver()) closeButton.onClick();
        prevMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onHold() {
        isDragged = true;
        position.x += Gdx.input.getX() - prevMousePos.x;
        position.y -= Gdx.input.getY() - prevMousePos.y;

        Vector2i parentPos;
        if(parent != null) parentPos = parent.position;
        else parentPos = Vector2i.zero();
        final Range<Integer> rangeX = Range.between(
                -parentPos.x,
                Gdx.graphics.getWidth() - (getContentWidth() + (texture.getRegionWidth()-1)*2) - parentPos.x);
        final Range<Integer> rangeY = Range.between(
                getContentHeight() + (texture.getRegionHeight()-1)*2 - parentPos.y - getWindowHeight(),
                Gdx.graphics.getHeight() - parentPos.y - getWindowHeight());

        position.x = rangeX.fit(position.x);
        position.y = rangeY.fit(position.y);

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
