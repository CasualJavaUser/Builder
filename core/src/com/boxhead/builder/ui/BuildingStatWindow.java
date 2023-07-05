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
    protected Set<Villager> villagers = new HashSet<>();
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

        if (Debug.isOpen()) {
            stats += "\nID: " + pinnedObject.getId();
        }

        if (pinnedObject instanceof ResidentialBuilding building) {
            warning = "";
            npcCapacity = building.getResidentCapacity();
            villagers = building.getResidents();
        }
        else if (pinnedObject instanceof ProductionBuilding building) {
            npcCapacity = building.getType().workerCapacity;
            villagers = building.getEmployees();
            job = building.getJob();

            if (building.getInventory().checkStorageAvailability(job.getRecipe(building)) == Inventory.Availability.OUTPUT_FULL) warning = "inventory full\n";
            else if (building.getInventory().checkStorageAvailability(job.getRecipe(building)) == Inventory.Availability.LACKS_INPUT) warning = "not enough resources\n";
            else warning = "";

            if (building.getEfficiency() != 1) stats += "\nefficiency: " + String.format("%.2f", building.getEfficiency());
            stats += "\njob quality: " + building.getJobQuality();

            if (pinnedObject instanceof ServiceBuilding serviceBuilding) {
                stats += "\nguests: " + serviceBuilding.getGuests().size() + " / " + serviceBuilding.getType().guestCapacity;
            }

            stats += "\n" + building.getInventory().getDisplayedAmount() + " / " + building.getInventory().getMaxCapacity();
            for (Resource resource : job.getRecipe(building).changedResources()) {
                stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                        building.getInventory().getResourceAmount(resource));
            }
        }
        else if (pinnedObject instanceof StorageBuilding building) {
            warning = "";
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
            setContentHeight(getContentHeight() + Villager.TEXTURE_SIZE + verticalPadding);
            int counterWidth = leftPadding + (Villager.TEXTURE_SIZE + leftPadding) * npcCapacity;
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
        for (Villager villager : villagers) {
            if (villager.getCurrentBuilding() == pinnedObject)
                batch.draw(villager.getTexture(),
                        getGlobalPosition().x + leftPadding + (Villager.TEXTURE_SIZE + leftPadding) * i++,
                        getGlobalPosition().y + verticalPadding);
        }
        batch.setColor(Color.BLACK);
        while (i < npcCapacity) {
            batch.draw(Textures.get(Textures.Npc.IDLE0),
                    getGlobalPosition().x + leftPadding + (Villager.TEXTURE_SIZE + leftPadding) * i++,
                    getGlobalPosition().y + verticalPadding);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
