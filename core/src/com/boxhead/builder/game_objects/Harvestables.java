package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE(Textures.get(Textures.Environment.BIG_TREE)),
        IRON_ORE(Textures.get(Textures.Environment.SMALL_TREE));

        private final TextureRegion texture;

        Type(TextureRegion texture) {
            this.texture = texture;
        }

        public TextureRegion getTexture() {
            return texture;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition) {
        switch (type) {
            case BIG_TREE: return new Harvestable(type.getTexture(), gridPosition, Harvestable.Characteristic.TREE, 10);
            case IRON_ORE: return new Harvestable(type.getTexture(), gridPosition, Harvestable.Characteristic.IRON_ORE, 10);
            default: throw new IllegalArgumentException();
        }
    }

}
