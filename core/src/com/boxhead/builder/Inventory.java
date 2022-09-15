package com.boxhead.builder;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);
    private int weight, maxWeight;

    public Inventory(int maxWeight) {
        if (maxWeight < 1)
            throw new IllegalArgumentException();

        this.maxWeight = maxWeight;
        weight = 0;
    }

    public int getWeight() {
        return weight;
    }

    public int moveResourcesTo(Inventory otherInventory, Resource resource, int amount) {
        if (!hasResourceAmount(resource, amount))
            throw new IllegalArgumentException();

        int amountToMove = otherInventory.getAvailableCapacityFor(resource);
        if(amountToMove > amount) amountToMove = amount;

        if (amountToMove > 0) {
            this.take(resource, amountToMove);
            otherInventory.put(resource, amountToMove);
        }

        return amountToMove;
    }

    public int getAvailableCapacityFor(Resource resource) {
        return (maxWeight - getWeight()) / resource.getWeight();
    }

    public boolean hasResourceAmount(Resource resource, int amount) {
        return resources.containsKey(resource) && resources.get(resource) >= amount;
    }

    public int getResourceAmount(Resource resource) {
        return resources.getOrDefault(resource, 0);
    }

    public void put(Resource resource, int amount) {
        int currentAmount = resources.getOrDefault(resource, 0);

        if (amount > getAvailableCapacityFor(resource))
            throw new IllegalArgumentException();

        resources.put(resource, currentAmount + amount);
        weight += amount * resource.getWeight();
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
        weight -= amount * resource.getWeight();
    }

    public int getMaxWeight() {
        return maxWeight;
    }
}
