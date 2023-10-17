package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.*;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

public class WaterBuilding extends ProductionBuilding {
    public static class Type extends ProductionBuilding.Type {
        public final BoxCollider waterArea;

        protected static Type[] values;

        public static final Type FISHING_HUT = new Type(
                Textures.Building.FISHING_HUT,
                "fishing hut",
                new Vector2i(0, -1),
                new BoxCollider(0, 0, 3, 2),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                Jobs.FISHERMAN,
                3,
                10,
                new BoxCollider(3, 0, 2, 2)
        );

        public static final Type WATERMILL = new Type(
                Textures.Building.WATERMILL,
                "watermill",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 4, 4),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                Jobs.FISHERMAN,
                3,
                10,
                new BoxCollider(4, 0, 1, 4)
        );

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                       Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, BoxCollider waterArea) {
            super(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, productionInterval, 0);
            this.waterArea = waterArea;
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException();
        }
    }

    public WaterBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }
}
