package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

public class ResidentialBuilding extends EnterableBuilding {

    int residentCapacity, residentCount = 0;

    public ResidentialBuilding(TextureRegion texture, int residentCapacity, Vector2i entrancePosition) {
        super(texture, entrancePosition);
        this.residentCapacity = residentCapacity;
    }

    public boolean addResident() {
        if (residentCount < residentCapacity) {
            residentCount++;
            return true;
        }
        return false;
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
