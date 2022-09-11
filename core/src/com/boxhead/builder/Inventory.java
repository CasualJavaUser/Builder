package com.boxhead.builder;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private final Map<Resource, Integer> resources = new HashMap<>();
    private final int resourceTypesCapacity;
    private final int resourceCapacityPerType;

    public Inventory(int resourceTypesCapacity, int resourceCapacityPerType) {
        if (resourceTypesCapacity < 1 || resourceCapacityPerType < 1)
            throw new IllegalArgumentException();

        this.resourceTypesCapacity = resourceTypesCapacity;
        this.resourceCapacityPerType = resourceCapacityPerType;
    }

    public int moveResourcesTo(Inventory otherInventory, Resource resource, int amount) {
        if (!hasResourceAmount(resource, amount))
            throw new IllegalArgumentException();

        int amountToMove = otherInventory.getAvailableCapacityFor(resource);

        if (amountToMove > 0) {
            this.take(resource, amountToMove);
            otherInventory.add(resource, amountToMove);
        }

        return amountToMove;
    }

    public void createNewResources(Resource resource, int amount) {
        if (getAvailableCapacityFor(resource) < amount)
            throw new IllegalArgumentException();
        add(resource, amount);
    }

    public int getAvailableCapacityFor(Resource resource) {
        if (!resources.containsKey(resource) && resources.size() == resourceTypesCapacity)
            return 0;
        else
            return resourceCapacityPerType - resources.getOrDefault(resource, 0);
    }

    public boolean hasResourceAmount(Resource resource, int amount) {
        return resources.containsKey(resource) && resources.get(resource) >= amount;
    }

    public int getResourceAmount(Resource resource) {
        return resources.getOrDefault(resource, 0);
    }

    public int getResourceCapacityPerType() {
        return resourceCapacityPerType;
    }

    private void add(Resource resource, int amount) {
        int currentAmount = resources.getOrDefault(resource, 0);

        if (currentAmount + amount > resourceCapacityPerType)
            throw new IllegalArgumentException();
        if (currentAmount == 0 && resourceTypesCapacity == resources.size())
            throw new IllegalArgumentException();

        resources.put(resource, currentAmount + amount);
    }

    private void take(Resource resource, int amount) {
        if (!hasResourceAmount(resource, amount))
            throw new IllegalArgumentException();

        int newAmount = resources.get(resource) - amount;

        if (newAmount > 0)
            resources.put(resource, newAmount);
        else {
            resources.remove(resource);
        }
    }
}
