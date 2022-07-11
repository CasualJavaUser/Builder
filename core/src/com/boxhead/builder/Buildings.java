package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Buildings {

    public enum Types {
        DEFAULT_BUILDING,
        DEFAULT_PRODUCTION_BUILDING,
        DEFAULT_RESIDENTIAL_BUILDING,
        DEFAULT_SERVICE_BUILDING,
        BIG;

        public TextureRegion getTexture() {
            switch (this) {
                case DEFAULT_PRODUCTION_BUILDING: return Textures.getBuilding("work_fungus");
                case DEFAULT_RESIDENTIAL_BUILDING: return Textures.getBuilding("house_fungus");
                case DEFAULT_SERVICE_BUILDING: return Textures.getBuilding("service_fungus");
                case BIG: return Textures.getBuilding("fungi");
                default: return Textures.getBuilding("fungus");
            }
        }
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_PRODUCTION_BUILDING: return new ProductionBuilding(Textures.getBuilding("work_fungus"), Jobs.LUMBERJACK, 1, new Vector2i(0, -1));
            case DEFAULT_RESIDENTIAL_BUILDING: return new ResidentialBuilding(Textures.getBuilding("house_fungus"), 5, new Vector2i(0, -1));
            case DEFAULT_SERVICE_BUILDING: return new ServiceBuilding(Textures.getBuilding("service_fungus"), Jobs.DOCTOR, Services.HEAL, 5, 10, new Vector2i(0, -1));
            case BIG: return new Building(Textures.getBuilding("fungi"));
            default: return new Building(Textures.getBuilding("fungus"));
        }
    }
}
