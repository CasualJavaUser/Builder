package com.boxhead.builder.game_objects;

import com.boxhead.builder.Job;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

import java.util.Set;
import java.util.stream.Collectors;

public class PlantationBuilding extends FarmBuilding<Harvestable> {

    public PlantationBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
    }

    public Harvestables.Type getCrop() {
        return type.crop;
    }

    @Override
    public Resource getResource() {
        return type.crop.characteristic.resource;
    }

    @Override
    public int getYield() {
        return type.crop.yield;
    }

    /**
     * Returns true if a new FieldHarvestable can be created on the given tile.
     */
    public boolean isArable(Vector2i gridPosition) {
        if (!fieldCollider.overlaps(gridPosition) || !World.isBuildable(gridPosition))
            return false;

        for (Harvestable harvestable : ownFieldWorks) {
            if (harvestable.getGridPosition().equals(gridPosition))
                return false;
        }
        return true;
    }

    @Override
    public void endShift(Job.ShiftTime shift) {
        super.endShift(shift);
        Set<Harvestable> notPlanted = ownFieldWorks.stream().filter(h -> h.getCurrentPhase() < 0).collect(Collectors.toSet());
        ownFieldWorks.removeAll(notPlanted);
    }
}
