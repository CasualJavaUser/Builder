package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.utils.Range;
import com.boxhead.builder.utils.Vector2i;

public class ScrollPane extends BoxPane {
    UIComponent firstComponent = null, lastComponent = null;
    private final Vector2i nextPos = new Vector2i();

    public ScrollPane(int width, int height) {
        this(width, height, UI.PADDING);
    }

    public ScrollPane(int width, int height, int padding) {
        super(true, padding);
        this.width = width;
        this.height = height;
    }

    public void scroll() {
        if (!InputManager.isScrolled()) return;

        if (!components.isEmpty()) {
            int scroll = (int) (InputManager.getScroll() * InputManager.SCROLL_SENSITIVITY);
            int delta1 = getY() + height - firstComponent.getHeight() - firstComponent.getY();
            int delta2 = getY() - lastComponent.getY();
            Range<Integer> range = Range.between(delta2, delta1);
            scroll = range.fit(scroll);
            for (UIComponent component : components) {
                component.move(0, scroll);
            }
        }
    }

    public void scrollToBottom() {
        if (!components.isEmpty()) {
            int delta = getY() - lastComponent.getY();
            for (UIComponent component : components) {
                component.move(0, delta);
            }
        }
    }

    @Override
    public void addUIComponent(UIComponent component) {
        components.add(component);
        if (firstComponent == null) firstComponent = component;
        lastComponent = component;
    }

    @Override
    public void pack() {
        nextPos.set(getX() + width / 2, getY() + height);
        for (UIComponent component : components) {
            nextPos.add(0, -component.getHeight());
            component.setPosition(nextPos.x - component.getWidth() / 2, nextPos.y);
            nextPos.add(0, -padding);
            if (component instanceof Pane pane) pane.pack();
        }
    }

    @Override
    public void clear() {
        components.clear();
        firstComponent = null;
        lastComponent = null;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.flush();
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(
                getX(),
                getY(),
                width,
                height
        );
        super.draw(batch);
        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
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
