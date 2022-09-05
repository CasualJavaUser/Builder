package com.boxhead.builder;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private final Map<Resources, Integer> resources = new HashMap<>();
    private final int resourceTypesCapacity;
    private final int resourceCapacityPerType;

    public Inventory(int resourceTypesCapacity, int resourceCapacityPerType) {
        if (resourceTypesCapacity < 1 || resourceCapacityPerType < 1)
            throw new IllegalArgumentException();

        this.resourceTypesCapacity = resourceTypesCapacity;
        this.resourceCapacityPerType = resourceCapacityPerType;
    }

    public int moveResourcesFrom(Inventory otherInventory, Resources resourceType, int amount) {
        if (!otherInventory.hasResourceAmount(resourceType, amount))
            throw new IllegalArgumentException();

        int amountToMove = getAvailableCapacityFor(resourceType);

        if (amountToMove > 0) {
            otherInventory.substract(resourceType, amountToMove);
            this.add(resourceType, amountToMove);
        }

        return amountToMove;
    }

    public void createNewResources(Resources resourceType, int amount) {
        if (getAvailableCapacityFor(resourceType) < amount)
            throw new IllegalArgumentException();
        add(resourceType, amount);
    }

    public int getAvailableCapacityFor(Resources resourceType) {
        if (!resources.containsKey(resourceType) && resources.size() == resourceTypesCapacity)
            return 0;
        else
            return resourceCapacityPerType - resources.getOrDefault(resourceType, 0);
    }

    public boolean hasResourceAmount(Resources resourceType, int amount) {
        return resources.containsKey(resourceType) && resources.get(resourceType) >= amount;
    }

    public int getResourceAmount(Resources resourceType) {
        return resources.getOrDefault(resourceType, 0);
    }

    public int getResourceCapacityPerType() {
        return resourceCapacityPerType;
    }

    private void add(Resources resourceType, int amount) {
        int currentAmount = resources.getOrDefault(resourceType, 0);

        if (currentAmount + amount > resourceCapacityPerType)
            throw new IllegalArgumentException();
        if (currentAmount == 0 && resourceTypesCapacity == resources.size())
            throw new IllegalArgumentException();

        resources.put(resourceType, currentAmount + amount);
    }

    private void substract(Resources resourceType, int amount) {
        if (!hasResourceAmount(resourceType, amount))
            throw new IllegalArgumentException();

        int newAmount = resources.get(resourceType) - amount;

        if (newAmount > 0)
            resources.put(resourceType, newAmount);
        else {
            resources.remove(resourceType);
        }
    }
}
