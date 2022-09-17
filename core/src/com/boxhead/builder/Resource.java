package com.boxhead.builder;

public enum Resource {
    NOTHING(1),
    WOOD(2),
    IRON(3),
    COAL(1),
    STEEL(3),
    TOOLS(2);

    public final int weight;

    Resource(int weight) {
        this.weight = weight;
    }
}
