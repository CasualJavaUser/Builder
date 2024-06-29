package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class ImagePane extends Pane {
    private final Textures.Ui background;
    private final boolean vertical;
    private final int width, height;
    private int totalCompSize = 0;

    public ImagePane(Textures.Ui background, boolean vertical) {
        this(background, vertical, UI.PADDING);
    }

    public ImagePane(Textures.Ui background, boolean vertical, int padding) {
        this.background = background;
        this.vertical = vertical;
        this.padding = padding;
        width = Textures.get(background).getRegionWidth();
        height = Textures.get(background).getRegionHeight();
    }

    @Override
    public void pack() {
        if (totalCompSize > 0) totalCompSize -= padding;
        Vector2i nextPos;
        nextPos = new Vector2i(getX() + width / 2, getY() + height / 2);

        for (UIComponent component : components) {
            if (vertical) {
                nextPos.add(0, -component.getHeight());
                component.setPosition(nextPos.x - (component.getWidth() / 2), nextPos.y);
                component.move(0, totalCompSize / 2);
                nextPos.add(0, -padding);
            }
            else {
                component.setPosition(nextPos.x, nextPos.y - (component.getHeight() / 2));
                nextPos.add(component.getWidth(), 0);
                component.move(-totalCompSize / 2, 0);
                nextPos.add(padding, 0);
            }
            if (component instanceof Pane pane) pane.pack();
        }
    }

    @Override
    public void addUIComponent(UIComponent component) {
        super.addUIComponent(component);
        totalCompSize += vertical ? component.getHeight() : component.getWidth();
        totalCompSize += padding;
    }

    @Override
    public void clear() {
        super.clear();
        totalCompSize = 0;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(Textures.get(background), getX(), getY());
        super.draw(batch);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
