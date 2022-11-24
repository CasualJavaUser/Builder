package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.utils.Vector2i;

public class StorageBuilding extends EnterableBuilding {

    public StorageBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    public boolean isFull() {
        return getInventory().getAvailableCapacity() == 0;
    }

    public int getStored(Resource resource) {
        return getInventory().getResourceAmount(resource);
    }

    @Deprecated
    public int getStoredMass() {
        return getInventory().getCurrentAmount();
    }

    @Deprecated
    public int getMaxMass() {
        return getInventory().getMaxCapacity();
    }

    public int getRemainingCapacity() {
        return inventory.getAvailableCapacity();
    }
}
