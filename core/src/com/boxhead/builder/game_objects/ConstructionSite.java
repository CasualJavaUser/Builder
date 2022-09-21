package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ConstructionSite extends Building implements FieldWork {
    private int progress = 0;
    private final int totalLabour, capacity = 1;    //(temp) capacity of 1 makes debugging easier
    private Building building;
    private int currentlyWorking = 0;
    private final Set<NPC> assigned = new HashSet<>(capacity, 1f);

    public ConstructionSite(String name, Vector2i gridPosition, Buildings.Type buildingType, int totalLabour) {
        super(name, buildingType.getConstructionSite(), gridPosition);
        building = Buildings.get(buildingType, gridPosition);
        building.setCollider(collider);
        this.totalLabour = totalLabour;

        switch (buildingType) {
            default: collider = getDefaultCollider();
        }
    }

    @Override
    public Object getCharacteristic() {
        return this.getClass();
    }

    @Override
    public boolean assignWorker(NPC npc) {
        if (assigned.size() < capacity) {
            return assigned.add(npc);
        }
        return false;
    }

    @Override
    public void dissociateWorker(NPC npc) {
        assigned.remove(npc);
    }

    @Override
    public boolean isFree() {
        return assigned.size() < capacity;
    }

    @Override
    public void work() {
        progress += currentlyWorking;

        if (progress >= totalLabour) {
            World.getBuildings().remove(this);
            World.placeBuilding(building);

            for (NPC npc : assigned) {
                if (npc != null) {
                    npc.getWorkplace().dissociateFieldWork(npc);
                    npc.giveOrder(NPC.Order.Type.GO_TO, npc.getWorkplace());
                    npc.giveOrder(NPC.Order.Type.ENTER, npc.getWorkplace());
                }
            }
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (assigned.contains(npc)) {
            if (b) currentlyWorking++;
            else currentlyWorking--;
        }
    }

    public Building getBuilding() {
        return building;
    }
}
