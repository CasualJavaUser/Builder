package com.boxhead.builder;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);
    private final int maxMass;
    private int currentMass;
    private int displayedMass;

    public Inventory(int maxWeight) {
        if (maxWeight < 1)
            throw new IllegalArgumentException();

        this.maxMass = maxWeight;
        currentMass = 0;
        displayedMass = 0;
    }

    public enum Availability {
        AVAILABLE,
        FULL,
        OUTPUT_FULL,
        LACKS_INPUT
    }

    public int moveResourcesTo(Inventory otherInventory, Resource resource, int units) {
        if (!hasResourceAmount(resource, units))
            throw new IllegalArgumentException();

        int amountToMove = otherInventory.getAvailableCapacityFor(resource);
        if (amountToMove > units) amountToMove = units;

        if (amountToMove > 0) {
            this.take(resource, amountToMove);
            otherInventory.put(resource, amountToMove);
        }

        return amountToMove;
    }

    /**
     * Moves as many units of resources as possible
     *
     * @return How many units were transferred
     */
    public int moveResourcesTo(Inventory otherInventory, Resource resource) {
        int moved = Math.min(resources.get(resource), otherInventory.getAvailableCapacityFor(resource));

        take(resource, moved);
        otherInventory.put(resource, moved);
        return moved;
    }

    public int getAvailableCapacityFor(Resource resource) {
        return (maxMass - currentMass) / resource.mass;
    }

    public Availability checkStorageAvailability(Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            int inStorage = resources.getOrDefault(resource, 0);
            int change = recipe.getChange(resource);
            if (inStorage + change < 0)
                return Availability.LACKS_INPUT;

            if (change > getAvailableCapacityFor(resource))
                return Availability.OUTPUT_FULL;
        }
        return Availability.AVAILABLE;
    }

    public boolean hasResourceAmount(Resource resource, int units) {
        return resources.containsKey(resource) && resources.get(resource) >= units;
    }

    public int getResourceAmount(Resource resource) {
        return resources.getOrDefault(resource, 0);
    }

    public boolean isEmpty() {
        return currentMass == 0;
    }

    public boolean isFull() {
        return displayedMass >= maxMass;
    }

    public void put(Resource resource, int units) {
        int currentUnits = resources.getOrDefault(resource, 0);

        if (units > getAvailableCapacityFor(resource))
            throw new IllegalArgumentException();

        resources.put(resource, currentUnits + units);
        updateMass(resource, units);
    }

    public void put(Recipe recipe) {
        if (checkStorageAvailability(recipe) != Availability.AVAILABLE) {
            throw new IllegalArgumentException();
        }
        for (Resource resource : recipe.changedResources()) {
            int inStorage = resources.getOrDefault(resource, 0);
            int change = recipe.getChange(resource);
            resources.put(resource, inStorage + change);
            updateMass(resource, change);
        }
    }

    private void take(Resource resource, int units) {
        if (!hasResourceAmount(resource, units))
            throw new IllegalArgumentException();

        int newAmount = resources.get(resource) - units;

        if (newAmount > 0)
            resources.put(resource, newAmount);
        else {
            resources.remove(resource);
        }
        updateMass(resource, -units);
    }

    public int getMaxMass() {
        return maxMass;
    }

    public int getCurrentMass() {
        return currentMass;
    }

    public int getDisplayedMass() {
        return displayedMass;
    }

    public int remainingCapacity() {
        return maxMass - currentMass;
    }

    private void updateMass(Resource resource, int units) {
        currentMass += units * resource.mass;
        if (resource != Resource.NOTHING) displayedMass += units * resource.mass;
    }
}
