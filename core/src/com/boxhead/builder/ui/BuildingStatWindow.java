package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;

public class BuildingStatWindow extends Window{
    private Building building;
    private Jobs job;
    private Services service;
    private NPC[] npcsInside;
    private Resources resource;
    private int guestsInside, guestCapacity;
    String stats = "";
    private boolean pinned;

    public BuildingStatWindow() {
        super(Textures.getUI("stat_window"));
    }

    public void show(Building building) {
        this.building = building;
        pinned = true;
        updateStats();
        updatePosition();
        setVisible(true);
    }

    @Override
    public void onHold() {
        pinned = false;
        super.onHold();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if(pinned) {
            updatePosition();
        }
        super.draw(batch);
        updateStats();
        UI.FONT.draw(batch, stats, position.x + 10, position.y + 105);
    }

    private void updateStats() {
        stats = building.getName();
        if(building instanceof ProductionBuilding) {
            stats += "\njob quality: " + ((ProductionBuilding) building).getJobQuality();
            if(((ProductionBuilding) building).getJob().getProduct() != Resources.NOTHING) {
                stats += "\nproduct: " + ((ProductionBuilding) building).getJob().getProduct().toString().toLowerCase();
            }
        }
    }

    private void updatePosition() {
        Vector3 buildingPosition = GameScreen.getCamera().project(new Vector3(building.getPosition().x * World.TILE_SIZE, building.getPosition().y * World.TILE_SIZE, 0));
        position.set((int)(buildingPosition.x + 20/GameScreen.getCamera().zoom), (int)(buildingPosition.y + 10/GameScreen.getCamera().zoom));
    }
}
