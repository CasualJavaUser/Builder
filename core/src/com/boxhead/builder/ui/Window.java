package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Window extends UIElement {
    private int contentWidth = 0, contentHeight = 0;
    private int windowWidth, windowHeight;

    public Window(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position) {
        this(texture, parent, layer, position, true);
    }

    public Window(TextureRegion texture, UIElement parent, UI.Layer layer, Vector2i position, boolean visible) {
        super(texture, parent, layer, position, visible);
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(tint);
        drawWindow(batch);
        batch.setColor(UI.DEFAULT_COLOR);
    }

    protected void drawWindow(SpriteBatch batch) {
        Vector2i position = getGlobalPosition();
        int width = texture.getRegionWidth() - 1, height = texture.getRegionHeight() - 1;
        //top left
        drawCorner(batch, position.x, position.y + contentHeight + height + height, false, true);
        //top right
        drawCorner(batch, position.x + contentWidth + width, position.y + contentHeight + height + height, true, true);
        //bottom right
        drawCorner(batch, position.x + contentWidth + width, position.y + height, true, false);
        //bottom left
        drawCorner(batch, position.x, position.y + height, false, false);

        for (int i = 0; i < contentWidth; i++) {

            //top edge
            drawEdge(batch, position.x + width + i, position.y + contentHeight + height + height, false, true, false);
            //botom edge
            drawEdge(batch, position.x + width + i, position.y + height, false, false, false);
            for (int j = 0; j < contentHeight; j++) {
                drawMiddle(batch, position.x + width + i, position.y + height + j + height);
            }
        }

        for (int i = 0; i < contentHeight; i++) {
            //left edge
            drawEdge(batch, position.x, position.y + height + i + height, false, false, true);
            //right edge
            drawEdge(batch, position.x + contentWidth + texture.getRegionWidth() - 1, position.y + height + i + height, true, false, true);
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
                texture.getRegionY() + 1,
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
                flip90 ? texture.getRegionY() : texture.getRegionY() + 1,
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
                texture.getRegionY(),
                1,
                1,
                false,
                false);
    }

    public void setWidth(int width) {
        contentWidth = width;
        windowWidth = contentWidth + (texture.getRegionWidth()-1)*2;
    }

    public void setWindowWidth(int width) {
        contentWidth = width - (texture.getRegionWidth()-1)*2;
        windowWidth = width;
    }

    public void setHeight(int height) {
        contentHeight = height;
        windowHeight = contentHeight + (texture.getRegionHeight()-1)*2;
    }

    public void setWindowHeight(int height) {
        contentHeight = height - (texture.getRegionHeight()-1)*2;
        windowHeight = height;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public int getContentWidth() {
        return contentWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }
}
