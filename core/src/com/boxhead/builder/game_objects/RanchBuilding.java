package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class RanchBuilding extends FarmBuilding<FarmAnimal> {
    private static final int TILES_PER_ANIMAL = 9;

    public RanchBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    public Animals.Type getAnimal() {
        return type.farmAnimal;
    }

    public int getAnimalCount() {
        return fieldCollider.getArea() / TILES_PER_ANIMAL;
    }

    public void spawnAnimals() {
        for (int i = 0; i < getAnimalCount(); i++) {
            FarmAnimal animal = new FarmAnimal(
                    type.farmAnimal,
                    getFieldCollider().getGridPosition().clone(),
                    fieldCollider
            );
            World.spawnAnimal(animal);
            addFieldWork(animal);
        }
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
