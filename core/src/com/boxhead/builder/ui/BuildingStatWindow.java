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
        UI.FONT.draw(batch, stats, position.x + 10, position.y + 170);
    }

    private void updateStats() {
        stats = building.getName();
        if(building instanceof ProductionBuilding) {
            stats += "\njob quality: " + ((ProductionBuilding) building).getJobQuality();
            //Resources[] resources = ((ProductionBuilding) building).getJob().getResources();
            job = ((ProductionBuilding) building).getJob();
            if(job.getResources()[0] != Resources.NOTHING) {
                stats += "\nproduct(s):";
                for (int i = 0; i < job.getResources().length; i++) {
                    if(job.getChange()[i] > 0) {
                        stats += "\n- " + job.getResources()[i].toString().toLowerCase();
                    }
                }
            }
        }
        if(building instanceof StorageBuilding) {
            for (Resources resource : Resources.values()) {
                stats += "\n" + resource.toString().toLowerCase() + ": " +
                        ((StorageBuilding) building).getStored(resource) + " / " +
                        ((StorageBuilding) building).getMaxStorage(resource);
            }
        }
    }

    private void updatePosition() {
        Vector3 buildingPosition = GameScreen.getCamera().project(new Vector3(building.getPosition().x * World.TILE_SIZE, building.getPosition().y * World.TILE_SIZE, 0));
        position.set((int)(buildingPosition.x + 40/GameScreen.getCamera().zoom), (int)(buildingPosition.y + 10/GameScreen.getCamera().zoom));
    }
}
