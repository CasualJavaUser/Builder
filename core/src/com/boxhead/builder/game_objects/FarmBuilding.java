package com.boxhead.builder.game_objects;

import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.util.Comparator;
import java.util.Optional;

public class FarmBuilding extends ProductionBuilding {

    private BoxCollider fieldCollider;
    private final SortedList<FieldHarvestable> ownHarvestables;

    public FarmBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        fieldCollider = new BoxCollider();
        ownHarvestables = new SortedList<>(Comparator.comparingLong(h -> h.getGridPosition().gridHash()));
    }

    public void setFieldCollider(BoxCollider collider) {
        fieldCollider = collider;
    }

    public BoxCollider getFieldCollider() {
        return fieldCollider;
    }

    public int getFieldWidth() {
        return fieldCollider.getWidth();
    }

    public int getFieldHeight() {
        return fieldCollider.getHeight();
    }

    public Vector2i getFieldGridPosition() {
        return fieldCollider.getGridPosition();
    }

    /**
     * Returns true if a new FieldHarvestable can be created on the given tile.
     */
    public boolean isArable(Vector2i gridPosition) {
        if (!fieldCollider.overlaps(gridPosition) || !World.isBuildable(gridPosition))
            return false;

        for (FieldHarvestable harvestable : ownHarvestables) {
            if (harvestable.getGridPosition().equals(gridPosition))
                return false;
        }
        return true;
    }

    public void addFieldHarvestable(FieldHarvestable harvestable) {
        ownHarvestables.add(harvestable);
    }

    public void removeFieldHarvestable(FieldHarvestable harvestable) {
        ownHarvestables.remove(harvestable);
    }

    public Optional<FieldHarvestable> findWorkableField() {
        return ownHarvestables.stream().filter(FieldHarvestable::isFree).findFirst();
    }
}
