package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

public class Buildings {
    private static boolean isBuilding = false;
    private static Types currentBuilding = null;

    public enum Types {
        DEFAULT_PRODUCTION_BUILDING,
        DEFAULT_RESIDENTIAL_BUILDING,
        DEFAULT_SERVICE_BUILDING,
        DEFAULT_STORAGE_BUILDING,
        BIG;

        public TextureRegion getTexture() {
            switch (this) {
                case DEFAULT_PRODUCTION_BUILDING: return Textures.getBuilding("work_fungus");
                case DEFAULT_RESIDENTIAL_BUILDING: return Textures.getBuilding("house_fungus");
                case DEFAULT_SERVICE_BUILDING: return Textures.getBuilding("service_fungus");
                case DEFAULT_STORAGE_BUILDING: return Textures.getBuilding("service_fungus");  //TODO temp texture
                case BIG: return Textures.getBuilding("fungi");
                default: return Textures.getBuilding("fungus");
            }
        }
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_PRODUCTION_BUILDING: return new ProductionBuilding("lumber mill", Textures.getBuilding("work_fungus"), Jobs.LUMBERJACK, 1, new Vector2i(0, -1), 100);
            case DEFAULT_RESIDENTIAL_BUILDING: return new ResidentialBuilding("house", Textures.getBuilding("house_fungus"), 5, new Vector2i(0, -1));
            case DEFAULT_SERVICE_BUILDING: return new ServiceBuilding("hospital", Textures.getBuilding("service_fungus"), Jobs.DOCTOR, Services.HEAL, 5, 10, new Vector2i(0, -1), 100, 100);
            case DEFAULT_STORAGE_BUILDING: return new StorageBuilding("storage", Textures.getBuilding("service_fungus"));
            case BIG: return new Building("fungi", Textures.getBuilding("fungi"));
            default: return new Building("fungus", Textures.getBuilding("fungus"));
        }
    }

    public static void placeBuilding(SpriteBatch batch) {
        TextureRegion texture = currentBuilding.getTexture();
        Vector3 mousePos = BuilderGame.getGameScreen().getCamera().unproject(BuilderGame.getGameScreen().getMouseScreenPos());

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2,
                mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE),
                posY = mouseY - (mouseY % World.TILE_SIZE);

        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, posX, posY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            isBuilding = !World.placeBuilding(currentBuilding, new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE));
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
