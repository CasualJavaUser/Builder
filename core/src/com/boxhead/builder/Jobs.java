package com.boxhead.builder;

import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.utils.Pair;

import java.util.HashMap;
import java.util.Map;

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

    private final Object poi;

    private final Map<Resources, Integer> resourceChanges = new HashMap<>();

    @SafeVarargs
    Jobs(Object interest, Pair<Resources, Integer>... usedResources) {
        poi = interest;

        for (Pair<Resources, Integer> usedResource : usedResources) {
            if (resourceChanges.containsKey(usedResource.first))
                throw new IllegalArgumentException();

            resourceChanges.put(usedResource.first, usedResource.second);
        }
    }

    public Object getPoI() {
        return poi;
    }

    public Map<Resources, Integer> getResourceChanges() {
        return resourceChanges;
    }

    // todo: refactor to use getResourceChanges instead
    @Deprecated
    public Resources[] getResources() {
        return resourceChanges.keySet().toArray(new Resources[]{});
    }

    // todo: refactor to use getResourceChanges instead
    @Deprecated
    public Integer[] getChange() {
        return resourceChanges.values().toArray(new Integer[]{});
    }
}
