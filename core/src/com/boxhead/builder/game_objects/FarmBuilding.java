package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Recipe;
import com.boxhead.builder.Resource;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Comparator;
import java.util.Optional;

public abstract class FarmBuilding<T extends FieldWork> extends ProductionBuilding {

    protected BoxCollider fieldCollider;
    protected transient SortedList<T> ownFieldWorks;
    protected Recipe recipe;

    public FarmBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        fieldCollider = new BoxCollider();
        ownFieldWorks = new SortedList<>(Comparator.comparingLong(h -> h.getGridPosition().gridHash()));
        recipe = new Recipe(Pair.of(getResource(), Villager.INVENTORY_SIZE));
    }

    public void setFieldCollider(BoxCollider collider) {
        fieldCollider = collider;
    }

    public abstract Resource getResource();

    public abstract int getYield();

    public Recipe getRecipe() {
        return recipe;
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

    public void addFieldWork(T t) {
        ownFieldWorks.add(t);
    }

    public void removeHarvestable(T t) {
        ownFieldWorks.remove(t);
    }

    public Optional<T> findWorkableFieldWork() {
        return ownFieldWorks.stream().filter(FieldWork::isFree).findFirst();
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(ownFieldWorks.size());
        for (T t : ownFieldWorks) {
            oos.writeObject(t);
        }
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        int size = ois.readInt();
        ownFieldWorks = new SortedList<>(Comparator.comparingLong(h -> h.getGridPosition().gridHash()));
        for (int i = 0; i < size; i++) {
            ownFieldWorks.add((T) ois.readObject());
        }
    }
}
