package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.GameObject;

public abstract class StatWindow<T extends GameObject> extends Window {
    protected T pinnedObject = null;
    protected boolean pinned;
    protected static final int topPadding = 12, leftPadding = 10, rightPadding = 20;
    protected String stats = "";

    public StatWindow() {
        super(Textures.get(Textures.Ui.WINDOW));
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
        }
        super.draw(batch);
        updateStats();
        updateWindowSize();
    }

    protected abstract void updateStats();

    protected void updateWindowSize() {
        sizeY = 30;
        for(char c : stats.toCharArray()) {
            if (c == '\n') sizeY += UI.FONT_SIZE+3;
        }

        sizeX = (int)(getLongestLineLength(stats) * 6.5f + leftPadding + rightPadding);
    }

    protected void updatePosition() {
        Vector3 objectPosition = getObjectScreenPosition();

        float cameraZoom = BuilderGame.getGameScreen().getCamera().zoom;
        int x = (int) (objectPosition.x + pinnedObject.getTexture().getRegionWidth() / cameraZoom);
        int y = (int) (objectPosition.y + (pinnedObject.getTexture().getRegionHeight()) / cameraZoom) + sizeY;

        position.set(x, y);
    }

    protected Vector3 getObjectScreenPosition() {
        return BuilderGame.getGameScreen().getCamera().project(new Vector3(
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
}
