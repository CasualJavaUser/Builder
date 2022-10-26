package com.boxhead.builder.ui;

public interface Clickable {
    default boolean isClicked() {return false;};
    default boolean isHeld() {return false;}
    default boolean isUp() {return false;}
    default void onClick() {};
    default void onHold() {}
    default void onUp() {}
}
