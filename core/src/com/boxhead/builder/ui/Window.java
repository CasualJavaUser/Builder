package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Action;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

public class Window extends DrawableComponent {
    protected final Pane[] tabs;
    protected final Button[] tabButtons;
    protected int activeTab = 0;
    private Action onClose = () -> {};
    private Action onOpen = () -> {};

    public enum Style {
        THICK,
        THIN
    }

    public Window(Style style, Pane pane) {
        super(
                (switch (style) {
                    case THICK -> Textures.Ui.MENU_WINDOW;
                    case THIN -> Textures.Ui.WINDOW;
                })
        );
        tabs = new Pane[] {pane};
        tabButtons = new Button[0];
    }

    @SafeVarargs
    public Window(Style style, Pair<Pane, Textures.Ui>... tabs) {
        super(
                (switch (style) {
                    case THICK -> Textures.Ui.MENU_WINDOW;
                    case THIN -> Textures.Ui.WINDOW;
                })
        );
        if (tabs.length == 0) throw new IllegalArgumentException();
        this.tabs = new Pane[tabs.length];
        tabButtons = new Button[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            this.tabs[i] = tabs[i].first;
            tabButtons[i] = new Button(tabs[i].second);
            int tabNumber = i;
            tabButtons[i].setOnUp(() -> activeTab = tabNumber);
        }
    }

    @Override
    public int getWidth() {
        return tabs[0].getWidth() + getBorderSize() * 2;
    }

    @Override
    public int getHeight() {
        return tabs[0].getHeight() + getBorderSize() * 2;
    }

    public int getContentWidth() {
        return tabs[0].getWidth();
    }

    public int getContentHeight() {
        return tabs[0].getHeight();
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(getTint());
        drawWindow(batch);
        tabs[activeTab].draw(batch);
        for (Button tabButton : tabButtons) {
            tabButton.draw(batch);
        }
    }

    public void open() {
        setVisible(true);
        UI.pushOnEscapeAction(this::close, this::isVisible);
        onOpen.execute();
    }

    public void close() {
        setVisible(false);
        onClose.execute();
    }

    public void openClose() {
        if (isVisible()) close();
        else open();
    }

    public void setTintCascading(Color tint) {
        setTint(tint);
        for (Pane tab : tabs) {
            tab.setTintCascading(tint);
        }
    }

    public void setOnOpen(Action onOpen) {
        this.onOpen = onOpen;
    }

    public void setOnClose(Action onClose) {
        this.onClose = onClose;
    }

    @Override
    public UIComponent onClick() {
        for (Button button : tabButtons) {
            if (button.isMouseOver())
                return button.onClick();
        }
        return tabs[activeTab].onClick();
    }

    @Override
    public void onHold() {
        for (Button button : tabButtons) {
            if (button.isMouseOver()) {
                button.onHold();
                return;
            }
        }
        tabs[activeTab].onHold();
    }

    @Override
    public void onUp() {
        for (Button button : tabButtons) {
            if (button.isMouseOver()) {
                button.onUp();
                return;
            }
        }
        tabs[activeTab].onUp();
    }

    @Override
    public boolean isMouseOver() {
        for (Button button : tabButtons) {
            if (button.isMouseOver())
                return button.isMouseOver();
        }
        return super.isMouseOver();
    }

    public int getBorderSize() {
        return texture.getRegionWidth()-1;
    }

    public void addUIComponent(UIComponent component) {
        tabs[0].addUIComponent(component);
    }

    public void addUIComponents(UIComponent... comps) {
        tabs[0].addUIComponents(comps);
    }

    public void pack() {
        for (Pane tab : tabs) {
            tab.setPosition(getX() + getBorderSize(), getY() + getBorderSize());
            tab.pack();

            int x = getX() + getBorderSize() + UI.PADDING;
            for (Button button : tabButtons) {
                button.setPosition(new Vector2i(x, getY() + getBorderSize() + getContentHeight() + 1));
                x += button.getWidth() + UI.PADDING;
            }
        }
    }

    public void clear() {
        for(Pane tab : tabs) {
            tab.clear();
        }
    }

    private void drawWindow(SpriteBatch batch) {
        int borderSize = getBorderSize();
        //bottom left
        drawCorner(batch, getX(), getY(), false, false);
        //top left
        drawCorner(batch, getX(), getY() + getContentHeight() + borderSize, false, true);
        //top right
        drawCorner(batch, getX() + getContentWidth() + borderSize, getY() + getContentHeight() + borderSize, true, true);
        //bottom right
        drawCorner(batch, getX() + getContentWidth() + borderSize, getY(), true, false);

        for (int x = 0; x < getContentWidth(); x++) {
            //top edge
            drawEdge(batch, getX() + borderSize + x, getY() + getContentHeight() + borderSize, false, true, false);
            //bottom edge
            drawEdge(batch, getX() + borderSize + x, getY(), false, false, false);
        }

        for (int y = 0; y < getContentHeight(); y++) {
            //left edge
            drawEdge(batch, getX(), getY() + borderSize + y, false, false, true);
            //right edge
            drawEdge(batch, getX() + getContentWidth() + borderSize, getY() + borderSize + y, true, false, true);
            //middle
            for (int x = 0; x < getContentWidth(); x++) {
                drawMiddle(batch, getX() + borderSize + x, getY() + borderSize + y);
            }
        }
    }

    private void drawCorner(SpriteBatch batch, int x, int y, boolean flipX, boolean flipY) {
        int width = texture.getRegionWidth(), height = texture.getRegionHeight();
        batch.draw(texture.getTexture(),
                x,
                y,
                0,
                0,
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
                y,
                0,
                0,
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
        batch.draw(texture.getTexture(),
                x,
                y,
                0,
                0,
                1,
                1,
                1,
                1,
                0,
                texture.getRegionX() + texture.getRegionWidth() - 1,
                texture.getRegionY(),
                1,
                1,
                false,
                false);
    }
}
