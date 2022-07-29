package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class Harvestable extends GameObject implements FieldWork {
    private final Harvestable.Types type;
    private int amountLeft;
    private NPC assigned;
    private boolean worked;

    public Harvestable(TextureRegion texture, Vector2i gridPosition, Harvestable.Types type, int size) {
        super(texture, gridPosition);
        this.type = type;
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
        if (worked && assigned.getWorkplace().getStorage().checkStorageAvailability(assigned.getJob()) == 0) {
            assigned.getWorkplace().getStorage().addToStorage(assigned.getJob());
            for (int i = 0; i < assigned.getJob().getResources().length; i++) {
                if (assigned.getJob().getResources()[i] == type.getResource()) {
                    amountLeft += assigned.getJob().getChange()[i];
                    break;
                }
            }
        }

        if (amountLeft <= 0) {
            World.makeNavigable(gridPosition);
            World.getHarvestables().remove(this);

            assigned.navigateTo(assigned.getWorkplace());
            assigned.setDestination(NPC.Pathfinding.Destination.WORK);
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (npc == assigned) worked = b;
    }

    public enum Types {
        TREE(Resources.WOOD),
        IRON_ORE(Resources.IRON);

        private final Resources resource;

        Types(Resources resource) {
            this.resource = resource;
        }

        public Resources getResource() {
            return resource;
        }
    }
}
