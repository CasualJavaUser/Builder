package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Window extends UIElement implements Clickable {

    protected boolean isDragged = false;
    protected Button closeButton;
    private final Vector2i mouseOnClick = new Vector2i();

    public Window(TextureRegion texture) {
        this(texture, new Vector2i());
    }

    public Window(TextureRegion texture, Vector2i position) {
        this(texture, position, false);
    }

    public Window(TextureRegion texture, Vector2i position, boolean visible) {
        super(texture, position, visible);
        closeButton = new Button(Textures.get(Textures.Ui.CLOSE_BUTTON), new Vector2i()) {
            @Override
            public void onClick() {
                close();
            }
        };
    }

    @Override
    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + texture.getRegionWidth()) &&
                    y >= position.y && y < (position.y + texture.getRegionHeight());
        }
        return false;
    }

    @Override
    public boolean isHeld() {
        if(isClicked()) isDragged = true;
        if(isDragged) isDragged = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        return isDragged;
    }

    @Override
    public void onClick() {
        mouseOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onHold() {
        if(closeButton.isClicked()) closeButton.onClick();
        //closeButton.onClick() is called here instead of in the onClick() function because onClick() is called before onHold()
        //and the isDragged variable stayed true despite of the left mouse button not being pressed.
        position.x += Gdx.input.getX() - mouseOnClick.x;
        position.y -= Gdx.input.getY() - mouseOnClick.y;

        if (position.x > Gdx.graphics.getWidth() - texture.getRegionWidth())
            position.x = Gdx.graphics.getWidth() - texture.getRegionWidth();
        else if (position.x < 0)
                 position.x = 0;

        if (position.y > Gdx.graphics.getHeight() - texture.getRegionHeight())
            position.y = Gdx.graphics.getHeight() - texture.getRegionHeight();
        else if (position.y < 0)
                 position.y = 0;

        mouseOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        closeButton.setPosition(position.x + texture.getRegionWidth() - closeButton.getTexture().getRegionWidth(),
                position.y + texture.getRegionHeight() - closeButton.getTexture().getRegionHeight());
        closeButton.draw(batch);
    }

    private void close() {
        setVisible(false);
        isDragged = false;
    }
}
