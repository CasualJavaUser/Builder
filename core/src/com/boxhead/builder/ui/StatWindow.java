package com.boxhead.builder.ui;

import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.GameObject;

public abstract class StatWindow<T extends GameObject> extends Window {
    protected T pinnedObject = null;
    protected boolean pinned;

    public StatWindow() {
        super(Textures.get(Textures.Ui.STAT_WINDOW));
    }

    public void show(T gameObject) {
        this.pinnedObject = gameObject;
        pinned = true;
        updateStats();
        updatePosition();
        setVisible(true);
    }

    protected abstract void updateStats();

    protected void updatePosition() {
        Vector3 objectPosition = getObjectScreenPosition();

        float cameraZoom = BuilderGame.getGameScreen().getCamera().zoom;
        int x = (int) (objectPosition.x + pinnedObject.getTexture().getRegionWidth() / cameraZoom);
        int y = (int) (objectPosition.y + (pinnedObject.getTexture().getRegionHeight() - 5) / cameraZoom);

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
}
