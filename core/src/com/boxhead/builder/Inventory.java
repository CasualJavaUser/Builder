package com.boxhead.builder;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);
    private final int maxWeight;
    private int currentWeight;

    public Inventory(int maxWeight) {
        if (maxWeight < 1)
            throw new IllegalArgumentException();

        this.maxWeight = maxWeight;
        currentWeight = 0;
    }

    public enum Availability {
        AVAILABLE,
        FULL,
        OUTPUT_FULL,
        LACKS_INPUT
    }

    public int moveResourcesTo(Inventory otherInventory, Resource resource, int amount) {
        if (!hasResourceAmount(resource, amount))
            throw new IllegalArgumentException();

        int amountToMove = otherInventory.getAvailableCapacityFor(resource);
        if (amountToMove > amount) amountToMove = amount;

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
        return (maxWeight - currentWeight) / resource.weight;
    }

    /*public Availability checkStorageAvailability(Job recipe) {
        for (Resource resource : recipe.getResourceChanges().keySet()) {
            int change = recipe.getResourceChanges().get(resource);
            if (resources.getOrDefault(resource, 0) + change < 0)
                return Availability.LACKS_INPUT;

            if (change > getAvailableCapacityFor(resource))
                return Availability.OUTPUT_FULL;
        }
        return Availability.AVAILABLE;
    }*/

    public boolean hasResourceAmount(Resource resource, int amount) {
        return resources.containsKey(resource) && resources.get(resource) >= amount;
    }

    public int getResourceAmount(Resource resource) {
        return resources.getOrDefault(resource, 0);
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public boolean isEmpty() {
        return currentWeight == 0;
    }

    public void put(Resource resource, int amount) {
        int currentAmount = resources.getOrDefault(resource, 0);

        if (amount > getAvailableCapacityFor(resource))
            throw new IllegalArgumentException();

        resources.put(resource, currentAmount + amount);
        currentWeight += amount * resource.weight;
    }

    /*public void put(Job recipe) {
        if (checkStorageAvailability(recipe) != Availability.AVAILABLE) {
            throw new IllegalArgumentException();
        }
        for (Resource resource : recipe.getResourceChanges().keySet()) {
            int currentAmount = resources.getOrDefault(resource, 0);
            int change = recipe.getResourceChanges().get(resource);
            resources.put(resource, currentAmount + change);
            currentWeight += change * resource.weight;
        }
    }*/

    private void take(Resource resource, int amount) {
        if (!hasResourceAmount(resource, amount))
            throw new IllegalArgumentException();

        int newAmount = resources.get(resource) - amount;

        if (newAmount > 0)
            resources.put(resource, newAmount);
        else {
            resources.remove(resource);
        }
        currentWeight -= amount * resource.weight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public int remainingCapacity() {
        return maxWeight - currentWeight;
    }
}
