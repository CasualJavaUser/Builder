package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.*;

import java.util.Set;

public class BuildingStatWindow extends StatWindow<Building> {
    private Job job;
    private Service service;
    private int npcCapacity;
    protected Set<NPC> npcs;
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

        if(pinnedObject instanceof EnterableBuilding) {
            drawNPCCounter(batch);
        }
    }

    @Override
    protected void updateStats() {
        stats = pinnedObject.getName() + "\n";
        if(pinnedObject instanceof ResidentialBuilding) {
            npcCapacity = ((ResidentialBuilding) pinnedObject).getResidentCapacity();
            npcs = ((ResidentialBuilding) pinnedObject).getResidents();
        }

        if(pinnedObject instanceof ProductionBuilding) {
            ProductionBuilding building = (ProductionBuilding) pinnedObject;
            npcCapacity = building.getEmployeeCapacity();
            npcs = building.getEmployees();

            switch (((ProductionBuilding) pinnedObject).getAvailability()) {  //warning
                case -1: warning = "not enough resources\n"; break;
                case 1: warning = "storage full\n"; break;
                case 2: warning = "no storage in range\n"; break;
                default: warning = "";
            }

            stats += "job quality: " + ((ProductionBuilding) pinnedObject).getJobQuality();  //job quality and products
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
            StorageBuilding storage = (StorageBuilding) pinnedObject;
            stats += storage.getStoredWeight() + " / " + storage.getMaxWeight();
            for (int i = 1; i < Resource.values().length; i++) {
                if(storage.getStored(Resource.values()[i]) != 0) {
                    stats += "\n" + Resource.values()[i].toString().toLowerCase() + ": " +
                            storage.getStored(Resource.values()[i]);
                }
            }
        }

        if(pinnedObject instanceof ConstructionSite) {
            stats += '(' + ((ConstructionSite) pinnedObject).getBuilding().getName() + ')';
        }
    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        if(!warning.equals("")) {
            sizeY += 20;
            sizeX = warning.length() * 7 + leftPadding + rightPadding;
        }
        if(pinnedObject instanceof EnterableBuilding) {
            sizeY += Textures.get(Textures.Npc.FUNGUY).getRegionHeight();
            int counterWidth = leftPadding + (Textures.get(Textures.Npc.FUNGUY).getRegionWidth() + leftPadding) * npcCapacity;
            if(sizeX < counterWidth)
                sizeX = counterWidth;
        }
    }

    private void drawNPCCounter(SpriteBatch batch) {
        int i = 0;
        for (NPC npc : npcs) {
            if (npc.getCurrentBuilding() == pinnedObject)
                batch.draw(npc.getTexture(),
                        position.x + leftPadding + (npc.getTexture().getRegionWidth() + leftPadding) * i++,
                        position.y - sizeY);
        }
        batch.setColor(Color.BLACK);
        while (i < npcCapacity) {
            batch.draw(Textures.get(Textures.Npc.FUNGUY),
                    position.x + leftPadding + (Textures.get(Textures.Npc.FUNGUY).getRegionWidth() + leftPadding) * i++,
                    position.y - sizeY);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
