package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;

public class BuildingStatWindow extends Window{
    private Building building;
    private Jobs job;
    private Services service;
    private NPC[] npcsInside;
    private int guestsInside, guestCapacity;
    private boolean pinned;
    String stats = "";
    String warning = "";

    private final static int rightPadding = 10;

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
        UI.FONT.setColor(Color.RED);
        UI.FONT.draw(batch, warning, position.x + rightPadding, position.y + 180);
        UI.FONT.setColor(Color.WHITE);
        UI.FONT.draw(batch, stats, position.x + rightPadding, position.y + 155);
    }

    private void updateStats() {
        stats = building.getName();
        if(building instanceof ProductionBuilding) {
            switch (((ProductionBuilding) building).getAvailability()) {
                case -1: warning = "not enough resources"; break;
                case 1: warning = "storage full"; break;
                case 2: warning = "no storage in range"; break;
                default: warning = "";
            }

            stats += "\njob quality: " + ((ProductionBuilding) building).getJobQuality();
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
            for (int i = 1; i < Resources.values().length; i++) {
                stats += "\n" + Resources.values()[i].toString().toLowerCase() + ": " +
                        ((StorageBuilding) building).getStored(Resources.values()[i]) + " / " +
                        ((StorageBuilding) building).getMaxStorage(Resources.values()[i]);
            }
        }
    }

    private void updatePosition() {
        Vector3 buildingPosition = BuilderGame.getGameScreen().getCamera().project(new Vector3(building.getPosition().x * World.TILE_SIZE, building.getPosition().y * World.TILE_SIZE, 0));
        position.set((int)(buildingPosition.x + 40/BuilderGame.getGameScreen().getCamera().zoom), (int)(buildingPosition.y + 10/BuilderGame.getGameScreen().getCamera().zoom));
    }
}
