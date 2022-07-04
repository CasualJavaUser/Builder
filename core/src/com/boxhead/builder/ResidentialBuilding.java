package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ResidentialBuilding extends Building {

    int residentCapacity, residentCount = 0;

    public ResidentialBuilding(TextureRegion texture, int residentCapacity) {
        super(texture);
        this.residentCapacity = residentCapacity;
    }
}
