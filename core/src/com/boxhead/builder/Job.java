package com.boxhead.builder;

import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public enum Job {
    UNEMPLOYED(null, Pair.of(Resource.NOTHING, 0)),
    LUMBERJACK(Harvestable.Type.TREE,
            Pair.of(Resource.WOOD, 1)),
    MINER_IRON(Harvestable.Type.IRON_ORE,
            Pair.of(Resource.IRON, 1)),
    BUILDER(ConstructionSite.class,
            Pair.of(Resource.NOTHING, 0)),
    SMELTER(null,
            Pair.of(Resource.STEEL, 3),
            Pair.of(Resource.IRON, -3),
            Pair.of(Resource.COAL, -1)),
    BLACKSMITH_IRON(null,
            Pair.of(Resource.TOOLS, 1),
            Pair.of(Resource.IRON, -2)),
    BLACKSMITH_STEEL(null,
            Pair.of(Resource.TOOLS, 1),
            Pair.of(Resource.STEEL, -1)),
    DOCTOR(null, Pair.of(Resource.NOTHING, 0));

    private final Object poi;

    private final Map<Resource, Integer> resourceChanges = new HashMap<>();

    @SafeVarargs
    Job(Object interest, Pair<Resource, Integer>... usedResources) {
        poi = interest;

        for (Pair<Resource, Integer> usedResource : usedResources) {
            if (resourceChanges.containsKey(usedResource.first))
                throw new IllegalArgumentException();

            resourceChanges.put(usedResource.first, usedResource.second);
        }
    }

    public Object getPoI() {
        return poi;
    }

    public Map<Resource, Integer> getResourceChanges() {
        return resourceChanges;
    }

    public boolean producesAnyResources() {
        return resourceChanges.entrySet().stream()
                .anyMatch(entry -> entry.getKey() != Resource.NOTHING && entry.getValue() > 0);
    }

    // todo: refactor to use getResourceChanges instead
    @Deprecated
    public Resource[] getResources() {
        return resourceChanges.keySet().toArray(new Resource[]{});
    }

    // todo: refactor to use getResourceChanges instead
    @Deprecated
    public Integer[] getChanges() {
        return resourceChanges.values().toArray(new Integer[]{});
    }
}
