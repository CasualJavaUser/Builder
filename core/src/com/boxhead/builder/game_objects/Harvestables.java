package com.boxhead.builder.game_objects;

import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE,
        IRON_ORE
    }

    public static Harvestable create(Type type, Vector2i gridPosition) {
        switch (type) {
            case BIG_TREE: return new Harvestable(Textures.get(Textures.Environment.BIG_TREE), gridPosition, Harvestable.Characteristic.TREE, 10);
            case IRON_ORE: return new Harvestable(Textures.get(Textures.Environment.SMALL_TREE), gridPosition, Harvestable.Characteristic.IRON_ORE, 10);
            default: throw new IllegalArgumentException();
        }
    }

}
