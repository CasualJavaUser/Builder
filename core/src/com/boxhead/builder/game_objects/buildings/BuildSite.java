package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.Map;

public abstract class BuildSite extends Building implements FieldWork {
    protected int progress = 0;
    protected int totalLabour, capacity = 1;
    protected int currentlyWorking = 0;
    protected BoxCollider fieldCollider;
    protected final Map<Villager, Boolean> assigned = new HashMap<>(capacity);

    protected BuildSite(Building.Type type, Vector2i gridPosition, int totalLabour, BoxCollider fieldCollider) {
        super(type, type.getConstructionSite(), gridPosition, type.buildCost.sum());
        this.totalLabour = totalLabour;
        this.fieldCollider = fieldCollider;
    }

    protected BuildSite(Building building, int totalLabour, int storageCapacity) {
        super(building.type, building.type.texture, building.getGridPosition(), storageCapacity);
        this.totalLabour = totalLabour;
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
        villager.setAnimation(Villager.Animation.WALK);
    }

    @Override
    public void setWork(Villager villager) {
        if (assigned.containsKey(villager)) {
            assigned.replace(villager, true);
            updateCurrentlyWorking();
            villager.setAnimation(Villager.Animation.HAMMERING, gridPosition.x < villager.getGridPosition().x);
        }
    }

    private void updateCurrentlyWorking() {
        currentlyWorking = 0;
        for (Boolean working : assigned.values()) {
            if (working)
                currentlyWorking++;
        }
    }
}
