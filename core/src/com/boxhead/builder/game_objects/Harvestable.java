package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Harvestable extends GameObject implements FieldWork {
    private final Characteristic characteristic;
    private final int productionInterval = 50;
    private int productionCounter = 0;
    private int amountLeft;
    private NPC assigned;
    private boolean worked;
    private BoxCollider collider;

    public Harvestable(TextureRegion texture, Vector2i gridPosition, Characteristic characteristic, int size) {
        super(texture, gridPosition);
        this.characteristic = characteristic;
        amountLeft = size;
        if(characteristic != Characteristic.TREE) collider = getDefaultCollider();
        else collider = new BoxCollider(new Vector2i(gridPosition.x + texture.getRegionWidth()/ World.TILE_SIZE/2, gridPosition.y), 1, 1);
    }

    public Harvestable(TextureRegion texture, Vector2i gridPosition, BoxCollider collider, Characteristic characteristic, int size) {
        super(texture, gridPosition);
        this.collider = collider;
        this.characteristic = characteristic;
        amountLeft = size;
    }

    public static Harvestable getByCoordinates(Vector2i gridPosition) {
        for (Harvestable harvestable : World.getHarvestables()) {
            if (harvestable.gridPosition.equals(gridPosition)) {
                return harvestable;
            }
        }
        return null;
    }

    public enum Characteristic {
        TREE(Resource.WOOD),
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
    public boolean assignWorker(NPC npc) {
        if (assigned == null) {
            assigned = npc;
            return true;
        }
        return false;
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
    public void work() {
        if (worked) {
            boolean exit = false;
            if (assigned.getInventory().getAvailableCapacityFor(characteristic.resource) > 0) {
                productionCounter++;
                if (productionCounter == productionInterval) {
                    productionCounter = 0;
                    amountLeft--;
                    assigned.getInventory().put(characteristic.resource, 1);
                }
            } else exit = true;

            if (amountLeft <= 0) {
                World.makeNavigable(collider);
                World.getHarvestables().remove(this);
                exit = true;
            }
            if (exit) {
                assigned.getWorkplace().dissociateFieldWork(assigned);
                assigned.giveOrder(NPC.Order.Type.GO_TO, assigned.getWorkplace());
                assigned.giveOrder(NPC.Order.Type.ENTER, assigned.getWorkplace());
                assigned.giveResourceOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, characteristic.resource);
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
}
