package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

import java.util.*;

public class ScrollPane extends UIElement {
    private Set<UIElement> elements = new HashSet<>();
    private UIElement firstElement = null, lastElement = null;
    private final int padding = 10;
    private int width, height;
    private Vector2i nextPos;
    private boolean scrollable = false;

    public ScrollPane(UIElement parent, UI.Layer layer, Vector2i position, int width, int height) {
        super(parent, layer, position, true);
        this.width = width;
        this.height = height;
        nextPos = new Vector2i(width/2, height);
    }

    public ScrollPane(UIElement parent, UI.Layer layer, int x1, int y1, int x2, int y2) {
        this(parent, layer, new Vector2i(x1, y1), x2-x1, y2-y1);
    }

    public void addElement(UIElement element) {
        if(elements.isEmpty()) firstElement = element;
        lastElement = element;

        elements.add(element);
        element.setScissors(
                getGlobalPosition().x,
                getGlobalPosition().y,
                width,
                height);
        element.setParent(this);
        element.setLocalPosition(
                nextPos.x - element.getWidth() / 2,
                nextPos.y - element.getHeight());
        nextPos.set(nextPos.x, nextPos.y - element.getHeight() - padding);

        if(firstElement.getGlobalPosition().y + firstElement.getHeight() - lastElement.getGlobalPosition().y > height)
            scrollable = true;
    }

    public void clear() {
        for (UIElement element : elements) {
            element.removeScissors();
        }
        nextPos.set(width/2, height);
        elements.clear();
        firstElement = null;
        lastElement = null;
        scrollable = false;
    }

    public boolean isMouseOver() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= getGlobalPosition().x && x < (getGlobalPosition().x + width) &&
                y >= getGlobalPosition().y && y < (getGlobalPosition().y + height);
    }

    public void scroll() {
        if(!elements.isEmpty() && scrollable) {
            int scroll = (int) (InputManager.getScroll() * 10);
            if (scroll != 0) {
                int delta1 = (getGlobalPosition().y + height - firstElement.getHeight()) - firstElement.getGlobalPosition().y;
                int delta2 = getGlobalPosition().y - lastElement.getGlobalPosition().y;
                Range<Integer> range = Range.between(delta2, delta1);
                scroll = range.fit(scroll);
                for (UIElement element : elements) {
                    element.setGlobalPosition(
                            element.getGlobalPosition().x,
                            element.getGlobalPosition().y + scroll);
                }
            }
        }
    }
}
