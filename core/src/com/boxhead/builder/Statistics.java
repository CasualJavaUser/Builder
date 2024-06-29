package com.boxhead.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Statistics {
    public static final int VALUES_PER_STAT = 30;
    private static final Map<Type, float[]> values = new HashMap<>();

    public enum Type {
        POPULATION(() -> World.getVillagers().size()),
        HAPPINESS(() -> (int) World.getAverageHappiness()),
        HEALTH(() -> (int) World.getAverage(Stat.HEALTH)),
        HUNGER(() -> (int) World.getAverage(Stat.HUNGER)),
        TIREDNESS(() -> (int) World.getAverage(Stat.TIREDNESS));

        private final Supplier<Integer> supplier;

        Type(Supplier<Integer> supplier) {
            this.supplier = supplier;
        }

        public int getValue() {
            return supplier.get();
        }
    }

    public static void init() {
        for (Type value : Type.values()) {
            values.put(value, new float[VALUES_PER_STAT]);
        }
    }

    public static void updateStatistics() {
        for (Type stat : values.keySet()) {
            float[] statValues = values.get(stat);
            System.arraycopy(statValues, 0, statValues, 1, statValues.length - 1);
            statValues[0] = stat.getValue();
        }
    }

    public static float[] getValues(Type type) {
        return values.get(type);
    }
}
