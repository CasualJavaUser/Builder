package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Jobs;
import com.boxhead.builder.Resources;

import java.util.Map;

public class StorageBuilding extends Building {

    public StorageBuilding(String name, TextureRegion texture) {
        super(name, texture);
    }

    public boolean isFull(Resources resource) {
        return getInventory().getAvailableCapacityFor(resource) == 0;
    }

    public void addToStorage(Jobs recipe) {
        recipe.getResourceChanges()
                .forEach((resources, amount) -> getInventory().createNewResources(resources, amount));
    }

    /**
     * Check if the resources can be taken from or put into storage.
     *
     * @param job the job to be done
     * @return 1 if there is not enough space in storage, -1 if there are not enough resources, 0 if there is enough space and resources
     */
    public int checkStorageAvailability(Jobs job) {
        for (Map.Entry<Resources, Integer> resourceChange : job.getResourceChanges().entrySet()) {
            Resources resource = resourceChange.getKey();
            int amount = resourceChange.getValue();

            if (amount > 0 && getInventory().getAvailableCapacityFor(resource) < amount)
                return 1;
            if (amount < 0 && !getInventory().hasResourceAmount(resource, amount))
                return -1;
        }
        return 0;
    }

    public int getStored(Resources resource) {
        return getInventory().getResourceAmount(resource);
    }

    public int getMaxStorage(Resources resource) {
        return getInventory().getResourceCapacityPerType();
    }
}
