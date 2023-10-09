package com.boxhead.builder.game_objects.buildings;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Circle;
import com.boxhead.builder.utils.Range;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static boolean isInDemolishingMode = false;
    private static Building.Type currentBuilding;
    private static Range<Integer> rangeX, rangeY;

    public static Building create(Building.Type type, Vector2i gridPosition) {
        if (type instanceof RanchBuilding.Type t) return new RanchBuilding(t, gridPosition);
        if (type instanceof PlantationBuilding.Type t) return new PlantationBuilding(t, gridPosition);
        if (type instanceof SchoolBuilding.Type t) return new SchoolBuilding(t, gridPosition);
        if (type instanceof ServiceBuilding.Type t) return new ServiceBuilding(t, gridPosition);
        if (type instanceof WaterBuilding.Type t) return new WaterBuilding(t, gridPosition);
        if (type instanceof ProductionBuilding.Type t) return new ProductionBuilding(t, gridPosition);
        if (type instanceof ResidentialBuilding.Type t) return new ResidentialBuilding(t, gridPosition);
        return new Building(type, gridPosition);
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode || isInDemolishingMode)
            throw new IllegalStateException("Not in building mode");

        if (Tiles.isInFieldMode()) {
            handleFieldMode(batch);
            return;
        }

        TextureRegion texture = currentBuilding.getTexture();
        Vector2 mousePos = GameScreen.getMouseWorldPosition();

        //center texture around mouse
        int screenX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int screenY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        //snap texture to grid within map boundaries
        screenX = rangeX.fit(screenX - (screenX % World.TILE_SIZE));
        screenY = rangeY.fit(screenY - (screenY % World.TILE_SIZE));

        Vector2i gridPosition = new Vector2i(screenX, screenY).divide(World.TILE_SIZE);

        if (currentBuilding instanceof ProductionBuilding.Type productionBuilding && productionBuilding.range > 0) {
            showBuildingRange(batch,
                    gridPosition.plus(currentBuilding.entrancePosition),
                    productionBuilding.range);
        }
        boolean isBuildable = checkAndShowTileAvailability(batch, gridPosition);

        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, screenX, screenY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE) && isBuildable) {
            if (currentBuilding instanceof FarmBuilding.Type) {
                Tiles.toFieldMode(
                        currentBuilding.relativeCollider.cloneAndTranslate(gridPosition),
                        currentBuilding instanceof RanchBuilding.Type,
                        Range.between(FarmBuilding.MIN_FIELD_SIZE, FarmBuilding.MAX_FIELD_SIZE)
                );
            } else {
                ConstructionSite constructionSite = new ConstructionSite(currentBuilding, gridPosition, 1000);
                Harvestable onEntrance = World.findHarvestables(constructionSite.getEntrancePosition());
                if (onEntrance != null) World.removeFieldWorks(onEntrance);
                World.removeFieldWorks(constructionSite.getCollider());
                World.placeFieldWork(constructionSite);
                World.makeBuilt(constructionSite.getCollider());

                if (!InputManager.isKeyDown(InputManager.CONTROL))
                    turnOffBuildingMode();
            }
        }
    }

    private static void handleFieldMode(SpriteBatch batch) {
        TextureRegion texture = currentBuilding.getTexture();

        batch.setColor(UI.SEMI_TRANSPARENT);
        Tiles.drawTile(batch, texture, Tiles.getBuildingCollider().getGridPosition());
        BoxCollider fieldCollider = Tiles.handleFieldMode(batch);
        if (fieldCollider == null) return;  //field hasn't been placed yet

        ConstructionSite constructionSite = new ConstructionSite(
                currentBuilding,
                Tiles.getBuildingCollider().getGridPosition().clone(),
                100,
                fieldCollider);
        Harvestable onEntrance = World.findHarvestables(constructionSite.getEntrancePosition());
        if (onEntrance != null) World.removeFieldWorks(onEntrance);
        World.removeFieldWorks(constructionSite.getCollider());
        World.removeFieldWorks(fieldCollider);
        World.placeFieldWork(constructionSite);
        World.makeBuilt(constructionSite.getCollider());
        World.makeBuilt(fieldCollider);

        Tiles.turnOffFieldMode();
        if (!InputManager.isKeyDown(InputManager.CONTROL))
            turnOffBuildingMode();
    }

    public static void handleDemolishingMode() {
        if (!isInDemolishingMode || isInBuildingMode)
            throw new IllegalStateException("Not in demolishing mode");

        if (!InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) return;

        for (Building building : World.getBuildings()) {
            if (building.isMouseOver()) {
                if (building instanceof BuildSite)
                    return;

                World.placeFieldWork(new DemolitionSite(building, 100));
                if (!InputManager.isKeyDown(Input.Keys.CONTROL_LEFT)) isInDemolishingMode = false;
                break;
            }
        }
    }

    public static void toBuildingMode(Building.Type building) {
        currentBuilding = building;
        rangeX = Range.between(0, World.getWidth() - currentBuilding.getTexture().getRegionWidth());
        rangeY = Range.between(0, World.getHeight() - currentBuilding.getTexture().getRegionHeight());
        isInDemolishingMode = false;
        isInBuildingMode = true;
        UI.setTip("Press CTRL to place multiple");
    }

    public static void toDemolishingMode() {
        isInDemolishingMode = true;
        isInBuildingMode = false;
    }

    public static void turnOffBuildingMode() {
        isInBuildingMode = false;
        Tiles.turnOffFieldMode();
        UI.setTip("");
    }

    public static void turnOffDemolishingMode() {
        isInDemolishingMode = false;
    }

    public static boolean isInBuildingMode() {
        return isInBuildingMode;
    }

    public static boolean isInDemolishingMode() {
        return isInDemolishingMode;
    }

    private static boolean checkAndShowTileAvailability(SpriteBatch batch, Vector2i gridPosition) {
        boolean isBuildable = true;
        BoxCollider area = currentBuilding.relativeCollider.cloneAndTranslate(gridPosition);
        List<Harvestable> harvestables = World.findHarvestables(area);
        List<Vector2i> harvestablePositions = new ArrayList<>(harvestables.size());
        for (Harvestable harvestable : harvestables) {
            harvestablePositions.add(harvestable.getCollider().getGridPosition());
        }

        for (Vector2i tile : area) {
            if (harvestablePositions.contains(tile)) {
                batch.setColor(UI.SEMI_TRANSPARENT_YELLOW);
            } else if (World.isBuildable(tile)) {
                batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            } else {
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                isBuildable = false;
            }

            Tiles.drawTile(batch, Textures.Tile.DEFAULT, tile);
        }

        Vector2i entrancePos = currentBuilding.entrancePosition.plus(gridPosition);
        if (rangeX.contains(entrancePos.x) && rangeY.contains(entrancePos.y) && World.isBuildable(entrancePos))
            batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
        else {
            batch.setColor(UI.SEMI_TRANSPARENT_RED);
            isBuildable = false;
        }
        Tiles.drawTile(batch, Textures.Tile.DEFAULT, entrancePos);

        if (currentBuilding instanceof WaterBuilding.Type building) {
            BoxCollider waterArea = building.waterArea.cloneAndTranslate(gridPosition);
            for (Vector2i tile : waterArea) {
                if (World.getTile(tile).equals(Tile.WATER)) {
                    batch.setColor(UI.SEMI_TRANSPARENT_BLUE);
                } else {
                    batch.setColor(UI.SEMI_TRANSPARENT_MAGENTA);
                    isBuildable = false;
                }

                Tiles.drawTile(batch, Textures.Tile.DEFAULT, tile);
            }
        }

        return isBuildable;
    }

    private static void showBuildingRange(SpriteBatch batch, Vector2i gridPosition, int range) {
        batch.setColor(UI.VERY_TRANSPARENT);
        Circle.draw(
                batch,
                Textures.Tile.DEFAULT,
                gridPosition,
                range);
        batch.setColor(UI.DEFAULT_COLOR);
    }

    public static void saveShiftActivity(ObjectOutputStream oos) throws IOException {
        for (ProductionBuilding.Type type : ProductionBuilding.Type.values()) {
            for (boolean shift : type.shifts) {
                oos.writeBoolean(shift);
            }
        }
    }

    public static void loadShiftActivity(ObjectInputStream ois) throws IOException {
        for (ProductionBuilding.Type type : ProductionBuilding.Type.values()) {
            for (int i = 0; i < type.shifts.length; i++) {
                type.shifts[i] = ois.readBoolean();
            }
            UI.loadShiftMenuValues();
        }
    }
}
