package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.utils.Vector2i;

import java.util.Map;

public class BuildingStatWindow extends StatWindow<Building> {
    private int counterWidth = 0;
    private String warning = "";
    private Button onOffButton;
    private Button left, right;
    private TextArea npcCount;
    private Button changeResourceButton;

    public BuildingStatWindow(UI.Layer layer) {
        super(layer);
        int y = Villager.TEXTURE_SIZE + UI.PADDING * 2;
        left = new Button(
                Textures.get(Textures.Ui.LEFT_ARROW),
                this,
                layer,
                new Vector2i(getEdgeWidth(), y)
        );
        right = new Button(
                Textures.get(Textures.Ui.RIGHT_ARROW),
                this,
                layer,
                new Vector2i(getEdgeWidth() + 32, y)
        );
        npcCount = new TextArea(
                "",
                this,
                layer,
                new Vector2i(getEdgeWidth() + 16, y - 6),
                16,
                TextArea.Align.CENTER
        );
        onOffButton = new Button(
                Textures.get(Textures.Ui.POWER_BUTTON),
                this,
                layer,
                new Vector2i(getEdgeWidth(), y += (16 + UI.PADDING))
        );
        changeResourceButton = new Button(
                Textures.get(Textures.Ui.SMALL_BUTTON),
                this,
                layer,
                new Vector2i(getEdgeWidth(), y + 32 + UI.PADDING)
        );

        left.setOnUp(() -> {
            if (pinnedObject instanceof ProductionBuilding building) {
                if (building.getEmployeeCapacity() > 1) {
                    building.setEmployeeCapacity(building.getEmployeeCapacity() - 1);
                    npcCount.setText(building.getEmployeeCapacity() + "");
                }
            }
            else {
                throw new IllegalStateException();
            }
        });
        right.setOnUp(() -> {
            if (pinnedObject instanceof ProductionBuilding building) {
                ProductionBuilding.Type type = building.getType();
                if (building.getEmployeeCapacity() < type.maxEmployeeCapacity) {
                    building.setEmployeeCapacity(building.getEmployeeCapacity() + 1);
                    npcCount.setText(building.getEmployeeCapacity() + "");
                }
            }
            else {
                throw new IllegalStateException();
            }
        });
        changeResourceButton.setOnUp(() -> UI.showFarmResourceMenu(((FarmBuilding<?>) pinnedObject)));
        onOffButton.setOnUp(() -> ((ProductionBuilding) pinnedObject).switchBuildingActivity());
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
        left.setVisible(false);
        right.setVisible(false);
        npcCount.setVisible(false);
        changeResourceButton.setVisible(false);
        onOffButton.setVisible(false);
        warning = "";

        if (Debug.isOpen()) {
            stats += "\nID: " + pinnedObject.getId();
        }

        if (pinnedObject instanceof ProductionBuilding building) {
            Job job = building.getJob();
            left.setVisible(true);
            right.setVisible(true);
            npcCount.setVisible(true);
            npcCount.setText(building.getEmployeeCapacity() + "");
            onOffButton.setVisible(true);

            if (!building.isActive()) warning = "building not active";
            else if (building.getInventory().checkStorageAvailability(job.getRecipe(building)) == Inventory.Availability.OUTPUT_FULL) warning = "inventory full\n";
            else if (building.getInventory().checkStorageAvailability(job.getRecipe(building)) == Inventory.Availability.LACKS_INPUT) warning = "not enough resources\n";

            if (building.getEfficiency() != 1) stats += "\nefficiency: " + String.format("%.2f", building.getEfficiency());
            stats += "\njob quality: " + building.getJobQuality();

            if (pinnedObject instanceof ServiceBuilding serviceBuilding) {
                stats += "\nguests: " + serviceBuilding.getGuests().size() + " / " + serviceBuilding.getType().guestCapacity;
            }
            else if (pinnedObject instanceof SchoolBuilding school) {
                stats += "\nstudents: " + school.getNumberOfStudents() + " / " + school.getOverallStudentCapacity();
            }
            else if (pinnedObject instanceof FarmBuilding) {
                changeResourceButton.setVisible(true);
                if (pinnedObject instanceof RanchBuilding)
                    changeResourceButton.setText("animal");
                else
                    changeResourceButton.setText("crop");
            }

            if (!(pinnedObject instanceof SchoolBuilding)) {
                stats += "\n" + building.getInventory().getDisplayedAmount() + " / " + building.getInventory().getMaxCapacity();
                for (Resource resource : job.getRecipe(building).changedResources()) {
                    stats = stats.concat("\n" + resource.toString().toLowerCase() + ": " +
                            building.getInventory().getResourceAmount(resource));
                }
            }
        }
        else if (pinnedObject instanceof ConstructionSite construction) {
            stats += "\n" + construction.getInventory().getDisplayedAmount() + " / " + construction.getInventory().getMaxCapacity();
            for (Map.Entry<Resource, Integer> entry : construction.getType().buildCost) {
                stats = stats.concat("\n" + entry.getKey().toString().toLowerCase() + ": " +
                        construction.getInventory().getResourceAmount(entry.getKey()) + " / " + entry.getValue());
            }
        }
        else if (pinnedObject.getClass() == Building.class) {
            Building building = pinnedObject;
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
            setContentWidth((int)(warning.length() * UI.FONT_WIDTH));
        }
        if(pinnedObject instanceof ProductionBuilding || pinnedObject instanceof ResidentialBuilding) {
            setContentHeight(getContentHeight() + Villager.TEXTURE_SIZE + UI.PADDING);
            if(getContentWidth() < counterWidth)
                setContentWidth(counterWidth);

            if (pinnedObject instanceof ProductionBuilding) {
                setContentHeight(getContentHeight() + UI.PADDING * 2 + 16 + 32);
                if (pinnedObject instanceof FarmBuilding) {
                    setContentHeight(getContentHeight() + UI.PADDING + 32);
                }
            }
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
            for (int j = 0; j < building.getEmployeeCapacity() - building.getShift(i).getEmployees().size(); j++) {
                batch.draw(Textures.get(Textures.Npc.IDLE0),
                        getGlobalPosition().x + UI.PADDING + x,
                        getGlobalPosition().y + UI.PADDING);

                x += Villager.TEXTURE_SIZE + 5;
            }
            batch.setColor(tint);

            if (i < 2)
                batch.draw(Textures.get(Textures.Ui.SHIFT_DIVIDER),
                        getGlobalPosition().x + UI.PADDING + x,
                        getGlobalPosition().y + UI.PADDING);
                x += 8;
        }
        counterWidth = x;
    }

    private void drawResidentCounter(SpriteBatch batch, ResidentialBuilding building) {
        batch.setColor(tint);
        int x = 0;
        for (Villager villager : building.getResidents()) {
            batch.draw(villager.getTexture(),
                    getGlobalPosition().x + UI.PADDING + x,
                    getGlobalPosition().y + UI.PADDING);
            x += Villager.TEXTURE_SIZE + 5;
        }
        counterWidth = x;
    }

    @Override
    public void addToUI() {
        super.addToUI();
        left.addToUI();
        right.addToUI();
        npcCount.addToUI();
        changeResourceButton.addToUI();
        onOffButton.addToUI();
    }
}
