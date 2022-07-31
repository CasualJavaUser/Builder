package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class ConstructionSite extends EnterableBuilding implements FieldWork {
    private int progress = 0;
    private final int totalLabour;
    private final Buildings.Types buildingType;
    private final NPC[] assigned = new NPC[4];
    private int assignedCount = 0, currentlyWorking = 0;

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
        for (int i = 0; i < assigned.length; i++) {
            if (assigned[i] == null) {
                assigned[i] = npc;
                assignedCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void dissociateWorker(NPC npc) {
        for (int i = 0; i < assigned.length; i++) {
            if (assigned[i] == npc) {
                assigned[i] = null;
                assignedCount--;
                return;
            }
        }
    }

    @Override
    public boolean isFree() {
        return assignedCount < assigned.length;
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
        for (NPC employee : assigned) {
            if (npc == employee) {
                if (b) currentlyWorking++;
                else currentlyWorking--;
                return;
            }
        }
    }
}