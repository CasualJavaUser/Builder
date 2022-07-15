package com.boxhead.builder;

import com.boxhead.builder.utils.Pair;

public enum Jobs {
    UNEMPLOYED(new Pair<>(Resources.NOTHING, 0)),
    LUMBERJACK(new Pair<>(Resources.WOOD, 1)),
    SMELTER(new Pair<>(Resources.STEEL, 3),
            new Pair<>(Resources.IRON, -3),
            new Pair<>(Resources.COAL, -1)),
    BLACKSMITH_IRON(
            new Pair<>(Resources.TOOLS, 1),
            new Pair<>(Resources.IRON, -2)),
    BLACKSMITH_STEEL(
            new Pair<>(Resources.TOOLS, 1),
            new Pair<>(Resources.STEEL, -1)),
    DOCTOR(new Pair<>(Resources.NOTHING, 0));

    private final Resources[] resources;
    private final int[] change;

    @SafeVarargs
    Jobs(Pair<Resources, Integer>... usedResources) {
        resources = new Resources[usedResources.length];
        change = new int[usedResources.length];
        for (int i = 0; i < usedResources.length; i++) {
            resources[i] = usedResources[i].first;
            change[i] = usedResources[i].second;
        }
    }

    public Resources[] getResources() {
        return resources;
    }

    public int[] getChange() {
        return change;
    }
}
