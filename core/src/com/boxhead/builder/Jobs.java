package com.boxhead.builder;

import com.boxhead.builder.utils.Pair;

public enum Jobs {
    UNEMPLOYED(null, new Pair<>(Resources.NOTHING, 0)),
    LUMBERJACK(Harvestable.Types.TREE,
            new Pair<>(Resources.WOOD, 1)),
    MINER_IRON(Harvestable.Types.IRON_ORE,
            new Pair<>(Resources.IRON, 1)),
    BUILDER(ConstructionSite.class,
            new Pair<>(Resources.NOTHING, 0)),
    SMELTER(null,
            new Pair<>(Resources.STEEL, 3),
            new Pair<>(Resources.IRON, -3),
            new Pair<>(Resources.COAL, -1)),
    BLACKSMITH_IRON(null,
            new Pair<>(Resources.TOOLS, 1),
            new Pair<>(Resources.IRON, -2)),
    BLACKSMITH_STEEL(null,
            new Pair<>(Resources.TOOLS, 1),
            new Pair<>(Resources.STEEL, -1)),
    DOCTOR(null, new Pair<>(Resources.NOTHING, 0));

    private final Resources[] resources;
    private final int[] change;
    private final Object poi;

    @SafeVarargs
    Jobs(Object interest, Pair<Resources, Integer>... usedResources) {
        poi = interest;
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

    public Object getPoI() {
        return poi;
    }
}
