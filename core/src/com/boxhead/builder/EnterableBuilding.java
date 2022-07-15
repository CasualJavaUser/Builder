package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class EnterableBuilding extends Building {
    /**
     * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
     */
    protected Vector2i entrancePosition;

    protected EnterableBuilding(String name, TextureRegion texture, Vector2i entrancePosition) {
        super(name, texture);
        this.entrancePosition = entrancePosition;
    }

    public Vector2i getEntrancePosition() {
        return position.addScalar(entrancePosition);
    }

    public static EnterableBuilding getByCoordinates(Vector2i gridPosition) {
        for (Building building : World.getBuildings()) {
            if (building.getPosition().equals(gridPosition)) {
                return (EnterableBuilding) building;
            }
        }
        return null;
    }
}
