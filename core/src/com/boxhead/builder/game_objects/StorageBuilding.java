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

    public int getStoredWeight() {
        return getInventory().getCurrentWeight();
    }

    public int getMaxWeight() {
        return getInventory().getMaxWeight();
    }

    public int getRemainingCapacity() {
        return inventory.getMaxWeight() - inventory.getCurrentWeight();
    }
}
