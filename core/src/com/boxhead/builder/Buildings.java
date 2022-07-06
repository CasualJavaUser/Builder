package com.boxhead.builder;

public class Buildings {

    public enum Types {
        DEFAULT_BUILDING,
        DEFAULT_PRODUCTION_BUILDING,
        DEFAULT_RESIDENTIAL_BUILDING,
        DEFAULT_SERVICE_BUILDING,
        BIG
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_PRODUCTION_BUILDING: return new ProductionBuilding(Textures.getBuilding("work_fungus"), Jobs.LUMBERJACK, 1, new Vector2i(0, -1));
            case DEFAULT_RESIDENTIAL_BUILDING: return new ResidentialBuilding(Textures.getBuilding("house_fungus"), 5, new Vector2i(0, -1));
            case BIG: return new Building(Textures.getBuilding("fungi"));
            default: return new Building(Textures.getBuilding("fungus"));
        }
    }
}
