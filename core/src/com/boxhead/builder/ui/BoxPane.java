package com.boxhead.builder.ui;

import com.boxhead.builder.utils.Vector2i;

public class BoxPane extends Pane {
    private final boolean vertical;
    protected int width = 0, height = 0;

    public BoxPane() {
        this(true, UI.PADDING);
    }

    public BoxPane(boolean vertical) {
        this(vertical, UI.PADDING);
    }

    public BoxPane(boolean vertical, int padding) {
        this.vertical = vertical;
        this.padding = padding;
        if (vertical) height = -padding;
        else width = -padding;
    }

    @Override
    public void pack() {
        Vector2i nextPos;
        if (vertical) {
            nextPos = new Vector2i(getX() + width/2 + padding, getY() + height + padding);
        }
        else {
            nextPos = new Vector2i(getX() + padding, getY() + padding);
        }

        for (UIComponent component : components) {
            if (vertical) {
                nextPos.add(0, -component.getHeight());
                component.setPosition(nextPos.x - component.getWidth()/2, nextPos.y);
                nextPos.add(0, -padding);
            }
            else {
                component.setPosition(nextPos.x, nextPos.y);
                nextPos.add(component.getWidth() + padding, 0);
            }
            if (component instanceof Pane pane) pane.pack();
        }
    }

    @Override
    public void clear() {
        super.clear();
        width = 0;
        height = 0;
    }

    @Override
    public void addUIComponent(UIComponent component) {
        super.addUIComponent(component);
        if (vertical) {
            if (width < component.getWidth()) width = component.getWidth();
            height += component.getHeight() + padding;
        }
        else {
            if (height < component.getHeight()) height = component.getHeight();
            width += component.getWidth() + padding;
        }
    }

    @Override
    public int getWidth() {
        return width + padding * 2;
    }

    @Override
    public int getHeight() {
        return height + padding * 2;
    }
}
