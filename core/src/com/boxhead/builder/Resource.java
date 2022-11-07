package com.boxhead.builder;

public enum Resource {
    NOTHING(1),
    WOOD(2),
    IRON(3),
    COAL(1),
    STEEL(3),
    TOOLS(2);

    public final int mass;

    Resource(int mass) {
        this.mass = mass;
    }
}
