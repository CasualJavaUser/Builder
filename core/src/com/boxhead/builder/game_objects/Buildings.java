package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.TileCircle;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static boolean isDemolishing = false;
    private static Type currentBuilding;
    private static Range<Integer> rangeX, rangeY;

    public enum Type {
        DEFAULT_PRODUCTION_BUILDING(Textures.Building.WORK_FUNGUS, Jobs.LUMBERJACK, new Vector2i(0, -1)),
        DEFAULT_RESIDENTIAL_BUILDING(Textures.Building.HOUSE_FUNGUS, new Vector2i(0, -1)),
        DEFAULT_SERVICE_BUILDING(Textures.Building.SERVICE_FUNGUS, Jobs.DOCTOR, new Vector2i(0, -1)),
        DEFAULT_STORAGE_BUILDING(Textures.Building.STORAGE_FUNGUS),
        BIG(Textures.Building.FUNGI),
        CONSTRUCTION_OFFICE(Textures.Building.CONSTRUCTION_OFFICE, Jobs.BUILDER, new Vector2i(0, -1));

        public final Textures.Building texture;
        public final Job job;
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;

        Type(Textures.Building texture, Job job, Vector2i entrancePosition, BoxCollider relativeCollider) {
            this.texture = texture;
            this.job = job;
            this.entrancePosition = entrancePosition;
            this.relativeCollider = relativeCollider;
        }

        Type(Textures.Building texture, Job job, Vector2i entrancePosition) {
            this.texture = texture;
            this.job = job;
            this.entrancePosition = entrancePosition;
            TextureRegion tex = Textures.get(texture);
            this.relativeCollider = new BoxCollider(Vector2i.zero(),
                    tex.getRegionWidth() / World.TILE_SIZE,
                    tex.getRegionHeight() / World.TILE_SIZE);
        }

        Type(Textures.Building texture, Vector2i entrancePosition) {
            this(texture, null, entrancePosition);
        }

        Type(Textures.Building texture) {
            this(texture, null, null);
        }

        public BoxCollider getRelativeCollider() {
            return relativeCollider;
        }

        public TextureRegion getTexture() {
            return Textures.get(texture);
        }

        public Job getJob() {
            return job;
        }

        public Vector2i getEntrancePosition() {
            return entrancePosition;
        }

        public TextureRegion getConstructionSite() {
            try {
                return Textures.get(Textures.Building.valueOf(texture.name() + "_CS"));
            } catch (IllegalArgumentException e) {
                return getTexture();
            }
        }
    }

    public static Building create(Type building, Vector2i gridPosition) {
        switch (building) {
            case DEFAULT_PRODUCTION_BUILDING:
                return new ProductionBuilding("lumber mill", building, gridPosition, 1, 100);
            case DEFAULT_RESIDENTIAL_BUILDING:
                return new ResidentialBuilding("house", building, gridPosition, 5);
            case DEFAULT_SERVICE_BUILDING:
                return new ServiceBuilding("hospital", building, gridPosition, Service.HEAL, 5, 10, 100, 100);
            case DEFAULT_STORAGE_BUILDING:
                return new StorageBuilding("storage", building, gridPosition);
            case BIG:
                return new Building("fungi", building, gridPosition);
            case CONSTRUCTION_OFFICE:
                return new ProductionBuilding("construction office", building, gridPosition, 5, 0);
            default:
                throw new IllegalArgumentException("Unknown building type: " + building);
        }
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode || isDemolishing)
            throw new IllegalStateException("Not in building mode");

        TextureRegion texture = currentBuilding.getTexture();
        Vector3 mousePos = GameScreen.getMouseWorldPosition();

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE);
        int posY = mouseY - (mouseY % World.TILE_SIZE);

        posX = rangeX.fit(posX);
        posY = rangeY.fit(posY);

        if (currentBuilding.getJob() != null && currentBuilding.getJob().getRange() > 0) {
            showBuildingRange(batch,
                    posX + currentBuilding.entrancePosition.x * World.TILE_SIZE,
                    posY + currentBuilding.entrancePosition.y * World.TILE_SIZE,
                    currentBuilding.job.getRange());
        }
        boolean isBuildable = checkAndShowTileAvailability(batch, posX, posY);
        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, posX, posY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)  && isBuildable) {
            Vector2i buildingPosition = new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE);
            World.placeFieldWork(new ConstructionSite("construction site", buildingPosition, currentBuilding, 100));

            if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                isInBuildingMode = false;
        }
    }

    public static void demolish() {
        if (!isDemolishing || isInBuildingMode)
            throw new IllegalStateException("Not in demolishing mode");

        for(Building building : World.getBuildings()) {
            if(building.isClicked()) {
                World.removeBuilding(building);
                if(!InputManager.isKey(Input.Keys.CONTROL_LEFT)) isDemolishing = false;
                break;
            }
        }
    }

    public static void switchDemolishingMode() {
        isDemolishing = !isDemolishing;
        isInBuildingMode = false;
    }

    public static void toBuildingMode(Type building) {
        currentBuilding = building;
        rangeX = Range.between(0, World.getWidth() - currentBuilding.getTexture().getRegionWidth());
        rangeY = Range.between(0, World.getHeight() - currentBuilding.getTexture().getRegionHeight());
        isDemolishing = false;
        isInBuildingMode = true;
    }

    public static void turnOffBuildingMode() {
        isInBuildingMode = false;
    }

    public static boolean isInBuildingMode() {
        return isInBuildingMode;
    }

    public static boolean isDemolishing() {
        return isDemolishing;
    }

    private static boolean checkAndShowTileAvailability(SpriteBatch batch, int posX, int posY) {
        boolean isBuildable = true;
        for (int y = 0; y < currentBuilding.getRelativeCollider().getHeight(); y++) {
            for (int x = 0; x < currentBuilding.getRelativeCollider().getWidth(); x++) {
                if(World.isBuildable(new Vector2i(posX/World.TILE_SIZE + x, posY/World.TILE_SIZE + y)))
                    batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
                else {
                    batch.setColor(UI.SEMI_TRANSPARENT_RED);
                    isBuildable = false;
                }
                batch.draw(Textures.get(Textures.Tile.DEFAULT), posX + x * World.TILE_SIZE, posY + y * World.TILE_SIZE);
            }
        }
        if(currentBuilding.getEntrancePosition() != null) {
            Vector2i entrancePos = new Vector2i(posX/World.TILE_SIZE + currentBuilding.getEntrancePosition().x,
                                                posY/World.TILE_SIZE + currentBuilding.getEntrancePosition().y);
            if(rangeX.contains(entrancePos.x * World.TILE_SIZE) && rangeY.contains(entrancePos.y * World.TILE_SIZE) && World.isBuildable(entrancePos))
                batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            else {
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                isBuildable = false;
            }

            batch.draw(Textures.get(Textures.Tile.DEFAULT),
                    posX + currentBuilding.getEntrancePosition().x * World.TILE_SIZE,
                    posY + currentBuilding.getEntrancePosition().y * World.TILE_SIZE);
        }
        return isBuildable;
    }

    private static void showBuildingRange(SpriteBatch batch, int posX, int posY, int range) {
        batch.setColor(UI.VERY_TRANSPARENT);
        TileCircle.draw(
                batch,
                Textures.get(Textures.Tile.DEFAULT),  //todo temp texture
                posX,
                posY,
                range * World.TILE_SIZE);
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
