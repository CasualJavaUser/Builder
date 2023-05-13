package com.boxhead.builder;

import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public enum Service {
    BARTENDING(Pair.of(Stat.HUNGER, -0.02f)),
    HEALTHCARE(Pair.of(Stat.HEALTH, 0.02f));

    private final Map<Stat, Float> effects = new HashMap<>();

    @SafeVarargs
    Service(Pair<Stat, Float>... effects) {
        for (Pair<Stat, Float> effect : effects) {
            this.effects.put(effect.first, effect.second);
        }
    }

    public void applyEffects(Villager villager, int multiplier) {
        for (Stat stat : effects.keySet()) {
            float villagerStat = villager.getStats()[stat.ordinal()];
             if (villagerStat >= -0.1 && villagerStat <= 100.1) {
                 villager.getStats()[stat.ordinal()] += (effects.get(stat) * multiplier) - stat.rate;
             }
        }
    }

    public Map<Stat, Float> getEffects() {
        return effects;
    }
}
