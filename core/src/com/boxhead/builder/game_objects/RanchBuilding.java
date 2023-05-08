package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.utils.Vector2i;

public class RanchBuilding extends FarmBuilding<FarmAnimal> {
    public RanchBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    public Animals.Type getAnimal() {
        return type.farmAnimal;
    }

    @Override
    public Resource getResource() {
        return type.farmAnimal.resource;
    }

    @Override
    public int getYield() {
        return type.farmAnimal.yield;
    }
}
