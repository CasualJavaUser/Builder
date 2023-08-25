package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.Harvestables;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.Set;
import java.util.stream.Collectors;

public class PlantationBuilding extends FarmBuilding<Harvestable> {
    public static class Type extends FarmBuilding.Type {
        protected static Type[] values;

        public static final Type PLANTATION = new Type(
                Textures.Building.PLANTATION,
                "plantation",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.FARMER,
                3
        );

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        public Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost, Job job, int maxEmployeeCapacity) {
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

    private Harvestables.Type crop = Harvestables.Type.WHEAT;

    public PlantationBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    @Override
    public Type getType() {
        return ((Type) type);
    }

    public Harvestables.Type getCrop() {
        return crop;
    }

    @Override
    public Resource getResource() {
        return crop.characteristic.resource;
    }

    @Override
    public Resource getDefaultResource() {
        return Harvestables.Type.WHEAT.characteristic.resource;
    }

    @Override
    public int getYield() {
        return crop.yield;
    }

    /**
     * Returns true if a new FieldHarvestable can be created on the given tile.
     */
    public boolean isArable(Vector2i gridPosition) {
        if (!fieldCollider.overlaps(gridPosition) || !World.isNavigable(gridPosition))
            return false;

        for (Harvestable harvestable : ownFieldWorks) {
            if (harvestable.getGridPosition().equals(gridPosition))
                return false;
        }
        return true;
    }

    @Override
    public void endShift(Job.ShiftTime shift) {
        super.endShift(shift);
        Set<Harvestable> notPlanted = ownFieldWorks.stream().filter(h -> h.getCurrentPhase() < 0).collect(Collectors.toSet());
        ownFieldWorks.removeAll(notPlanted);
    }
}
