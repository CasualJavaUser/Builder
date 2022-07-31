package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class ResidentialBuilding extends EnterableBuilding {

    private int residentCapacity, residentCount = 0, residentsInside = 0;
    private NPC[] residents;

    public ResidentialBuilding(String name, TextureRegion texture, int residentCapacity, Vector2i entrancePosition) {
        super(name, texture, entrancePosition);
        this.residentCapacity = residentCapacity;
        residents = new NPC[residentCapacity];
    }

    public boolean addResident(NPC npc) {
        if (residentCount < residentCapacity) {
            for (int i = 0; i < residentCapacity; i++) {
                if (residents[i] == null) {
                    residents[i] = npc;
                    residentCount++;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeResident(NPC npc) {
        if (residentCount > 0) {
            for (int i = 0; i < residentCapacity; i++) {
                if (residents[i] == npc) {
                    residents[i] = null;
                    residentCount--;
                    return true;
                }
            }
        }
        return false;
    }

    public int getResidentCapacity() {
        return residentCapacity;
    }

    public int getResidentCount() {
        return residentCount;
    }
}
