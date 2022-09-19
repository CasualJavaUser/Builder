package com.boxhead.builder;

import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.utils.Vector2i;

/*public interface WorldObject extends Comparable<WorldObject> {
    Vector2i getGridPosition();
    @Override
    default int compareTo(WorldObject o) {
        return (o.getGridPosition().x + o.getGridPosition().y) - (getGridPosition().x + getGridPosition().y);
    }
}*/

public interface WorldObject {
    Vector2i getGridPosition();
}
