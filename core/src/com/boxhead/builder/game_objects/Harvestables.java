package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import static com.badlogic.gdx.math.MathUtils.random;

public class Harvestables {

    public enum Type {
        BIG_TREE(Harvestable.Characteristic.TREE, 10, Textures.Environment.PINE_TREE),
        SAPLING(BIG_TREE, Harvestable.Characteristic.FIELD, Resource.NOTHING, Harvestable.Condition.TIME, 1000, Textures.Environment.ROCK1),
        STONE(Harvestable.Characteristic.STONE, 5, Textures.Environment.ROCK1, Textures.Environment.ROCK2, Textures.Environment.ROCK3),
        IRON_ORE(Harvestable.Characteristic.IRON_ORE, Resource.IRON, 5, Textures.Environment.PINE_TREE),
        FIELD_HARVEST(Harvestable.Characteristic.FIELD, 10, Textures.Environment.PINE_TREE),

        FIELD_SEMI_GROWN(FIELD_HARVEST, Harvestable.Characteristic.FIELD, Resource.NOTHING, Harvestable.Condition.TIME, 1000, Textures.Environment.ROCK2),
        FIELD_SEEDED(FIELD_SEMI_GROWN, Harvestable.Characteristic.FIELD, Resource.NOTHING, Harvestable.Condition.TIME, 1000, Textures.Environment.PINE_TREE),
        FIELD_EMPTY(FIELD_SEEDED, Harvestable.Characteristic.FIELD, Resource.NOTHING, Harvestable.Condition.WORK, 10, Textures.Environment.ROCK1);

        public final Textures.Environment[] textures;
        public final Type nextPhase;
        public final Harvestable.Condition condition;
        public final Harvestable.Characteristic characteristic;
        public final Resource resource;
        public final int size;

        /**
         * Default resource, condition: work
         */
        Type(Harvestable.Characteristic characteristic, int size, Textures.Environment... textures) {
            this(characteristic, characteristic.resource, size, textures);
        }

        Type(Harvestable.Characteristic characteristic, Resource resource, int size, Textures.Environment... textures) {
            nextPhase = null;
            condition = Harvestable.Condition.WORK;
            this.characteristic = characteristic;
            this.resource = resource;
            this.size = size;
            this.textures = textures;
        }

        Type(Type nextPhase, Harvestable.Characteristic characteristic, Resource resource, Harvestable.Condition condition, int size, Textures.Environment... textures) {
            this.nextPhase = nextPhase;
            this.characteristic = characteristic;
            this.resource = resource;
            this.condition = condition;
            this.size = size;
            this.textures = textures;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition) {
        int textureId = random.nextInt(type.textures.length);
        return create(type, gridPosition, textureId);
    }

    public static Harvestable create(Type type, Vector2i gridPosition, int textureId) {
        if (type.characteristic == Harvestable.Characteristic.TREE) {
            Textures.Environment texture = type.textures[textureId];
            return new Harvestable(texture, gridPosition, new BoxCollider(new Vector2i(
                    gridPosition.x + Textures.get(texture).getRegionWidth() / World.TILE_SIZE / 2, gridPosition.y), 1, 1),
                    type);
        }

        return new Harvestable(type.textures[textureId], gridPosition, type);
    }
}
