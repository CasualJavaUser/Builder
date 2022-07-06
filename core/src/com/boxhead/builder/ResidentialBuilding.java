package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ResidentialBuilding extends EnterableBuilding {

    int residentCapacity, residentCount = 0;

    public ResidentialBuilding(TextureRegion texture, int residentCapacity, Vector2i entrancePosition) {
        super(texture, entrancePosition);
        this.residentCapacity = residentCapacity;
    }
}
