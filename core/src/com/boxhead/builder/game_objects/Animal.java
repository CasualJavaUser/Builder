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
        walkingSpeed = 0.4f;

        currentAnimation = Enum.valueOf(Textures.NpcAnimation.class, type.name());
    }

    @Override
    public void wander() {
        if (path == null || followPath()) {
            if (World.getRandom().nextInt(360) == 0) {
                navigateTo(randomPosInRange(10));
            }
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        currentAnimation = Enum.valueOf(Textures.NpcAnimation.class, type.name());
        textureId = type.textureId;
        currentTexture = getTexture();
        path = null;
    }
}
