package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;
import com.boxhead.builder.game_objects.StorageBuilding;

public class BuildingStatWindow extends StatWindow<Building> {
    private Job job;
    private Service service;
    private NPC[] npcsInside;
    private int guestsInside, guestCapacity;
    String stats = "";
    String warning = "";

    private final static int rightPadding = 10;

    public BuildingStatWindow() {
        super();
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

    @Override
    protected void updateStats() {
        stats = pinnedObject.getName();
        if(pinnedObject instanceof ProductionBuilding) {
            switch (((ProductionBuilding) pinnedObject).getAvailability()) {
                case -1: warning = "not enough resources"; break;
                case 1: warning = "storage full"; break;
                case 2: warning = "no storage in range"; break;
                default: warning = "";
            }

            stats += "\njob quality: " + ((ProductionBuilding) pinnedObject).getJobQuality();
            job = ((ProductionBuilding) pinnedObject).getJob();
            if(job.producesAnyResources()) {
                stats += "\nproduct(s):";
                for (int i = 0; i < job.getResources().length; i++) {
                    if(job.getChanges()[i] > 0) {
                        stats += "\n- " + job.getResources()[i].toString().toLowerCase();
                    }
                }
            }
        }
        if(pinnedObject instanceof StorageBuilding) {
            for (int i = 1; i < Resource.values().length; i++) {
                stats += "\n" + Resource.values()[i].toString().toLowerCase() + ": " +
                        ((StorageBuilding) pinnedObject).getStored(Resource.values()[i]) + " / " +
                        ((StorageBuilding) pinnedObject).getMaxStorage(Resource.values()[i]);
            }
        }
    }
}
