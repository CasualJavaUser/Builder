package com.boxhead.builder.game_objects;

import com.boxhead.builder.Inventory;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class StorageBuilding extends Building {
    /**
     * Absolute position of the tile from which NPCs can enter.
     */
    protected final Vector2i entrancePosition;
    protected final Inventory inventory;
    private final Map<Pair<Villager, Resource>, Integer> reservations = new HashMap<>();
    private final Map<Resource, Integer> reservedTotals = new HashMap<>();

    public StorageBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        entrancePosition = gridPosition.add(type.entrancePosition);
        inventory = new Inventory(200);
    }

    public StorageBuilding(Buildings.Type type, Textures.TextureId texture, Vector2i gridPosition, int storageCapacity) {
        super(type, texture, gridPosition);
        entrancePosition = gridPosition.add(type.entrancePosition);
        inventory = new Inventory(storageCapacity);
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
        reserveResources(null, resource, units);
    }

    public void reserveResources(Villager reservee, Resource resource, int units) {
        if (getFreeResources(resource) >= units) {
            updateReservations(reservee, resource, units);
        }
    }

    public boolean reserveSpace(int units) {
        return reserveSpace(null, units);
    }

    public boolean reserveSpace(Villager reservee, int units) {
        if (inventory.getAvailableCapacity() >= reservedTotals.getOrDefault(Resource.NOTHING, 0) + units) {
            updateReservations(reservee, Resource.NOTHING, units);
            return true;
        }
        return false;
    }

    public void cancelReservation(int units) {
        Pair<Villager, Resource> pair = Pair.of(null, Resource.NOTHING);
        Integer currentlyReserved = reservations.get(pair);
        if (currentlyReserved == null || currentlyReserved < units)
            throw new IllegalArgumentException("cancelling reservation that wasn't made");

        updateReservations(null, Resource.NOTHING, -units);
    }

    public void cancelReservation(Villager reservee) {
        Pair<Villager, Resource> pair = Pair.of(reservee, Resource.NOTHING);
        Integer reservedUnits = reservations.get(pair);
        if (reservedUnits == null)
            throw new IllegalArgumentException("cancelling reservation that wasn't made");

        updateReservations(reservee, Resource.NOTHING, -reservedUnits);
    }

    public void transferReservationOwnership(Villager currentReservee, Villager newReservee, Resource resource, int units) {
        Pair<Villager, Resource> pair = Pair.of(currentReservee, resource);
        Integer reservedUnits = reservations.get(pair);

        if (reservedUnits == null || reservedUnits < units)
            throw new IllegalArgumentException();

        if (reservedUnits > units)
            reservations.put(pair, reservedUnits - units);
        else
            reservations.remove(pair);

        pair = Pair.of(newReservee, resource);
        reservedUnits = reservations.get(pair);
        if (reservedUnits == null)
            reservations.put(pair, units);
        else
            reservations.put(pair, reservedUnits + units);
    }

    public void moveReservedResources(Villager reservee, Inventory source, Inventory destination, Resource resource, int movedUnits) {
        source.moveResourcesTo(destination, resource, movedUnits);
        if (this.inventory == source) {
            updateReservations(reservee, resource, -movedUnits);
        } else if (this.inventory == destination) {
            updateReservations(reservee, Resource.NOTHING, -movedUnits);
        } else throw new IllegalArgumentException();
    }

    public boolean hasReserved(Villager villager, Resource resource) {
        return reservations.containsKey(Pair.of(villager, resource));
    }

    public int getFreeResources(Resource resource) {
        return inventory.getResourceAmount(resource) - reservedTotals.getOrDefault(resource, 0);
    }

    public int getFreeSpace() {
        return inventory.getAvailableCapacity() - reservedTotals.getOrDefault(Resource.NOTHING, 0);
    }

    public int getReservedBy(Villager reservee, Resource resource) {
        Pair<Villager, Resource> pair = Pair.of(reservee, resource);
        return reservations.getOrDefault(pair, 0);
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void updateReservations(Villager reservee, Resource resource, int units) {
        Pair<Villager, Resource> pair = Pair.of(reservee, resource);
        int reserved = reservations.getOrDefault(pair, 0) + units;
        if (reserved == 0)
            reservations.remove(pair);
        else
            reservations.put(pair, reserved);

        reserved = reservedTotals.getOrDefault(resource, 0) + units;
        if (reserved == 0)
            reservedTotals.remove(resource);
        else
            reservedTotals.put(resource, reserved);
    }
}
