package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

import static com.badlogic.gdx.math.MathUtils.random;

public class StaticHarvestable extends Harvestable {
    private final BoxCollider collider;
    private final int textureVariant;

    public StaticHarvestable(Harvestables.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        textureVariant = random.nextInt(type.textures.length);
        if (type.characteristic == Characteristic.TREE) {
            collider = new BoxCollider(new Vector2i(
                    gridPosition.x + Textures.get(type.textures[textureVariant]).getRegionWidth() / World.TILE_SIZE / 2, gridPosition.y), 1, 1);
        } else {
            collider = getDefaultCollider();
        }
    }

    public StaticHarvestable(Harvestables.Type type, Vector2i gridPosition, BoxCollider collider) {
        super(type, gridPosition);
        this.collider = collider;
        textureVariant = random.nextInt(type.textures.length);
    }

    @Override
    public boolean isFree() {
        return assigned == null;
    }

    @Override
    public boolean isNavigable() {
        return false;
    }

    @Override
    public void work() {
        if (worked) {
            boolean exit = false;
            Resource resource = type.resource;
            if (!assigned.getInventory().isFull()) {
                if (productionCycle()) {
                    assigned.getInventory().put(resource, 1);
                }
            } else exit = true;

            if (amountLeft <= 0) {
                exit = true;
                World.removeFieldWorks(this);
            }

            if (exit)
                exit(resource);
        }
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Harvestables.Type.valueOf(ois.readUTF());
        texture = Textures.get(type.textures[textureVariant]);
    }
}
