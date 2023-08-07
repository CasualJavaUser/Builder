package com.boxhead.builder;

import com.boxhead.builder.utils.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Recipe implements Iterable<Map.Entry<Resource, Integer>>, Serializable {
    private final Map<Resource, Integer> changes;

    @SafeVarargs
    public Recipe(Pair<Resource, Integer>... resources) {
        changes = new HashMap<>(resources.length);

        for (Pair<Resource, Integer> pair : resources) {
            changes.put(pair.first, pair.second);
        }
    }

    private Recipe(int initialCapacity) {
        changes = new HashMap<>(initialCapacity);
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

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    @Override
    public String toString() {
        String s = "";
        for (Resource resource : Resource.values()) {
            if (changes.get(resource) != null) {
                s = s.concat("- " + resource.name().toLowerCase() + ": " + changes.get(resource)) + "\n";
            }
        }
        s = s.substring(0, s.length()-1);
        return s;
    }

    @Override
    public Iterator<Map.Entry<Resource, Integer>> iterator() {
        return changes.entrySet().iterator();
    }
}
