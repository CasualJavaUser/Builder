package com.boxhead.builder.game_objects;

import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE(Textures.Harvestables.PINE_TREE, Harvestable.Characteristic.TREE, 10, World.FULL_DAY),
        ROCK1(Textures.Harvestables.ROCK1, Harvestable.Characteristic.ROCK, 5, 0),
        ROCK2(Textures.Harvestables.ROCK2, Harvestable.Characteristic.ROCK, 5, 0),
        ROCK3(Textures.Harvestables.ROCK3, Harvestable.Characteristic.ROCK, 5, 0),
        IRON_ORE(Textures.Harvestables.ROCK1, Harvestable.Characteristic.ROCK, 5, 0),
        WHEAT(Textures.Harvestables.WHEAT, Harvestable.Characteristic.FIELD_CROP, 2, World.FULL_DAY / 10);

        public final Textures.Harvestables textureId;
        public final Harvestable.Characteristic characteristic;
        public final int yield;
        public final int growthTime;

        Type(Textures.Harvestables textureId, Harvestable.Characteristic characteristic, int yield, int growthTime) {
            this.textureId = textureId;
            this.characteristic = characteristic;
            this.yield = yield;
            this.growthTime = growthTime;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition) {
        return new Harvestable(type, gridPosition);
    }
}
