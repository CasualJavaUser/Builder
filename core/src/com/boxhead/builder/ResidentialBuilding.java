package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

public class ResidentialBuilding extends FunctionalBuilding {

    public ResidentialBuilding(Texture texture, int residentCapacity) {
        super(texture, Jobs.REST, residentCapacity);
    }
}
