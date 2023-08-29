package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Animals;
import com.boxhead.builder.game_objects.FarmAnimal;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

public class RanchBuilding extends FarmBuilding<FarmAnimal> {
    public static class Type extends FarmBuilding.Type {
        protected static Type[] values;

        public static final Type RANCH = new Type(
                Textures.Building.RANCH,
                "ranch",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 5, 2),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.FARMER,
                3
        );

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost, Job job, int maxEmployeeCapacity) {
            super(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity);
        }

        public static Type[] values() {
            return values;
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException();
        }
    }

    private static final int TILES_PER_ANIMAL = 9;

    private Animals.Type farmAnimal = Animals.Type.COW;

    public RanchBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    @Override
    public Type getType() {
        return ((Type) type);
    }

    public Animals.Type getAnimal() {
        return farmAnimal;
    }

    public int getAnimalCount() {
        return fieldCollider.getArea() / TILES_PER_ANIMAL;
    }

    public void spawnAnimals() {
        for (int i = 0; i < getAnimalCount(); i++) {
            FarmAnimal animal = new FarmAnimal(
                    farmAnimal,
                    getFieldCollider().getGridPosition().clone(),
                    fieldCollider
            );
            World.spawnAnimal(animal);
            addFieldWork(animal);
        }
    }

    @Override
    public Resource getResource() {
        return farmAnimal.resource;
    }

    @Override
    public Resource getDefaultResource() {
        return Animals.Type.COW.resource;
    }

    @Override
    public int getYield() {
        return farmAnimal.yield;
    }
}
