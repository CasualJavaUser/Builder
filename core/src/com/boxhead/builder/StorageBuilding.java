package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Arrays;

public class StorageBuilding extends Building {
    private final int[] storage = new int[Resources.values().length];
    private final int[] maxStorage = new int[Resources.values().length];

    public StorageBuilding(String name, TextureRegion texture) {
        super(name, texture);
        Arrays.fill(maxStorage, 100);  //TODO set maxStorage
    }

    public boolean isFull(Resources resource) {
        return storage[resource.ordinal()] == maxStorage[resource.ordinal()];
    }

    public void setMaxStorage(Resources resource, int amount) {
        maxStorage[resource.ordinal()] = amount;
    }
    /*
    /**
     * Adds the given amount of the given resource to storage.
     * @param resource the resource to be added
     * @param amount the amount to be added
     * @return the amount of the given resource that couldn't be added to storage
     *
    public int addToStorage(Resources resource, int amount) {
        int amountLeft = 0;
        storage[resource.ordinal()] += amount;
        if(storage[resource.ordinal()] > maxStorage[resource.ordinal()]) {
            amountLeft = storage[resource.ordinal()] - maxStorage[resource.ordinal()];
            storage[resource.ordinal()] = maxStorage[resource.ordinal()];
        }
        return amountLeft;
    }

    /**
     * Removes no more than the given amount of the given resource from storage.
     * @param resource the resource to be removed
     * @param amount the amount to be removed
     * @return the amount of the given resource that was removed from storage
     *
    public int getFromStorage(Resources resource, int amount) {
        int removed = amount;
        storage[resource.ordinal()] -= amount;
        if (storage[resource.ordinal()] < 0) {
            removed += storage[resource.ordinal()];
            storage[resource.ordinal()] = 0;
        }
        return removed;
    }*/

    public void addToStorage(Jobs recipe) {
        for (int i = 0; i < recipe.getResources().length; i++) {
            storage[recipe.getResources()[i].ordinal()] += recipe.getChange()[i];
        }
    }

    public boolean checkStorageAvailability(Jobs job) {
        for (int i = 0; i < job.getResources().length; i++) {
            if (storage[job.getResources()[i].ordinal()] + job.getChange()[i] > maxStorage[job.getResources()[i].ordinal()] ||
                    storage[job.getResources()[i].ordinal()] + job.getChange()[i] < 0) {
                return false;
            }
        }
        return true;
    }

    public int getStored(Resources resource) {
        return storage[resource.ordinal()];
    }

    public int getStored(int ordinal) {
        return storage[ordinal];
    }

    public int getMaxStorage(Resources resource) {
        return maxStorage[resource.ordinal()];
    }

    public int getMaxStorage(int ordinal) {
        return maxStorage[ordinal];
    }
}
