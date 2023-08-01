package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.*;

public class BuildingStatWindow extends StatWindow<Building> {
    private int counterWidth = 0;
    String warning = "";

    public BuildingStatWindow(UI.Layer layer) {
        super(layer);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        UI.FONT.setColor(Color.RED);
        UI.FONT.draw(batch, warning, getGlobalPosition().x + UI.PADDING, getGlobalPosition().y + getWindowHeight() - UI.PADDING);
        UI.FONT.setColor(Color.WHITE);
        UI.FONT.draw(batch, stats, getGlobalPosition().x + UI.PADDING, getGlobalPosition().y + getWindowHeight() - UI.PADDING - (warning.equals("") ? 0 : UI.FONT.getXHeight() + UI.PADDING));

        if(pinnedObject instanceof ProductionBuilding)
            drawEmployeeCounter(batch, ((ProductionBuilding) pinnedObject));

        if (pinnedObject instanceof ResidentialBuilding)
            drawResidentCounter(batch, ((ResidentialBuilding) pinnedObject));
    }

    @Override
    protected void updateStats() {
        stats = pinnedObject.getName();

        if (Debug.isOpen()) {
            stats += "\nID: " + pinnedObject.getId();
        }

        if (pinnedObject instanceof ResidentialBuilding) {
            warning = "";
        }
        else if (pinnedObject instanceof ProductionBuilding building) {
            Job job = building.getJob();

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
            setContentWidth(warning.length() * 7 + UI.PADDING + UI.PADDING);
        }
        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
            setContentHeight(getContentHeight() + Villager.TEXTURE_SIZE + UI.PADDING);
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

    private void drawEmployeeCounter(SpriteBatch batch, ProductionBuilding building) {
        int x = 0;
        for (int i = 0; i < 3; i++) {
            for (Villager employee : building.getShift(i).getEmployees()) {
                if (!building.getType().getShiftActivity(i))
                    continue;

                batch.draw(employee.getTexture(),
                        getGlobalPosition().x + UI.PADDING + x,
                        getGlobalPosition().y + UI.PADDING);

                x += Villager.TEXTURE_SIZE + 5;
            }

            batch.setColor(Color.BLACK);
            for (int j = 0; j < building.getShift(i).getMaxEmployees() - building.getShift(i).getEmployees().size(); j++) {
                batch.draw(Textures.get(Textures.Npc.IDLE0),
                        getGlobalPosition().x + UI.PADDING + x,
                        getGlobalPosition().y + UI.PADDING);

                x += Villager.TEXTURE_SIZE + 5;
            }
            batch.setColor(UI.DEFAULT_COLOR);

            if (i < 2)
                batch.draw(Textures.get(Textures.Ui.SHIFT_DIVIDER),
                        getGlobalPosition().x + UI.PADDING + x,
                        getGlobalPosition().y + UI.PADDING);
                x += 8;
        }
        counterWidth = x;
    }

    private void drawResidentCounter(SpriteBatch batch, ResidentialBuilding building) {
        int x = 0;
        for (Villager villager : building.getResidents()) {
            batch.draw(villager.getTexture(),
                    getGlobalPosition().x + UI.PADDING + x,
                    getGlobalPosition().y + UI.PADDING);
            x += Villager.TEXTURE_SIZE + 5;
        }
        batch.setColor(Color.BLACK);
        for (int i = 0; i < building.getResidentCapacity() - building.getResidents().size(); i++) {
            batch.draw(Textures.get(Textures.Npc.IDLE0),
                    getGlobalPosition().x + UI.PADDING + x,
                    getGlobalPosition().y + UI.PADDING);
            x += Villager.TEXTURE_SIZE + 5;
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
