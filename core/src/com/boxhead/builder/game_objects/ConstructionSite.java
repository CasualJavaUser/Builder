package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class ConstructionSite extends Building implements FieldWork {
    private int progress = 0;
    private final int totalLabour, capacity = 1;    //(temp) capacity of 1 makes debugging easier
    private final Building building;
    private int currentlyWorking = 0;
    private final Map<NPC, Boolean> assigned = new HashMap<>(capacity, 1f);

    public ConstructionSite(String name, Vector2i gridPosition, Buildings.Type buildingType, int totalLabour) {
        super(name, buildingType.getConstructionSite(), gridPosition, null);
        building = Buildings.create(buildingType, gridPosition);
        this.totalLabour = totalLabour;
        collider = building.getCollider();
    }

    @Override
    public Object getCharacteristic() {
        return this.getClass();
    }

    @Override
    public void assignWorker(NPC npc) {
        if (assigned.size() < capacity) {
            assigned.put(npc, false);
        }
    }

    @Override
    public void dissociateWorker(NPC npc) {
        assigned.remove(npc);
        updateCurrentlyWorking();
    }

    @Override
    public boolean isFree() {
        return assigned.size() < capacity;
    }

    @Override
    public boolean isRemoved() {
        return progress >= totalLabour;
    }

    @Override
    public void work() {
        progress += currentlyWorking;

        if (progress >= totalLabour) {
            World.placeBuilding(building);

            for (NPC npc : assigned.keySet()) {
                npc.getWorkplace().dissociateFieldWork(npc);
                npc.giveOrder(NPC.Order.Type.GO_TO, npc.getWorkplace());
                npc.giveOrder(NPC.Order.Type.ENTER, npc.getWorkplace());
            }
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (assigned.containsKey(npc)) {
            assigned.replace(npc, b);
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

    public Building getBuilding() {
        return building;
    }
}
