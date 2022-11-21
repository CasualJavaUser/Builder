package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE(Textures.get(Textures.Environment.BIG_TREE)),
        STONE(Textures.get(Textures.Environment.STONE1), Textures.get(Textures.Environment.STONE2)),
        IRON_ORE(Textures.get(Textures.Environment.SMALL_TREE));

        private final TextureRegion[] textures;

        Type(TextureRegion... textures) {
            this.textures = textures;
        }

        public TextureRegion[] getTextures() {
            return textures;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition, int textureId) {
        switch (type) {
            case BIG_TREE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.TREE, 10);
            case STONE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.TREE, 5);
            case IRON_ORE: return new Harvestable(type.getTextures()[textureId], gridPosition, Harvestable.Characteristic.IRON_ORE, 10);
            default: throw new IllegalArgumentException();
        }
    }

}
