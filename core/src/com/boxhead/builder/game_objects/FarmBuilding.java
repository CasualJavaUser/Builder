package com.boxhead.builder.game_objects;

import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FarmBuilding extends ProductionBuilding {

    private BoxCollider fieldCollider;
    private transient SortedList<Harvestable> ownHarvestables;

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

        for (Harvestable harvestable : ownHarvestables) {
            if (harvestable.getGridPosition().equals(gridPosition))
                return false;
        }
        return true;
    }

    public void addHarvestable(Harvestable harvestable) {
        ownHarvestables.add(harvestable);
    }

    public void removeHarvestable(Harvestable harvestable) {
        ownHarvestables.remove(harvestable);
    }

    public Optional<Harvestable> findWorkableField() {
        return ownHarvestables.stream().filter(Harvestable::isFree).findFirst();
    }

    @Override
    public void endWorkday() {
        super.endWorkday();
        Set<Harvestable> notPlanted = ownHarvestables.stream().filter(h -> h.getCurrentPhase() < 0).collect(Collectors.toSet());
        ownHarvestables.removeAll(notPlanted);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(ownHarvestables.size());
        for (Harvestable harvestable : ownHarvestables) {
            oos.writeObject(harvestable);
        }
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        int size = ois.readInt();
        ownHarvestables = new SortedList<>(Comparator.comparingLong(h -> h.getGridPosition().gridHash()));
        for (int i = 0; i < size; i++) {
            ownHarvestables.add((Harvestable) ois.readObject());
        }
    }
}
