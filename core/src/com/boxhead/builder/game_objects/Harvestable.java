package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Harvestable extends GameObject implements FieldWork {
    private final Type type;
    private final int productionInterval = 50;
    private int productionCounter = 0;
    private int amountLeft;
    private NPC assigned;
    private boolean worked;
    private final BoxCollider collider;

    public Harvestable(TextureRegion texture, Vector2i gridPosition, Type type, int size) {
        super(texture, gridPosition);
        this.type = type;
        amountLeft = size;
        collider = new BoxCollider(gridPosition, super.texture.getRegionWidth(), super.texture.getRegionHeight());
    }

    public static Harvestable getByCoordinates(Vector2i gridPosition) {
        for (Harvestable harvestable : World.getHarvestables()) {
            if (harvestable.gridPosition.equals(gridPosition)) {
                return harvestable;
            }
        }
        return null;
    }

    @Override
    public Object getCharacteristic() {
        return type;
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
            if (assigned.getInventory().getAvailableCapacityFor(type.resource) > 0) {
                productionCounter++;
                if (productionCounter == productionInterval) {
                    productionCounter = 0;
                    amountLeft--;
                    assigned.getInventory().put(type.resource, 1);
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
                assigned.giveResourceOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, type.resource);
                worked = false;
                assigned = null;
            }
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (npc == assigned) worked = b;
    }

    public enum Type {
        TREE(Resource.WOOD),
        IRON_ORE(Resource.IRON);

        public final Resource resource;

        Type(Resource resource) {
            this.resource = resource;
        }
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }
}
