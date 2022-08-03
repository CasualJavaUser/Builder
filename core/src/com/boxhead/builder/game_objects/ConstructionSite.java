package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ConstructionSite extends EnterableBuilding implements FieldWork {
    private int progress = 0;
    private final int totalLabour, capacity = 1;    //(temp) capacity of 1 makes debugging easier
    private final Buildings.Types buildingType;
    private int currentlyWorking = 0;
    private final Set<NPC> assigned = new HashSet<>(capacity, 1f);

    public ConstructionSite(String name, Buildings.Types building, int totalLabour) {
        super(name, building.getTexture(), new Vector2i(0, -1));   //todo add texture to buildings atlas
        this.buildingType = building;
        this.totalLabour = totalLabour;
    }

    @Override
    public Object getCharacteristic() {
        return this.getClass();
    }

    @Override
    public boolean assignWorker(NPC npc) {
        if (assigned.size() < 4) {
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
            World.placeBuilding(buildingType, super.gridPosition);

            for (NPC npc : assigned) {
                if (npc != null) {
                    npc.navigateTo(npc.getWorkplace());
                    npc.setDestination(NPC.Pathfinding.Destination.WORK);
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
}