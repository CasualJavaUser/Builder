package com.boxhead.builder.game_objects;

import com.boxhead.builder.Inventory;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class StorageBuilding extends Building {
    /**
     * Absolute position of the tile from which NPCs can enter.
     */
    protected Vector2i entrancePosition;
    protected final Inventory inventory = new Inventory(200);
    protected final Inventory reservedInventory = new Inventory(200);

    public StorageBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        entrancePosition = gridPosition.add(type.entrancePosition);
    }

    public Vector2i getEntrancePosition() {
        return entrancePosition;
    }

    public static StorageBuilding getByCoordinates(Vector2i gridPosition) {
        for (Building building : World.getBuildings()) {
            if (building.getGridPosition().equals(gridPosition)) {
                return (StorageBuilding) building;
            }
        }
        return null;
    }

    public void reserveResources(Resource resource, int units) {
        if (inventory.getResourceAmount(resource) - reservedInventory.getResourceAmount(resource) >= units) {
            reservedInventory.put(resource, units);
        }
    }

    public boolean reserveSpace(int units) {
        if (inventory.getAvailableCapacity() >= units) {
            inventory.put(Resource.NOTHING, units);
            return true;
        }
        return false;
    }

    public void cancelReservation(int units) {
        inventory.put(Resource.NOTHING, -units);
    }

    public void moveReservedResourcesTo(Inventory otherInventory, Resource resource, int movedUnits, int reservedUnits) {
        if (movedUnits > 0) {   //move from this to other
            inventory.moveResourcesTo(otherInventory, resource, movedUnits);
            reservedInventory.put(resource, -reservedUnits);
        } else if (movedUnits < 0) {    //move from other to this
            inventory.put(Resource.NOTHING, -reservedUnits);
            inventory.moveResourcesTo(otherInventory, resource, movedUnits);
        }
    }

    public void putReservedResources(Resource resource, int units) {
        if (units > 0) {
            inventory.put(Resource.NOTHING, -units);
            inventory.put(resource, units);
        } else if (units < 0) {
            reservedInventory.put(resource, -units);
            inventory.put(resource, -units);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
