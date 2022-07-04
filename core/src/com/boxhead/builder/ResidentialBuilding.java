package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ResidentialBuilding extends FunctionalBuilding {

    public ResidentialBuilding(TextureRegion texture, int residentCapacity) {
        super(texture, Jobs.NULL, residentCapacity);
    }
}
