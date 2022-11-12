package com.boxhead.builder;

import com.boxhead.builder.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Recipe {
    private final Map<Resource, Integer> changes;

    @SafeVarargs
    public Recipe(Pair<Resource, Integer>... resources) {
        changes = new HashMap<>(resources.length, 1f);

        for (Pair<Resource, Integer> pair : resources) {
            changes.put(pair.first, pair.second);
        }
    }

    public int getChange(Resource resource) {
        if (!changes.containsKey(resource))
            return 0;

        return changes.get(resource);
    }

    public Set<Resource> changedResources() {
        return changes.keySet();
    }
}
