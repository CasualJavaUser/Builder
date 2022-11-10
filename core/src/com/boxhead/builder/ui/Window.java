package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class Window extends UIElement implements Clickable {

    protected boolean isDragged = false;
    protected Button closeButton;
    private final Vector2i mousePositionOnClick = new Vector2i();
    protected int sizeX = 0, sizeY = 0;
    protected int windowWidth, windowHeight;

    public Window(TextureRegion texture) {
        this(texture, new Vector2i());
    }

    public Window(TextureRegion texture, Vector2i position) {
        this(texture, position, false);
    }

    public Window(TextureRegion texture, Vector2i position, boolean visible) {
        super(texture, position, visible);
        closeButton = new Button(Textures.get(Textures.Ui.CLOSE_BUTTON), new Vector2i(), this::hide, true);
    }

    @Override
    public boolean isClicked() {
        if (InputManager.isButtonDown(InputManager.LEFT_MOUSE)) {
            int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
            return x >= position.x && x < (position.x + windowWidth) &&
                    y <= position.y && y > (position.y - windowHeight);
        }
        return false;
    }

    @Override
    public boolean isHeld() {
        if (isClicked()) isDragged = true;
        if (isDragged) isDragged = InputManager.isButton(InputManager.LEFT_MOUSE);
        return isDragged;
    }

    @Override
    public void onClick() {
        mousePositionOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onHold() {
        if (closeButton.isClicked())
            closeButton.onClick();
        //closeButton.onClick() is called here instead of in the onClick() function because onClick() is called before onHold()
        //and the isDragged variable stayed true in spite of the left mouse button not being pressed.
        position.x += Gdx.input.getX() - mousePositionOnClick.x;
        position.y -= Gdx.input.getY() - mousePositionOnClick.y;

        final Range<Integer> rangeX = Range.between(0, Gdx.graphics.getWidth() - (sizeX + (texture.getRegionWidth()-1)*2));
        final Range<Integer> rangeY = Range.between(sizeY + (texture.getRegionHeight()-1)*2, Gdx.graphics.getHeight());

        position.x = rangeX.fit(position.x);
        position.y = rangeY.fit(position.y);

        mousePositionOnClick.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void draw(SpriteBatch batch) {
        windowWidth = sizeX + (texture.getRegionWidth()-1)*2;
        windowHeight = sizeY + (texture.getRegionHeight()-1)*2;
        int closeButtonX = getGlobalPosition().x + windowWidth - closeButton.getTexture().getRegionWidth();
        int closeButtonY = getGlobalPosition().y - closeButton.getTexture().getRegionHeight();
        closeButton.setLocalPosition(closeButtonX, closeButtonY);

        drawWindow(batch);
        closeButton.draw(batch);
    }

    @Override
    public void hide() {
        setVisible(false);
        isDragged = false;
    }

    private void drawWindow(SpriteBatch batch) {
        int width = texture.getRegionWidth(), height = texture.getRegionHeight();
        drawCorner(batch, position.x, position.y, false, false);
        drawCorner(batch, position.x + sizeX + width-1, position.y, true, false);
        drawCorner(batch, position.x + sizeX + width-1, position.y - sizeY - height + 1, true, true);
        drawCorner(batch, position.x, position.y - sizeY - height + 1, false, true);

        for (int i = 0; i < sizeX; i++) {
            drawEdge(batch, position.x + width - 1 + i, position.y, false, false, false);
            drawEdge(batch, position.x + width - 1 + i, position.y - sizeY - texture.getRegionHeight() + 1, false, true, false);
            for (int j = 0; j < sizeY; j++) {
                drawMiddle(batch, position.x + width - 1 + i, position.y - 1 - j);
            }
        }

        for (int i = 0; i < sizeY; i++) {
            drawEdge(batch, position.x, position.y - 1 - i, false, false, true);
            drawEdge(batch, position.x + sizeX + texture.getRegionWidth() - 1, position.y - 1 - i, true, false, true);
        }
    }

    private void drawCorner(SpriteBatch batch, int x, int y, boolean flipX, boolean flipY) {
        int width = texture.getRegionWidth(), height = texture.getRegionHeight();
        batch.draw(texture.getTexture(),
                x,
                y - height + 1,
                (float)width/2,
                (float)height/2,
                width-1,
                height-1,
                1,
                1,
                0,
                texture.getRegionX(),
                texture.getRegionY(),
                width-1,
                height-1,
                flipX,
                flipY);
    }

    private void drawEdge(SpriteBatch batch, int x, int y, boolean flipX, boolean flipY, boolean flip90) {
        int width = texture.getRegionWidth(), height = texture.getRegionHeight();
        batch.draw(texture.getTexture(),
                x,
                y - height + 1,
                (float)width/2,
                (float)height/2,
                flip90 ? width - 1 : 1,
                flip90 ? 1 : height - 1,
                1,
                1,
                0,
                flip90 ? texture.getRegionX() : texture.getRegionX() + width - 1,
                flip90 ? texture.getRegionY() + height - 1 : texture.getRegionY(),
                flip90 ? width - 1 : 1,
                flip90 ? 1 : height - 1,
                flipX,
                flipY);
    }

    private void drawMiddle(SpriteBatch batch, int x, int y) {
        int width = texture.getRegionWidth(), height = texture.getRegionHeight();
        batch.draw(texture.getTexture(),
                x,
                y - height + 1,
                (float)width/2,
                (float)height/2,
                1,
                1,
                1,
                1,
                0,
                texture.getRegionX() + width - 1,
                texture.getRegionY() + height - 1,
                1,
                1,
                false,
                false);
    }
}
