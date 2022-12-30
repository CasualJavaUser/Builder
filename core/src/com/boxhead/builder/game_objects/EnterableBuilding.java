package com.boxhead.builder.game_objects;

import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class EnterableBuilding extends Building {
    /**
     * Absolute position of the tile from which NPCs can enter.
     */
    protected Vector2i entrancePosition;

    protected EnterableBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        entrancePosition = gridPosition.add(type.entrancePosition);
    }

    public Vector2i getEntrancePosition() {
        return entrancePosition;
    }

    public static EnterableBuilding getByCoordinates(Vector2i gridPosition) {
        for (Building building : World.getBuildings()) {
            if (building.getGridPosition().equals(gridPosition)) {
                return (EnterableBuilding) building;
            }
        }
        return null;
    }
}
