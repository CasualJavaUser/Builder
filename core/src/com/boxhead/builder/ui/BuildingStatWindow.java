package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
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

    public BuildingStatWindow(UI.Layer layer) {
        super(layer);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        UI.FONT.setColor(Color.RED);
        UI.FONT.draw(batch, warning, getGlobalPosition().x + leftPadding, getGlobalPosition().y + getWindowHeight() - verticalPadding);
        UI.FONT.setColor(Color.WHITE);
        UI.FONT.draw(batch, stats, getGlobalPosition().x + leftPadding, getGlobalPosition().y + getWindowHeight() - verticalPadding - (warning.equals("") ? 0 : UI.FONT.getXHeight() + verticalPadding));

        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
            drawNPCCounter(batch);
        }
    }

    @Override
    protected void updateStats() {
        stats = pinnedObject.getName();
        if(pinnedObject instanceof ResidentialBuilding building) {
            npcCapacity = building.getResidentCapacity();
            npcs = building.getResidents();
        }

        if(pinnedObject instanceof ProductionBuilding building) {
            npcCapacity = building.getType().npcCapacity;
            npcs = building.getEmployees();
            job = building.getJob();

            if(building.getInventory().checkStorageAvailability(job.getRecipe()) == Inventory.Availability.OUTPUT_FULL) warning = "inventory full\n";
            else if(building.getInventory().checkStorageAvailability(job.getRecipe()) == Inventory.Availability.LACKS_INPUT) warning = "not enough resources\n";
            else warning = "";

            stats += "\njob quality: " + building.getJobQuality();
            stats += "\n" + building.getInventory().getDisplayedAmount() + " / " + building.getInventory().getMaxCapacity();
            for (Resource resource : job.getRecipe().changedResources()) {
                stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                        building.getInventory().getResourceAmount(resource));
            }
        }

        if (pinnedObject instanceof StorageBuilding building) {
            stats += "\n" + building.getInventory().getDisplayedAmount() + " / " + building.getInventory().getMaxCapacity();
            for (Resource resource : building.getInventory().getStoredResources()) {
                if (resource != Resource.NOTHING) {
                    stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                            building.getInventory().getResourceAmount(resource));
                }
            }
        }
    }

    @Override
    protected void updatePosition() {
        Vector3 objectPosition = getObjectScreenPosition();

        float cameraZoom = GameScreen.camera.zoom;
        int x = (int) (objectPosition.x + pinnedObject.getCollider().getWidth() * World.TILE_SIZE / cameraZoom);
        int y = (int) (objectPosition.y + (pinnedObject.getCollider().getHeight()) * World.TILE_SIZE / cameraZoom);
        x = getStatWindowXRange().fit(x);
        y = getStatWindowYRange().fit(y);

        setGlobalPosition(x, y);
    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        if(!warning.equals("")) {
            setContentHeight(getContentHeight() + 20);
            setContentWidth(warning.length() * 7 + leftPadding + rightPadding);
        }
        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
            int npcSize = Textures.get(Textures.Npc.FUNGUY).getRegionHeight();
            setContentHeight(getContentHeight() + npcSize + verticalPadding);
            int counterWidth = leftPadding + (npcSize + leftPadding) * npcCapacity;
            if(getContentWidth() < counterWidth)
                setContentWidth(counterWidth);
        }
    }

    @Override
    public void pin(Building gameObject) {
        if(pinnedObject instanceof ProductionBuilding building) building.showRangeVisualiser(false);
        super.pin(gameObject);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(pinnedObject instanceof ProductionBuilding building) building.showRangeVisualiser(visible);
    }

    private void drawNPCCounter(SpriteBatch batch) {
        int i = 0;
        int npcSize = Textures.get(Textures.Npc.FUNGUY).getRegionHeight();
        for (NPC npc : npcs) {
            if (npc.getCurrentBuilding() == pinnedObject)
                batch.draw(npc.getTexture(),
                        getGlobalPosition().x + leftPadding + (npcSize + leftPadding) * i++,
                        getGlobalPosition().y + verticalPadding);
        }
        batch.setColor(Color.BLACK);
        while (i < npcCapacity) {
            batch.draw(Textures.get(Textures.Npc.FUNGUY),
                    getGlobalPosition().x + leftPadding + (npcSize + leftPadding) * i++,
                    getGlobalPosition().y + verticalPadding);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
