package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class Harvestables {

    public enum Type {
        BIG_TREE(Harvestable.Characteristic.TREE, 10, Textures.Environment.PINE_TREE),
        SAPLING(BIG_TREE, Harvestable.Characteristic.FIELD, Resource.NOTHING, 1000, Textures.Environment.ROCK1),
        STONE(Harvestable.Characteristic.STONE, 5, Textures.Environment.ROCK1, Textures.Environment.ROCK2, Textures.Environment.ROCK3),
        IRON_ORE(Harvestable.Characteristic.IRON_ORE, Resource.IRON, 5, Textures.Environment.PINE_TREE),
        FIELD_HARVEST(Harvestable.Characteristic.FIELD, 10, Textures.Environment.ROCK3),

        FIELD_SEMI_GROWN(FIELD_HARVEST, Harvestable.Characteristic.FIELD, Resource.NOTHING, 1000, Textures.Environment.ROCK2),
        FIELD_SEEDED(FIELD_SEMI_GROWN, Harvestable.Characteristic.FIELD, Resource.NOTHING, 1000, Textures.Environment.ROCK2),
        FIELD_EMPTY(FIELD_SEEDED, Harvestable.Characteristic.FIELD, Resource.NOTHING, 10, Textures.Environment.ROCK1),
        FIELD_GRAIN(Harvestable.Characteristic.FIELD, Resource.GRAIN, 10, new int[]{100, 1000, 100}, Textures.Environment.ROCK1, Textures.Environment.ROCK2, Textures.Environment.ROCK3);

        public final Textures.Environment[] textures;
        public final Type nextPhase;
        public final Harvestable.Characteristic characteristic;
        public final Resource resource;
        public final int size;
        public final int[] phaseTimes;

        //StaticHarvestable
        Type(Harvestable.Characteristic characteristic, int size, Textures.Environment... textures) {
            this(characteristic, characteristic.resource, size, textures);
        }

        Type(Harvestable.Characteristic characteristic, Resource resource, int size, Textures.Environment... textures) {
            this(null, characteristic, resource, size, textures);
        }

        Type(Type nextPhase, Harvestable.Characteristic characteristic, Resource resource, int size, Textures.Environment... textures) {
            this.nextPhase = nextPhase;
            this.characteristic = characteristic;
            this.resource = resource;
            this.size = size;
            this.textures = textures;
            phaseTimes = null;
        }

        //FieldHarvestable
        Type(Harvestable.Characteristic characteristic, Resource resource, int size, int[] phaseTimes, Textures.Environment... textures) {
            if (textures.length != phaseTimes.length) throw new IllegalArgumentException("Mismatch in array sizes");
            this.characteristic = characteristic;
            this.resource = resource;
            this.size = size;
            this.textures = textures;
            this.phaseTimes = phaseTimes;
            nextPhase = null;
        }
    }

    public static Harvestable create(Type type, Vector2i gridPosition, int textureId) {
        Harvestable harvestable = create(type, gridPosition);
        harvestable.texture = Textures.get(type.textures[textureId]);
        return harvestable;
    }

    public static Harvestable create(Type type, Vector2i gridPosition) {
        if (type.phaseTimes == null) {
            return new StaticHarvestable(type, gridPosition);
        } else {
            return new FieldHarvestable(type, gridPosition);
        }
    }
}
