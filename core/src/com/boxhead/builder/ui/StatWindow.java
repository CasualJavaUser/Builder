package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.GameScreen;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.GameObject;
import org.apache.commons.lang3.Range;

public abstract class StatWindow<T extends GameObject> extends DraggableWindow {
    protected T pinnedObject = null;
    protected boolean pinned;
    protected static final int verticalPadding = 6, leftPadding = 10, rightPadding = 25;
    protected String stats = "";

    public StatWindow() {
        super(Textures.get(Textures.Ui.WINDOW), false);
    }

    public void show(T gameObject) {
        this.pinnedObject = gameObject;
        pinned = true;
        updateStats();
        updateWindowSize();
        updatePosition();
        setVisible(true);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if(pinned) {
            updatePosition();
            if(!(getPinnedObjectBoundsX().contains(getObjectScreenPosition().x) &&
                    getPinnedObjectBoundsY().contains(getObjectScreenPosition().y)))
                setVisible(false);
        }
        updateStats();
        updateWindowSize();
        super.draw(batch);
    }

    protected abstract void updateStats();

    protected void updateWindowSize() {
        setHeight(10);
        for(char c : stats.toCharArray()) {
            if (c == '\n') setHeight(getContentHeight() + UI.FONT_SIZE +3);
        }

        setWidth((int)(getLongestLineLength(stats) * 6.5f + leftPadding + rightPadding));
    }

    protected void updatePosition() {
        Vector3 objectPosition = getObjectScreenPosition();

        float cameraZoom = GameScreen.camera.zoom;
        int x = (int) (objectPosition.x + pinnedObject.getTexture().getRegionWidth() / cameraZoom);
        int y = (int) (objectPosition.y + (pinnedObject.getTexture().getRegionHeight()) / cameraZoom);
        x = getStatWindowXRange().fit(x);
        y = getStatWindowYRange().fit(y);

        setGlobalPosition(x, y);
    }

    protected Vector3 getObjectScreenPosition() {
        return GameScreen.camera.project(new Vector3(
                pinnedObject.getGridPosition().x * World.TILE_SIZE,
                pinnedObject.getGridPosition().y * World.TILE_SIZE, 0));
    }

    @Override
    public void onHold() {
        pinned = false;
        super.onHold();
    }

    protected int getLongestLineLength(String text) {
        int longest = 0;
        for(String line : text.split("\n")) {
            if (line.length() > longest) longest = line.length();
        }
        return longest;
    }

    private Range<Integer> getStatWindowXRange() {
        return Range.between(0, Gdx.graphics.getWidth() - getWindowWidth());
    }

    private Range<Integer> getStatWindowYRange() {
        return Range.between(getWindowWidth(), Gdx.graphics.getHeight());
    }

    private Range<Float> getPinnedObjectBoundsX() {
        return Range.between(-pinnedObject.getTexture().getRegionWidth() / GameScreen.camera.zoom, (float)Gdx.graphics.getWidth());
    }

    private Range<Float> getPinnedObjectBoundsY() {
        return Range.between(-pinnedObject.getTexture().getRegionHeight() / GameScreen.camera.zoom, (float)Gdx.graphics.getHeight());
    }
}
