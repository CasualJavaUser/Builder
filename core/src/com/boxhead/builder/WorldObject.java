package com.boxhead.builder;

import com.boxhead.builder.utils.Vector2i;

import java.util.Comparator;

public interface WorldObject {
    Comparator<WorldObject> gridPositionComparator = Comparator.comparingLong(wo -> wo.getGridPosition().gridHash());

    Vector2i getGridPosition();
}
