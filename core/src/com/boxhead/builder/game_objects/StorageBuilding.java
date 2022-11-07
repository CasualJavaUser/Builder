package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.utils.Vector2i;

public class StorageBuilding extends Building {

    public StorageBuilding(String name, Buildings.Type type, Vector2i gridPosition) {
        super(name, type, gridPosition);
    }

    public boolean isFull(Resource resource) {
        return getInventory().getAvailableCapacityFor(resource) == 0;
    }

    public int getStored(Resource resource) {
        return getInventory().getResourceAmount(resource);
    }

    @Deprecated
    public int getStoredMass() {
        return getInventory().getCurrentMass();
    }

    @Deprecated
    public int getMaxMass() {
        return getInventory().getMaxMass();
    }

    public int getRemainingCapacity() {
        return getInventory().getMaxMass() - getInventory().getCurrentMass();
    }
}
