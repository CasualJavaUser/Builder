package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class ResidentialBuilding extends EnterableBuilding {

    private int residentCapacity, residentCount = 0, residentsInside = 0;
    private NPC[] residents;

    public ResidentialBuilding(String name, TextureRegion texture, int residentCapacity, Vector2i entrancePosition) {
        super(name, texture, entrancePosition);
        this.residentCapacity = residentCapacity;
    }

    public boolean addResident(NPC npc) {
        /*if (residentCount < residentCapacity) {
            residentCount++;
            return true;
        }
        return false;*/
        boolean b = false;
        if (residentCount < residentCapacity) {
            for (int i = 0; i < residents.length; i++) {
                if (residents[i] == null) {
                    residents[i] = npc;
                    residentCount++;
                    b = true;
                    break;
                }
            }
        }
        return b;
    }

    public boolean removeResident() {
        if (residentCount > 0) {
            residentCount--;
            return true;
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
