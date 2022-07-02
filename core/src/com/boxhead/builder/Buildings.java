package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

public class Buildings {

    public enum Types {
        DEFAULT_BUILDING,
        DEFAULT_FUNCTIONAL_BUILDING
    }

    public static Building get(Types building) {
        switch(building) {
            case DEFAULT_FUNCTIONAL_BUILDING: return new FunctionalBuilding(new Texture("house_fungus"), Jobs.UNEMPLOYED, 1);
            default: return new Building(new Texture("fungus.png"));
        }
    }
}
