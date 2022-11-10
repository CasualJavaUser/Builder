package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class EnterableBuilding extends Building {
    /**
     * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
     */
    protected Vector2i entrancePosition;

    protected EnterableBuilding(String name, Buildings.Type type, Vector2i gridPosition) {
        super(name, type, gridPosition);
        entrancePosition = type.getEntrancePosition();
    }

    public Vector2i getEntrancePosition() {
        return gridPosition.add(entrancePosition);
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
