package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Comparator;

public class Harvestable extends GameObject implements FieldWork {
    //TODO description
    public static final SortedList<Pair<Long, Harvestable>> timeTriggers = new SortedList<>((p1, p2) -> Long.compare(p2.first, p1.first));

    private final Harvestables.Type type;
    private final int productionInterval = 50;
    private int productionCounter = 0;
    private int amountLeft;
    private NPC assigned;
    private boolean worked;
    private final BoxCollider collider;
    private transient Textures.Environment textureId;

    public Harvestable(Textures.Environment texture, Vector2i gridPosition, Harvestables.Type type) {
        super(Textures.get(texture), gridPosition);
        textureId = texture;
        this.type = type;
        int size = type.size;
        amountLeft = size;
        if (type.condition == Condition.TIME) {
            timeTriggers.add(Pair.of(World.calculateDate(size), this));
        }
        if (type.characteristic != Characteristic.TREE) collider = getDefaultCollider();
        else
            collider = new BoxCollider(new Vector2i(gridPosition.x + getTexture().getRegionWidth() / World.TILE_SIZE / 2, gridPosition.y), 1, 1);
    }

    public Harvestable(Textures.Environment texture, Vector2i gridPosition, BoxCollider collider, Harvestables.Type type) {
        super(Textures.get(texture), gridPosition);
        textureId = texture;
        this.collider = collider;
        this.type = type;
        amountLeft = type.size;
        if (type.condition == Condition.TIME) {
            timeTriggers.add(Pair.of(World.calculateDate(type.size), this));
        }
    }

    public enum Characteristic {
        TREE(Resource.WOOD),
        STONE(Resource.STONE),
        IRON_ORE(Resource.IRON),
        FIELD(Resource.GRAIN);

        public final Resource resource;

        Characteristic(Resource resource) {
            this.resource = resource;
        }
    }

    public enum Condition {
        WORK,
        TIME
    }

    public void changePhase() {
        World.removeFieldWorks(this);
        Harvestables.Type nextType = type.nextPhase;
        if (nextType == null) return;

        World.placeFieldWork(Harvestables.create(nextType, gridPosition));
    }

    public Harvestables.Type getType() {
        return type;
    }

    @Override
    public Object getCharacteristic() {
        return type.characteristic;
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
        return type.condition == Condition.WORK && assigned == null;
    }

    @Override
    public void work() {    //todo this is really ugly and needs to be rewritten
        if (worked) {
            Resource resource = type.resource;
            boolean exit = false;
            if (assigned.getInventory().getAvailableCapacity() > 0) {
                productionCounter++;
                if (productionCounter == productionInterval) {
                    productionCounter = 0;
                    amountLeft--;
                    if (resource != Resource.NOTHING)
                        assigned.getInventory().put(resource, 1);
                }
            } else exit = true;

            if (amountLeft <= 0) {
                exit = true;
                changePhase();
            }

            if (exit) {
                assigned.getWorkplace().dissociateFieldWork(assigned);
                assigned.giveOrder(NPC.Order.Type.GO_TO, assigned.getWorkplace());
                assigned.giveOrder(NPC.Order.Type.ENTER, assigned.getWorkplace());
                if (resource != Resource.NOTHING) {
                    assigned.giveOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, resource, assigned.getInventory().getResourceAmount(resource));
                    assigned.giveOrder(NPC.Order.Type.REQUEST_TRANSPORT, resource, NPC.INVENTORY_SIZE);
                } else {
                    assigned.getWorkplace().cancelReservation(NPC.INVENTORY_SIZE);
                }
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
        return type.characteristic.toString() + " " + gridPosition.toString();
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(textureId.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = Textures.Environment.valueOf(ois.readUTF());
        texture = Textures.get(textureId);
    }
}
