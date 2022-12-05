package com.boxhead.builder.ui;

public interface Clickable {
    default Clickable onClick() {return this;}
    default void onHold() {}
    default void onUp() {}
    boolean isMouseOver();
}
