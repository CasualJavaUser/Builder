package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class Harvestable extends GameObject implements FieldWork {
    private final Characteristic characteristic;
    private final int productionInterval = 50;
    private int productionCounter = 0;
    private int amountLeft;
    private NPC assigned;
    private boolean worked;
    private final BoxCollider collider;
    private transient Textures.Environment textureType;

    public Harvestable(Textures.Environment texture, Vector2i gridPosition, Characteristic characteristic, int size) {
        super(Textures.get(texture), gridPosition);
        textureType = texture;
        this.characteristic = characteristic;
        amountLeft = size;
        if (characteristic != Characteristic.TREE) collider = getDefaultCollider();
        else
            collider = new BoxCollider(new Vector2i(gridPosition.x + getTexture().getRegionWidth() / World.TILE_SIZE / 2, gridPosition.y), 1, 1);
    }

    public Harvestable(Textures.Environment texture, Vector2i gridPosition, BoxCollider collider, Characteristic characteristic, int size) {
        super(Textures.get(texture), gridPosition);
        textureType = texture;
        this.collider = collider;
        this.characteristic = characteristic;
        amountLeft = size;
    }

    public enum Characteristic {
        TREE(Resource.WOOD),
        STONE(Resource.STONE),
        IRON_ORE(Resource.IRON);

        public final Resource resource;

        Characteristic(Resource resource) {
            this.resource = resource;
        }
    }

    @Override
    public Object getCharacteristic() {
        return characteristic;
    }

    @Override
    public void assignWorker(NPC npc) {
        if (assigned == null) {
            assigned = npc;
        } else if (assigned != npc)
            throw new IllegalArgumentException("Assignment over capacity");
    }

    @Override
    public void dissociateWorker(NPC npc) {
        if (assigned == npc) {
            assigned = null;
            worked = false;
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null;
    }

    @Override
    public boolean isRemoved() {
        return amountLeft <= 0;
    }

    @Override
    public void work() {
        if (worked) {
            Resource resource = characteristic.resource;
            boolean exit = false;
            if (assigned.getInventory().getAvailableCapacity() > 0) {
                productionCounter++;
                if (productionCounter == productionInterval) {
                    productionCounter = 0;
                    amountLeft--;
                    assigned.getInventory().put(resource, 1);
                }
            } else exit = true;

            if (amountLeft <= 0)
                exit = true;

            if (exit) {
                assigned.getWorkplace().dissociateFieldWork(assigned);
                assigned.giveOrder(NPC.Order.Type.GO_TO, assigned.getWorkplace());
                assigned.giveOrder(NPC.Order.Type.ENTER, assigned.getWorkplace());
                assigned.giveOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, resource, assigned.getInventory().getResourceAmount(resource));
                assigned.giveOrder(NPC.Order.Type.REQUEST_TRANSPORT, resource, NPC.INVENTORY_SIZE);
                worked = false;
                assigned = null;
            }
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (npc == assigned) worked = b;
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
    }

    @Override
    public String toString() {
        return characteristic.toString() + " " + gridPosition.toString();
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(textureType.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureType = Textures.Environment.valueOf(ois.readUTF());
        texture = Textures.get(textureType);
    }
}
