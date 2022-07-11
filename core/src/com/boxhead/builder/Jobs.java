package com.boxhead.builder;

public enum Jobs {
    NULL(Resources.NOTHING),
    UNEMPLOYED(Resources.NOTHING),
    LUMBERJACK(Resources.WOOD),
    DOCTOR(Resources.NOTHING);

    private final Resources product;

    Jobs(Resources product) {
        this.product = product;
    }

    public Resources getProduct() {
        return product;
    }
}
