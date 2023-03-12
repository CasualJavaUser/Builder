package com.boxhead.builder.game_objects;

import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class Animal extends NPC {
    protected final Animals.Type type;

    public Animal(Animals.Type type, Vector2i gridPosition) {
        super(type.textureId, gridPosition);
        this.type = type;

        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT0"));  //TODO temp textures
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT0"));
    }

    public void wander() {
        if (path == null || followPath()) {
            if (World.getRandom().nextInt(360) == 0) {
                navigateTo(randomPosInRange(10));
            }
        }
    }

    private Vector2i randomPosInRange(int range) {
        double angle = World.getRandom().nextDouble() * 2 * Math.PI;
        return new Vector2i((int)(Math.cos(angle) * range), (int)(Math.sin(angle) * range));
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = Textures.Npc.valueOf("IDLE0");
        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT0"));
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT0"));
        currentTexture = getTexture();
        path = null;
    }
}
