package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ServiceBuilding extends ProductionBuilding {

    private Services service;
    private int guestCapacity, guestCount;
    private NPC[] guests;

    public ServiceBuilding(TextureRegion texture, Jobs job, Services service, int employeeCapacity, int guestCapacity, Vector2i entrancePosition) {
        super(texture, job, employeeCapacity, entrancePosition);
        this.service = service;
        this.guestCapacity = guestCapacity;
        guests = new NPC[guestCapacity];
    }

    /**
     * Removes the specified guest from the building.
     * @param npc guest to be removed from the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean removeGuest(NPC npc) {
        boolean b = false;
        if (employeeCount > 0) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == npc) {
                    employees[i] = null;
                    employeeCount--;
                    b = true;
                    break;
                }
            }
        }
        return b;
    }

    /**
     * Adds the specified guest to the building.
     * @param npc guest to be added to the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean addGuest(NPC npc) {
        boolean b = false;
        if (employeeCount < employeeCapacity) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == null) {
                    employees[i] = npc;
                    employeeCount++;
                    b = true;
                    break;
                }
            }
        }
        return b;
    }
}
