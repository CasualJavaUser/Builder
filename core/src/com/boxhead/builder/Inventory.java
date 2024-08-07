package com.boxhead.builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class Inventory implements Serializable {

    @Serial
    private static final long serialVersionUID = 4L;
    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);
    private final int maxCapacity;
    private int currentAmount;
    private int displayedAmount;

    public Inventory(int maxCapacity) {
        if (maxCapacity < 1)
            throw new IllegalArgumentException();

        this.maxCapacity = maxCapacity;
        currentAmount = 0;
        displayedAmount = 0;
    }

    public enum Availability {
        AVAILABLE,
        FULL,
        FULL_OUTPUT,
        LACKS_INPUT
    }

    public void moveResourcesTo(Inventory otherInventory, Resource resource, int units) {
        if (getResourceAmount(resource) < units || otherInventory.getAvailableCapacity() < units
                || getAvailableCapacity() < -units || otherInventory.getResourceAmount(resource) < -units)
            throw new IllegalArgumentException();

        put(resource, -units);
        otherInventory.put(resource, units);
    }

    public void transferAllResourcesTo(Inventory otherInventory) {
        if (otherInventory.getAvailableCapacity() < currentAmount)
            throw new IllegalArgumentException();

        for (Resource resource : resources.keySet()) {
            int moved = resources.get(resource);

            take(resource, moved);
            otherInventory.put(resource, moved);
        }
    }

    public int getAvailableCapacity() {
        return maxCapacity - currentAmount;
    }

    public Availability checkStorageAvailability(Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            int inStorage = resources.getOrDefault(resource, 0);
            int change = recipe.getChange(resource);
            if (inStorage + change < 0)
                return Availability.LACKS_INPUT;

            if (change > getAvailableCapacity())
                return Availability.FULL_OUTPUT;
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
        return currentAmount == 0;
    }

    public boolean isFull() {
        return displayedAmount >= maxCapacity;
    }

    public void put(Resource resource, int units) {
        int currentUnits = resources.getOrDefault(resource, 0);

        if (currentAmount + units > maxCapacity || currentUnits + units < 0)
            throw new IllegalArgumentException("amount: " + (currentUnits + units) + " (" + resource.name() + ") fill: " + currentAmount + '/' + maxCapacity);

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

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public int getDisplayedAmount() {
        return displayedAmount;
    }

    public Set<Resource> getStoredResources() {
        return resources.keySet();
    }

    private void updateMass(Resource resource, int units) {
        currentAmount += units;
        if (resource != Resource.NOTHING) displayedAmount += units;
    }

    @Override
    public String toString() {
        String s = displayedAmount + " (" + currentAmount + ") / " + maxCapacity;
        for (Map.Entry<Resource, Integer> entry : resources.entrySet()) {
            s += entry.getKey().name().toLowerCase() + ": " + entry.getValue();
        }
        return s;
    }
}
