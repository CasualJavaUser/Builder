package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Jobs;
import com.boxhead.builder.Resources;

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

    /**
     * Check if the resources can be taken from or put into storage.
     * @param job the job to be done
     * @return 1 if there is not enough space in storage, -1 if there are not enough resources, 0 if there is enough space and resources
     */
    public int checkStorageAvailability(Jobs job) {
        for (int i = 0; i < job.getResources().length; i++) {
            if (storage[job.getResources()[i].ordinal()] + job.getChange()[i] > maxStorage[job.getResources()[i].ordinal()]) return 1;
            else if (storage[job.getResources()[i].ordinal()] + job.getChange()[i] < 0) return -1;
        }
        return 0;
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