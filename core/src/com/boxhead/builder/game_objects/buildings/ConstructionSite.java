package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Logistics;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class ConstructionSite extends Building implements FieldWork {
    private int progress = 0;
    private final int totalLabour, capacity = 1;    //(temp) capacity of 1 makes debugging easier
    private int currentlyWorking = 0;
    private BoxCollider fieldCollider;
    private final Map<Villager, Boolean> assigned = new HashMap<>(capacity, 1f);

    public ConstructionSite(Building.Type type, Vector2i gridPosition, int totalLabour) {
        this(type, gridPosition, totalLabour, new BoxCollider());
    }

    public ConstructionSite(Building.Type type, Vector2i gridPosition, int totalLabour, BoxCollider fieldCollider) {
        super(type, type.getConstructionSite(), gridPosition, type.buildCost.sum());
        this.totalLabour = totalLabour;
        this.fieldCollider = fieldCollider;

        Logistics.requestTransport(this, type.buildCost.negate(), Logistics.USE_STORAGE);
        reserveSpace(type.buildCost.sum());
    }

    @Override
    public String getName() {
        return "construction site\n(" + type.name + ")";
    }

    @Override
    public Object getCharacteristic() {
        return this.getClass();
    }

    @Override
    public void assignWorker(Villager villager) {
        if (assigned.size() < capacity) {
            assigned.put(villager, false);
        } else
            throw new IllegalArgumentException("Assignment over capacity");
    }

    @Override
    public void dissociateWorker(Villager villager) {
        assigned.remove(villager);
        updateCurrentlyWorking();
    }

    @Override
    public boolean isFree() {
        return inventory.isFull() && assigned.size() < capacity;
    }

    @Override
    public void work() {
        if ((float) progress / totalLabour < (float) inventory.getDisplayedAmount() / inventory.getMaxCapacity())
            progress += currentlyWorking;

        if (progress >= totalLabour) {
            World.removeFieldWorks(this);
            if (type instanceof FarmBuilding.Type farmType)
                World.placeFarm(farmType, gridPosition, fieldCollider);
            else
                World.placeBuilding(type, gridPosition);

            for (Villager villager : assigned.keySet()) {
                villager.getWorkplace().dissociateFieldWork(villager);
                villager.giveOrder(Villager.Order.Type.GO_TO, villager.getWorkplace());
            }
            World.updateStoredResources(type.buildCost.negate());
        }
    }

    @Override
    public void setWork(Villager villager, boolean b) {
        if (assigned.containsKey(villager)) {
            assigned.replace(villager, b);
            updateCurrentlyWorking();
        }
    }

    private void updateCurrentlyWorking() {
        currentlyWorking = 0;
        for (Boolean working : assigned.values()) {
            if (working)
                currentlyWorking++;
        }
    }

    public void setFieldCollider(BoxCollider fieldCollider) {
        this.fieldCollider = fieldCollider;
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = type.getConstructionSite();
    }
}
