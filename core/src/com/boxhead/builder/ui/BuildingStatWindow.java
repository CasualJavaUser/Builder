package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.*;

import java.util.HashSet;
import java.util.Set;

public class BuildingStatWindow extends StatWindow<Building> {
    private Job job;
    private Service service;
    private int npcCapacity;
    protected Set<NPC> npcs = new HashSet<>();
    private int guestsInside, guestCapacity;
    String warning = "";

    public BuildingStatWindow() {
        super();
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        UI.FONT.setColor(Color.RED);
        UI.FONT.draw(batch, warning, getGlobalPosition().x + leftPadding, getGlobalPosition().y + getContentHeight());
        UI.FONT.setColor(Color.WHITE);
        UI.FONT.draw(batch, stats, getGlobalPosition().x + leftPadding, getGlobalPosition().y + getContentHeight() - (warning.equals("") ? 0 : 20));

        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
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
            job = ((ProductionBuilding) pinnedObject).getJob();

            if(pinnedObject.getInventory().checkStorageAvailability(job.getRecipe()) == Inventory.Availability.OUTPUT_FULL) warning = "inventory full\n";
            else if(pinnedObject.getInventory().checkStorageAvailability(job.getRecipe()) == Inventory.Availability.LACKS_INPUT) warning = "not enough resources\n";
            else warning = "";

            stats += "job quality: " + ((ProductionBuilding) pinnedObject).getJobQuality();
            stats += "\n" + building.getInventory().getDisplayedAmount() + " / " + building.getInventory().getMaxCapacity();
            for (Resource resource : job.getRecipe().changedResources()) {
                stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                        building.getInventory().getResourceAmount(resource));
            }
        }

        if (pinnedObject instanceof StorageBuilding) {
            stats += "\n" + pinnedObject.getInventory().getDisplayedAmount() + " / " + pinnedObject.getInventory().getMaxCapacity();
            for (Resource resource : pinnedObject.getInventory().getStoredResources()) {
                if (resource != Resource.NOTHING) {
                    stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                            pinnedObject.getInventory().getResourceAmount(resource));
                }
            }
        }

        if (pinnedObject instanceof ConstructionSite) {
            stats += '(' + ((ConstructionSite) pinnedObject).getBuildingType().name + ')';
        }
    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        if(!warning.equals("")) {
            setHeight(getContentHeight() + 20);
            setWidth(warning.length() * 7 + leftPadding + rightPadding);
        }
        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
            setHeight(getContentHeight() + Textures.get(Textures.Npc.FUNGUY).getRegionHeight());
            int counterWidth = leftPadding + (Textures.get(Textures.Npc.FUNGUY).getRegionWidth() + leftPadding) * npcCapacity;
            if(getContentHeight() < counterWidth)
                setWidth(counterWidth);
        }
    }

    @Override
    public void show(Building gameObject) {
        if (pinnedObject instanceof ProductionBuilding) ((ProductionBuilding)pinnedObject).hideRangeVisualiser();
        super.show(gameObject);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(!visible && pinnedObject instanceof ProductionBuilding) ((ProductionBuilding)pinnedObject).hideRangeVisualiser();
    }

    private void drawNPCCounter(SpriteBatch batch) {
        int i = 0;
        for (NPC npc : npcs) {
            if (npc.getCurrentBuilding() == pinnedObject)
                batch.draw(npc.getTexture(),
                        position.x + leftPadding + (npc.getTexture().getRegionWidth() + leftPadding) * i++,
                        position.y + Textures.get(Textures.Npc.FUNGUY).getRegionHeight() - verticalPadding);
        }
        batch.setColor(Color.BLACK);
        while (i < npcCapacity) {
            batch.draw(Textures.get(Textures.Npc.FUNGUY),
                    position.x + leftPadding + (Textures.get(Textures.Npc.FUNGUY).getRegionWidth() + leftPadding) * i++,
                    position.y + Textures.get(Textures.Npc.FUNGUY).getRegionHeight() - verticalPadding);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
