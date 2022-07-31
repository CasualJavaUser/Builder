package com.boxhead.builder.ui;

import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.GameObject;

public abstract class StatWindow<T extends GameObject> extends Window {
    protected T pinnedObject = null;
    protected boolean pinned;

    public StatWindow() {
        super(Textures.getUI("stat_window"));
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
        position.set((int)(objectPosition.x + pinnedObject.getTexture().getRegionWidth()/BuilderGame.getGameScreen().getCamera().zoom),
                (int)(objectPosition.y + (pinnedObject.getTexture().getRegionHeight()-5)/BuilderGame.getGameScreen().getCamera().zoom));
    }

    protected Vector3 getObjectScreenPosition() {
        return BuilderGame.getGameScreen().getCamera().project(new Vector3(pinnedObject.getGridPosition().x * World.TILE_SIZE,
                                                                           pinnedObject.getGridPosition().y * World.TILE_SIZE, 0));
    }

    @Override
    public void onHold() {
        pinned = false;
        super.onHold();
    }
}
