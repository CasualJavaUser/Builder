package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public abstract class Pane extends UIComponent {
    protected final List<UIComponent> components = new ArrayList<>();
    protected int padding;

    public void addUIComponent(UIComponent component) {
        components.add(component);
    }

    public void addUIComponents(UIComponent... comps) {
        for (UIComponent component : comps) {
            addUIComponent(component);
        }
    }

    public void pack() {

    }

    public void clear() {
        components.clear();
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        for (UIComponent component : components) {
            component.move(x, y);
        }
    }

    public void setTintCascading(Color tint) {
        for (UIComponent component : components) {
            if (component instanceof DrawableComponent dc) dc.setTint(tint);
            else if (component instanceof Pane p) p.setTintCascading(tint);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (isVisible()) {
            for (UIComponent component : components) {
                component.draw(batch);
            }
        }
    }

    @Override
    public UIComponent onClick() {
        for (UIComponent component : components) {
            if (component.isMouseOver()) {
                return component.onClick();
            }
        }
        return null;
    }

    @Override
    public void onHold() {
        for (UIComponent component : components) {
            if (component.isMouseOver()) {
                component.onHold();
                return;
            }
        }
    }

    @Override
    public void onUp() {
        for (UIComponent component : components) {
            if (component.isMouseOver()) {
                component.onUp();
                return;
            }
        }
    }
}
