package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum Resources {
    NOTHING,
    WOOD,
    IRON,
    COAL,
    STEEL,
    TOOLS;

    private static final int[] resourceStorage = new int[Resources.values().length];

    public static final int MAX_STORAGE = 1000;

    public static boolean checkStorageAvailability(Jobs job) {
        for (int i = 0; i < job.getResources().length; i++) {
            if (resourceStorage[job.getResources()[i].ordinal()] + job.getChange()[i] > MAX_STORAGE ||
                    resourceStorage[job.getResources()[i].ordinal()] + job.getChange()[i] < 0) {
                return false;
            }
        }
        return true;
    }

    public static void addProducts(Jobs recipe) {
        for (int i = 0; i < recipe.getResources().length; i++) {
            resourceStorage[recipe.getResources()[i].ordinal()] += recipe.getChange()[i];
        }
    }

    public int getAmount() {
        if (this != Resources.NOTHING) return resourceStorage[this.ordinal()];
        return 0;
    }
}
