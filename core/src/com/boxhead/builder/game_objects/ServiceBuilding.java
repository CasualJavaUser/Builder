package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Jobs;
import com.boxhead.builder.Services;
import com.boxhead.builder.utils.Vector2i;

public class ServiceBuilding extends ProductionBuilding {

    private final Services service;
    private int guestCapacity, guestsInside;
    private NPC[] guests;
    private int serviceCounter, serviceInterval;

    public ServiceBuilding(String name, TextureRegion texture, Jobs job, Services service, int employeeCapacity, int guestCapacity, Vector2i entrancePosition, int productionInterval, int serviceInterval) {
        super(name, texture, job, employeeCapacity, entrancePosition, productionInterval);
        this.service = service;
        this.guestCapacity = guestCapacity;
        this.serviceInterval = serviceInterval;
        guests = new NPC[guestCapacity];
    }

    /**
     * Removes the specified guest from the building.
     *
     * @param npc guest to be removed from the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean removeGuest(NPC npc) {
        if (guestsInside > 0) {
            for (int i = 0; i < guests.length; i++) {
                if (guests[i] == npc) {
                    guests[i] = null;
                    guestsInside--;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds the specified guest to the building.
     *
     * @param npc guest to be added to the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean addGuest(NPC npc) {
        if (guestsInside < guestCapacity) {
            for (int i = 0; i < guests.length; i++) {
                if (guests[i] == null) {
                    guests[i] = npc;
                    guestsInside++;
                    return true;
                }
            }
        }
        return false;
    }

    public void provideServices() {
        for (NPC guest : guests) {
            if (guest != null) {
                service.applyEffects(guest.getStats(), employeesInside);
            }
        }
    }

    public boolean provides(NPC.Stats stat) {
        int it = 0;
        for (NPC.Stats s : service.getStats()) {
            if (s == stat && service.getEffects()[it] > 0) {
                return true;
            }
            it++;
        }
        return false;
    }

    public int getGuestCapacity() {
        return guestCapacity;
    }

    public int getGuestsInside() {
        return guestsInside;
    }
}
