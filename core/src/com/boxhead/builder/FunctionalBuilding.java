package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

public class FunctionalBuilding extends Building {

    protected Jobs job;
    protected int employeeCapacity, guestCapacity, employees = 0, guests = 0;

    public FunctionalBuilding(Texture texture, Jobs job, int employeeCapacity, int guestCapacity) {
        super(texture);
        this.job = job;
        this.employeeCapacity = employeeCapacity;
        this.guestCapacity = guestCapacity;
    }

    public FunctionalBuilding(Texture texture, Jobs job, int employeeCapacity) {
        this(texture, job, employeeCapacity, 0);
    }
}
