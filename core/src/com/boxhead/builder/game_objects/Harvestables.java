package com.boxhead.builder.game_objects;

import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE(Textures.Environment.PINE_TREE),
        STONE(Textures.Environment.ROCK1, Textures.Environment.ROCK2, Textures.Environment.ROCK3),
        IRON_ORE(Textures.Environment.PINE_TREE);

        private final Textures.Environment[] textures;

        Type(Textures.Environment... textures) {
            this.textures = textures;
        }

        public Textures.Environment[] getTextures() {
            return textures;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition, int textureId) {
        switch (type) {
            case BIG_TREE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.TREE, 10);
            case STONE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.STONE, 5);
            case IRON_ORE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.IRON_ORE, 10);
            default: throw new IllegalArgumentException();
        }
    }

}
