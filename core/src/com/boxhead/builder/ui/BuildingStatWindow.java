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
    String warning = "";

    public BuildingStatWindow() {
        super();
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        UI.FONT.setColor(Color.RED);
        UI.FONT.draw(batch, warning, position.x + leftPadding, position.y - topPadding);
        UI.FONT.setColor(Color.WHITE);
        UI.FONT.draw(batch, stats, position.x + leftPadding, position.y - topPadding - (warning.equals("") ? 0 : 20));
    }

    @Override
    protected void updateStats() {
        stats = pinnedObject.getName();
        if(pinnedObject instanceof ProductionBuilding) {
            switch (((ProductionBuilding) pinnedObject).getAvailability()) {
                case -1: warning = "not enough resources\n"; break;
                case 1: warning = "storage full\n"; break;
                case 2: warning = "no storage in range\n"; break;
                default: warning = "";
            }

            stats += "\njob quality: " + ((ProductionBuilding) pinnedObject).getJobQuality();
            job = ((ProductionBuilding) pinnedObject).getJob();
            if(job.producesAnyResources()) {
                stats += "\nproduct(s):";
                for (Resource resource : job.getResourceChanges().keySet()) {
                    if(job.getResourceChanges().get(resource) > 0) {
                        stats += "\n- " + resource.name().toLowerCase();
                    }
                }
            }
        }

        if(pinnedObject instanceof StorageBuilding) {
            StorageBuilding storage = ((StorageBuilding) pinnedObject);
            stats += "\n" + storage.getStoredWeight();
            for (int i = 1; i < Resource.values().length; i++) {
                if(storage.getStored(Resource.values()[i]) != 0) {
                    stats += "\n" + Resource.values()[i].toString().toLowerCase() + ": " +
                            storage.getStored(Resource.values()[i]);
                }
            }
        }

    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        if(!warning.equals("")) {
            sizeY += 20;
            sizeX = warning.length() * 7 + leftPadding * 2;
        }
    }
}
