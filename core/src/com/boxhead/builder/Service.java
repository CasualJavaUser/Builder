package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;

public class Service {

    public void applyEffects(int[] guestStats, int multiplier) {}

    public NPC.Stats[] getStats() {
        return null;
    }

    public int[] getEffects() {
        return null;
    }
}

/*public enum Service {
    HEAL(Pair.of(NPC.Stats.HEALTH, 1));

    private final NPC.Stats[] stats;
    private final int[] effect;

    @SafeVarargs
    Service(Pair<NPC.Stats, Integer>... effects) {
        stats = new NPC.Stats[effects.length];
        effect = new int[effects.length];
        for (int i = 0; i < effects.length; i++) {
            stats[i] = effects[i].first;
            effect[i] = effects[i].second;
        }
    }

    public void applyEffects(int[] npcStats, int multiplier) {
        for (int i = 0; i < stats.length; i++) {
            npcStats[stats[i].ordinal()] += effect[i] * multiplier;
        }
    }

    public NPC.Stats[] getStats() {
        return stats;
    }

    public int[] getEffects() {
        return effect;
    }
}*/
