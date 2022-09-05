package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static Type currentBuilding;

    public enum Type {
        DEFAULT_PRODUCTION_BUILDING(Textures.get(Textures.Building.WORK_FUNGUS)),
        DEFAULT_RESIDENTIAL_BUILDING(Textures.get(Textures.Building.HOUSE_FUNGUS)),
        DEFAULT_SERVICE_BUILDING(Textures.get(Textures.Building.SERVICE_FUNGUS)),
        DEFAULT_STORAGE_BUILDING(Textures.get(Textures.Building.SERVICE_FUNGUS)), //TODO temp texture
        BIG(Textures.get(Textures.Building.FUNGI)),
        CONSTRUCTION_OFFICE(Textures.get(Textures.Building.FUNGUS));

        public final TextureRegion texture;

        Type(TextureRegion texture) {
            this.texture = texture;
        }
    }

    public static Building get(Type building) {
        switch (building) {
            case DEFAULT_PRODUCTION_BUILDING:
                return new ProductionBuilding("lumber mill", building.texture, Job.LUMBERJACK, 1, new Vector2i(0, -1), 100);
            case DEFAULT_RESIDENTIAL_BUILDING:
                return new ResidentialBuilding("house", building.texture, 5, new Vector2i(0, -1));
            case DEFAULT_SERVICE_BUILDING:
                return new ServiceBuilding("hospital", building.texture, Job.DOCTOR, Service.HEAL, 5, 10, new Vector2i(0, -1), 100, 100);
            case DEFAULT_STORAGE_BUILDING:
                return new StorageBuilding("storage", building.texture);
            case BIG:
                return new Building("fungi", building.texture);
            case CONSTRUCTION_OFFICE:
                return new ProductionBuilding("construction office", building.texture, Job.BUILDER, 5, new Vector2i(0, -1));
            default:
                throw new IllegalArgumentException("Unknown building type: " + building);
        }
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode)
            throw new IllegalStateException("Not in building mode");

        TextureRegion texture = currentBuilding.texture;
        Vector3 mousePos = BuilderGame.getGameScreen().getMousePosition();

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE);
        int posY = mouseY - (mouseY % World.TILE_SIZE);

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
}
