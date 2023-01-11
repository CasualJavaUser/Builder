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

    private Recipe(int initialCapacity) {
        changes = new HashMap<>(initialCapacity, 1f);
    }

    public int getChange(Resource resource) {
        if (!changes.containsKey(resource))
            return 0;

        return changes.get(resource);
    }

    public Recipe negate() {
        Recipe negative = new Recipe(changes.size());
        for (Resource resource : changes.keySet()) {
            negative.changes.put(resource, -changes.get(resource));
        }
        return negative;
    }

    public int sum() {
        int sum = 0;
        for (Integer change : changes.values()) {
            sum += change;
        }
        return sum;
    }

    public Set<Resource> changedResources() {
        return changes.keySet();
    }
}
