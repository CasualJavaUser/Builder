package com.boxhead.builder.ui;

public interface Clickable {
    default void onClick() {};
    default void onHold() {}
    default void onUp() {}
    boolean isMouseOver();
}
