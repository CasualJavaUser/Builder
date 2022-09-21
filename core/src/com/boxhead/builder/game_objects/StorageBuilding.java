package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Job;
import com.boxhead.builder.Resource;
import com.boxhead.builder.utils.Vector2i;

import java.util.Map;

public class StorageBuilding extends Building {

    public StorageBuilding(String name, TextureRegion texture, Vector2i gridPosition) {
        super(name, texture, gridPosition);
    }

    public boolean isFull(Resource resource) {
        return getInventory().getAvailableCapacityFor(resource) == 0;
    }

    public void addToStorage(Job recipe) {
        recipe.getResourceChanges()
                .forEach((resources, amount) -> getInventory().put(resources, amount));  //createNewResources(resources, amount));
    }

    /**
     * Check if the resources can be taken from or put into storage.
     *
     * @param job the job to be done
     * @return 1 if there is not enough space in storage, -1 if there are not enough resources, 0 if there is enough space and resources
     */
    public int checkStorageAvailability(Job job) {
        for (Map.Entry<Resource, Integer> resourceChange : job.getResourceChanges().entrySet()) {
            Resource resource = resourceChange.getKey();
            int amount = resourceChange.getValue();

            if (amount > 0 && getInventory().getAvailableCapacityFor(resource) < amount)
                return 1;
            if (amount < 0 && !getInventory().hasResourceAmount(resource, amount))
                return -1;
        }
        return 0;
    }

    public int getStored(Resource resource) {
        return getInventory().getResourceAmount(resource);
    }

    public int getStoredWeight() {
        return getInventory().getCurrentWeight();
    }

    public int getMaxWeight() {
        return getInventory().getMaxWeight();
    }
}
