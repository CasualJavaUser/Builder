package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;

public class Animals {
    public enum Type {
        COW(Textures.Npc.IDLE0, Resource.MILK, 5, World.FULL_DAY / 20, false),
        PIG(Textures.Npc.IDLE0, Resource.MEAT, 5, World.FULL_DAY / 20, true),
        DEER(Textures.Npc.IDLE0, Resource.MEAT, 10, 0);

        public final Textures.TextureId textureId;
        public final Resource resource;
        public final int yield;
        public final int growthTime;
        public final boolean slaughtered;

        Type(Textures.TextureId textureId, Resource resource, int yield, int growthTime, boolean slaughtered) {
            this.textureId = textureId;
            this.resource = resource;
            this.yield = yield;
            this.growthTime = growthTime;
            this.slaughtered = slaughtered;
        }

        Type(Textures.TextureId textureId, Resource resource, int yield, int growthTime) {
            this(textureId, resource, yield, growthTime, true);
        }
    }
}
