package com.boxhead.builder.ui;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Clickable {
    AtomicBoolean isClicked = new AtomicBoolean(false);
    default void onClick() {};
    default void onHold() {}
    default void onUp() {}
    boolean isMouseOver();
}
