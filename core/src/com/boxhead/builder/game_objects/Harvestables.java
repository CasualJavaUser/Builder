package com.boxhead.builder.game_objects;

import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE,
        IRON_ORE
    }

    public static Harvestable get(Type type, Vector2i gridPosition) {
        switch (type) {
            case BIG_TREE: return new Harvestable(Textures.get(Textures.Environment.BIG_TREE), gridPosition, Harvestable.Characteristic.TREE, 10);
            case IRON_ORE: return new Harvestable(Textures.get(Textures.Environment.SMALL_TREE), gridPosition, Harvestable.Characteristic.IRON_ORE, 10);
            default: throw new IllegalArgumentException();
        }
    }

    public static BoxCollider getCollider(Harvestable harvestable) {
        BoxCollider collider;
        Vector2i pos = harvestable.getGridPosition();
        switch ((Harvestable.Characteristic)harvestable.getCharacteristic()) {
            case TREE: collider = new BoxCollider(new Vector2i(pos.x + harvestable.getTexture().getRegionWidth()/ World.TILE_SIZE/2, pos.y), 1, 1); break;
            default: collider = new BoxCollider(pos, harvestable.getTexture().getRegionWidth() / World.TILE_SIZE, harvestable.getTexture().getRegionHeight() / World.TILE_SIZE);
        }
        return collider;
    }

}
