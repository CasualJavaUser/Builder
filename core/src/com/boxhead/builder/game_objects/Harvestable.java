package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.util.Comparator;

public abstract class Harvestable extends GameObject implements FieldWork {
    public static final SortedList<Pair<Long, FieldHarvestable>> timeTriggers = new SortedList<>(Comparator.comparing(pair -> pair.first, Comparator.reverseOrder()));

    protected transient Harvestables.Type type;
    protected final int productionInterval = 50;
    protected int productionCounter = 0;
    protected int amountLeft;
    protected NPC assigned = null;
    protected boolean worked = false;

    public Harvestable(Harvestables.Type type, Vector2i gridPosition) {
        this(type, gridPosition, 0);
    }

    public Harvestable(Harvestables.Type type, Vector2i gridPosition, int textureId) {
        super(type.textures[textureId], gridPosition);
        this.type = type;
        amountLeft = type.size;
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

    public Harvestables.Type getType() {
        return type;
    }

    protected boolean productionCycle() {
        productionCounter++;
        if (productionCounter == productionInterval) {
            amountLeft--;
            productionCounter = 0;
            return true;
        }
        return false;
    }

    protected void exit(Resource resource) {
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

    @Override
    public Object getCharacteristic() {
        return type.characteristic;
    }

    @Override
    public void assignWorker(NPC npc) {
        if (isFree()) {
            assigned = npc;
        } else throw new IllegalArgumentException();
    }

    @Override
    public void dissociateWorker(NPC npc) {
        if (assigned == npc) {
            assigned = null;
            worked = false;
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (npc == assigned) worked = b;
    }

    @Override
    public String toString() {
        return type.characteristic.toString() + " " + gridPosition.toString();
    }

    public abstract void work();

    public abstract boolean isNavigable();

    public abstract boolean isFree();
}
