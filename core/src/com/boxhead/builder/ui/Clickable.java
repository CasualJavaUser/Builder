package com.boxhead.builder.ui;

public interface Clickable {
    boolean isClicked();
    default boolean isHeld() {return false;}
    void onClick();
    default void onHold() {}
}
