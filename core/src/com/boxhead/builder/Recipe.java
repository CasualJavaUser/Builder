package com.boxhead.builder;

import com.boxhead.builder.utils.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Recipe implements Iterable<Map.Entry<Resource, Integer>>, Cloneable {
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

    public Recipe negative() {
        Recipe negative = new Recipe(changes.size());
        for (Resource resource : changes.keySet()) {
            negative.changes.put(resource, -changes.get(resource));
        }
        return negative;
    }

    public Recipe half() {
        Recipe half = new Recipe(changes.size());
        for (Resource resource : changes.keySet()) {
            half.changes.put(resource, changes.get(resource) / 2);
        }
        return half;
    }

    /**
     * Rounds all resources down to the nearest 10
     */
    public void roundDown() {
        for (Resource resource : changes.keySet()) {
            int amount = changes.get(resource);
            changes.put(resource, amount - (amount % 10));
        }
    }

    public int sum() {
        int sum = 0;
        for (Integer change : changes.values()) {
            sum += change;
        }
        return sum;
    }

    public void add(Recipe otherRecipe) {
        for (Map.Entry<Resource, Integer> pair : otherRecipe) {
            Resource resource = pair.getKey();
            changes.put(resource, changes.getOrDefault(resource, 0) + pair.getValue());
            if (changes.get(resource) == 0) changes.remove(resource);
        }
    }

    public Map<Resource, Integer> getChanges() {
        return changes;
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
        s = s.substring(0, s.length() - 1);
        return s;
    }

    @Override
    public Iterator<Map.Entry<Resource, Integer>> iterator() {
        return changes.entrySet().iterator();
    }

    @Override
    public Recipe clone() {
        Recipe clone = new Recipe();
        clone.changes.putAll(changes);
        return clone;
    }
}
