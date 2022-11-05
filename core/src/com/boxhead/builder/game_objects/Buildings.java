package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static Type currentBuilding;

    public enum Type {
        DEFAULT_PRODUCTION_BUILDING(Textures.Building.WORK_FUNGUS),
        DEFAULT_RESIDENTIAL_BUILDING(Textures.Building.HOUSE_FUNGUS),
        DEFAULT_SERVICE_BUILDING(Textures.Building.SERVICE_FUNGUS),
        DEFAULT_STORAGE_BUILDING(Textures.Building.STORAGE_FUNGUS),
        BIG(Textures.Building.FUNGI),
        CONSTRUCTION_OFFICE(Textures.Building.CONSTRUCTION_OFFICE);

        public final Textures.Building texture;
        public final BoxCollider relativeCollider;

        Type(Textures.Building texture, BoxCollider relativeCollider) {
            this.texture = texture;
            this.relativeCollider = relativeCollider;
        }

        Type(Textures.Building texture) {
            this.texture = texture;
            TextureRegion tex = Textures.get(texture);
            this.relativeCollider = new BoxCollider(Vector2i.zero(),
                    tex.getRegionWidth() / World.TILE_SIZE,
                    tex.getRegionHeight() / World.TILE_SIZE);
        }

        public BoxCollider getRelativeCollider() {
            return relativeCollider;
        }

        public TextureRegion getTexture() {
            return Textures.get(texture);
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
                return new ProductionBuilding("lumber mill", building, gridPosition, Job.LUMBERJACK, 1, new Vector2i(0, -1), 100);
            case DEFAULT_RESIDENTIAL_BUILDING:
                return new ResidentialBuilding("house", building, gridPosition, 5, new Vector2i(0, -1));
            case DEFAULT_SERVICE_BUILDING:
                return new ServiceBuilding("hospital", building, gridPosition, Job.DOCTOR, Service.HEAL, 5, 10, new Vector2i(0, -1), 100, 100);
            case DEFAULT_STORAGE_BUILDING:
                return new StorageBuilding("storage", building, gridPosition);
            case BIG:
                return new Building("fungi", building, gridPosition);
            case CONSTRUCTION_OFFICE:
                return new ProductionBuilding("construction office", building, gridPosition, Job.BUILDER, 5, new Vector2i(0, -1), 0);
            default:
                throw new IllegalArgumentException("Unknown building type: " + building);
        }
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode)
            throw new IllegalStateException("Not in building mode");

        TextureRegion texture = currentBuilding.getTexture();
        Vector3 mousePos = BuilderGame.getGameScreen().getMousePosition();

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE);
        int posY = mouseY - (mouseY % World.TILE_SIZE);

        showTileAvailability(batch, posX, posY);
        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, posX, posY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2i buildingPosition = new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE);
            boolean constructionStarted = World.startConstruction(currentBuilding, buildingPosition);

            if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                isInBuildingMode = !constructionStarted;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            isInBuildingMode = false;
    }

    public static void toBuildingMode(Type building) {
        currentBuilding = building;
        isInBuildingMode = true;
    }

    public static boolean isInBuildingMode() {
        return isInBuildingMode;
    }

    private static void showTileAvailability(SpriteBatch batch, int posX, int posY) {
        for (int y = 0; y < currentBuilding.getRelativeCollider().getHeight(); y++) {
            for (int x = 0; x < currentBuilding.getRelativeCollider().getWidth(); x++) {
                if(World.getNavigableTiles().contains(new Vector2i(posX/World.TILE_SIZE + x, posY/World.TILE_SIZE + y)))
                    batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
                else
                    batch.setColor(UI.SEMI_TRANSPARENT_RED);
                batch.draw(Textures.get(Textures.Tile.DEFAULT), posX + x * World.TILE_SIZE, posY + y * World.TILE_SIZE);
            }
        }
    }
}
