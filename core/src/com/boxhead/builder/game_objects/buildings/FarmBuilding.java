package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;
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
    public static class Type extends ProductionBuilding.Type {
        protected static Type[] values;

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost, Job job, int maxEmployeeCapacity) {
            super(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity);
        }

        public static Type[] values() {
            return values;
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException();
        }
    }

    public static final int MIN_FIELD_SIZE = 3, MAX_FIELD_SIZE = 12;

    protected BoxCollider fieldCollider;
    protected transient SortedList<T> ownFieldWorks;
    protected transient Recipe recipe;

    public FarmBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        fieldCollider = new BoxCollider();
        ownFieldWorks = new SortedList<>(Comparator.comparingLong(h -> h.getGridPosition().gridHash()));
        recipe = new Recipe(Pair.of(getDefaultResource(), Villager.INVENTORY_SIZE));
    }

    @Override
    public Type getType() {
        return ((Type) type);
    }

    public void setFieldCollider(BoxCollider collider) {
        fieldCollider = collider;
    }

    public abstract Resource getResource();

    public abstract Resource getDefaultResource();

    public abstract int getYield();

    public Recipe getRecipe() {
        return recipe;
    }

    public BoxCollider getFieldCollider() {
        return fieldCollider;
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
        return ownFieldWorks.stream().filter(FieldWork::isFree).min(WorldObject.gridPositionComparator);
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
        recipe = new Recipe(Pair.of(getResource(), Villager.INVENTORY_SIZE));
    }
}
