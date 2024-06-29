package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.utils.Vector2i;

public abstract class UIComponent {
    private final Vector2i position = new Vector2i();
    private boolean visible = true;
    private boolean enabled = true;

    public abstract void draw(SpriteBatch batch);

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean isMouseOver() {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        return x >= position.x && x < (position.x + getWidth()) &&
                y >= position.y && y < (position.y + getHeight());
    }

    /**
     * @return the clicked ui component
     */
    public UIComponent onClick() {
        return null;
    }

    public void onHold() {}

    public void onUp() {}

    public void setPosition(Vector2i position) {
        setPosition(position.x, position.y);
    }

    public void setPosition(int x, int y) {
        this.position.set(x, y);
    }

    public void move(int x, int y) {
        this.position.add(x, y);
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
