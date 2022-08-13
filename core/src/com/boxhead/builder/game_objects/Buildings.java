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
    private static boolean isBuilding = false;
    private static Types currentBuilding = null;

    public enum Types {
        DEFAULT_PRODUCTION_BUILDING(Textures.get(Textures.Building.WORK_FUNGUS)),
        DEFAULT_RESIDENTIAL_BUILDING(Textures.get(Textures.Building.HOUSE_FUNGUS)),
        DEFAULT_SERVICE_BUILDING(Textures.get(Textures.Building.SERVICE_FUNGUS)),
        DEFAULT_STORAGE_BUILDING(Textures.get(Textures.Building.SERVICE_FUNGUS)), //TODO temp texture
        BIG(Textures.get(Textures.Building.FUNGI)),
        CONSTRUCTION_OFFICE(Textures.get(Textures.Building.FUNGUS));

        public final TextureRegion texture;

        Types(TextureRegion texture) {
            this.texture = texture;
        }
    }

    public static Building get(Types building) {
        switch (building) {
            case DEFAULT_PRODUCTION_BUILDING:
                return new ProductionBuilding("lumber mill", building.texture, Jobs.LUMBERJACK, 1, new Vector2i(0, -1), 100);
            case DEFAULT_RESIDENTIAL_BUILDING:
                return new ResidentialBuilding("house", building.texture, 5, new Vector2i(0, -1));
            case DEFAULT_SERVICE_BUILDING:
                return new ServiceBuilding("hospital", building.texture, Jobs.DOCTOR, Services.HEAL, 5, 10, new Vector2i(0, -1), 100, 100);
            case DEFAULT_STORAGE_BUILDING:
                return new StorageBuilding("storage", building.texture);
            case BIG:
                return new Building("fungi", building.texture);
            case CONSTRUCTION_OFFICE:
                return new ProductionBuilding("construction office", building.texture, Jobs.BUILDER, 5, new Vector2i(0, -1));
            default:
                throw new IllegalArgumentException("Unknown building type: " + building);
        }
    }

    public static void placeBuilding(SpriteBatch batch) {
        TextureRegion texture = currentBuilding.texture;
        Vector3 mousePos = BuilderGame.getGameScreen().getCamera().unproject(BuilderGame.getGameScreen().getMouseScreenPos());

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE);
        int posY = mouseY - (mouseY % World.TILE_SIZE);

        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, posX, posY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            isBuilding = !World.startConstruction(currentBuilding, new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE));
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) isBuilding = true;
        }
    }

    public static void build(Types building) {
        currentBuilding = building;
        isBuilding = true;
    }

    public static boolean isBuilding() {
        return isBuilding;
    }

    public static void setIsBuilding(boolean isBuilding) {
        Buildings.isBuilding = isBuilding;
    }
}
