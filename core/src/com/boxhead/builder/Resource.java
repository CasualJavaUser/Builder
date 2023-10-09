package com.boxhead.builder;

import com.boxhead.builder.game_objects.buildings.Building;
import com.boxhead.builder.ui.UI;

import java.util.Map;

public enum Resource {
    NOTHING,
    WOOD,
    STONE,
    IRON,
    COAL,
    STEEL,
    TOOLS,
    GRAIN,
    ALCOHOL,
    MILK,
    MEAT,
    FISH;

    /**
     * Resources that are free to use
     */
    public static final int[] storedResources = new int[values().length];

    public static void updateStoredResources(Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            storedResources[resource.ordinal()] += recipe.getChange(resource);
            UI.getResourceList().updateData(resource);
        }
    }

    public static void updateStoredResources(Resource resource, int amount) {
        storedResources[resource.ordinal()] += amount;
        UI.getResourceList().updateData(resource);
    }

    public static int getStored(Resource resource) {
        return storedResources[resource.ordinal()];
    }

    public static boolean canAfford(Recipe recipe) {
        for (Map.Entry<Resource, Integer> resource : recipe) {
            if (storedResources[resource.getKey().ordinal()] < resource.getValue()) return false;
        }
        return true;
    }

    /**
     * Calling this function without .canAfford() returning true may cause undefined behaviour
     */
    public static void takeFromStorage(Recipe recipe) {
        Recipe remaining = recipe.clone();

        for (Building storage : Logistics.getStorages()) {
            for (Resource resource : remaining.changedResources()) {
                int available = storage.getFreeResources(resource);
                int required = remaining.getChange(resource);

                if (available > 0) {
                    int transfer = Math.min(available, required);
                    storage.getInventory().put(resource, -transfer);
                    remaining.getChanges().put(resource, remaining.getChange(resource) - transfer);
                }
            }
        }
        updateStoredResources(recipe.negative());
    }
}
