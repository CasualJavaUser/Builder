package com.boxhead.builder.ui;

public interface Clickable {
    public boolean isClicked();
    public default boolean isHeld() {return false;}
    public void onClick();
    public default void onHold() {}
}
