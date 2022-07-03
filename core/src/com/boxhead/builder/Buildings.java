package com.boxhead.builder;

public class Buildings {

    public enum Types {
        DEFAULT_BUILDING,
        DEFAULT_FUNCTIONAL_BUILDING,
        DEFAULT_RESIDENTIAL_BUILDING,
        BIG
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_FUNCTIONAL_BUILDING: return new FunctionalBuilding(Textures.getBuilding("service_fungus"), Jobs.UNEMPLOYED, 1);
            case DEFAULT_RESIDENTIAL_BUILDING: return new ResidentialBuilding(Textures.getBuilding("house_fungus"), 5);
            case BIG: return new Building(Textures.getBuilding("fungi"));
            default: return new Building(Textures.getBuilding("fungus"));
        }
    }
}
