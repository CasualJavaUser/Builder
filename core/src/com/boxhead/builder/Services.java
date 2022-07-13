package com.boxhead.builder;

import com.sun.tools.javac.util.Pair;

public enum Services {
    HEAL(new Pair<>(NPC.Stats.HEALTH, 1));

    private final NPC.Stats[] stats;
    private final int[] effect;

    @SafeVarargs
    Services(Pair<NPC.Stats, Integer>... effects) {
        stats = new NPC.Stats[effects.length];
        effect = new int[effects.length];
        for (int i = 0; i < effects.length; i++) {
            stats[i] = effects[i].fst;
            effect[i] = effects[i].snd;
        }
    }

    public void applyEffects(int[] npcStats) {
        for (int i = 0; i < stats.length; i++) {
            npcStats[stats[i].ordinal()] += effect[i];
        }
    }

    public NPC.Stats[] getStats() {
        return stats;
    }

    public int[] getEffects() {
        return effect;
    }
}
