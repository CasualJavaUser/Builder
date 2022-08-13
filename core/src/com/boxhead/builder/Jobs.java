package com.boxhead.builder;

import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.utils.Pair;

public enum Jobs {
    UNEMPLOYED(null, Pair.of(Resources.NOTHING, 0)),
    LUMBERJACK(Harvestable.Types.TREE,
            Pair.of(Resources.WOOD, 1)),
    MINER_IRON(Harvestable.Types.IRON_ORE,
            Pair.of(Resources.IRON, 1)),
    BUILDER(ConstructionSite.class,
            Pair.of(Resources.NOTHING, 0)),
    SMELTER(null,
            Pair.of(Resources.STEEL, 3),
            Pair.of(Resources.IRON, -3),
            Pair.of(Resources.COAL, -1)),
    BLACKSMITH_IRON(null,
            Pair.of(Resources.TOOLS, 1),
            Pair.of(Resources.IRON, -2)),
    BLACKSMITH_STEEL(null,
            Pair.of(Resources.TOOLS, 1),
            Pair.of(Resources.STEEL, -1)),
    DOCTOR(null, Pair.of(Resources.NOTHING, 0));

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
