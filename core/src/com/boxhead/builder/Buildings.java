package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

public class Buildings {

    public enum Types {
        DEFAULT_BUILDING,
        DEFAULT_FUNCTIONAL_BUILDING,
        DEFAULT_RESIDENTIAL_BUILDING,
        SMALL,
        BIG
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_FUNCTIONAL_BUILDING: return new FunctionalBuilding(new Texture("work_fungus"), Jobs.UNEMPLOYED, 1);
            case DEFAULT_RESIDENTIAL_BUILDING: return new ResidentialBuilding(new Texture("house_fungus"), 5);
            case SMALL: return new Building(new Texture("funguy.png"));
            case BIG: return new Building(new Texture("fungi.png"));
            default: return new Building(new Texture("fungus.png"));
        }
    }
}
