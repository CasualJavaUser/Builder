package com.boxhead.builder;

public enum Resource {
    NOTHING(0),
    WOOD(2),
    IRON(3),
    COAL(1),
    STEEL(3),
    TOOLS(2);

    int weight;
    Resource(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
