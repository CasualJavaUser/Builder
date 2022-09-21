package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Job;
import com.boxhead.builder.Service;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ServiceBuilding extends ProductionBuilding {

    private final Service service;
    private final int guestCapacity;
    private final Set<NPC> guests;
    private int serviceCounter, serviceInterval;

    public ServiceBuilding(String name, TextureRegion texture, Vector2i gridPosition, Job job, Service service, int employeeCapacity, int guestCapacity, Vector2i entrancePosition, int productionInterval, int serviceInterval) {
        super(name, texture, gridPosition, job, employeeCapacity, entrancePosition, productionInterval);
        this.service = service;
        this.guestCapacity = guestCapacity;
        this.serviceInterval = serviceInterval;
        guests = new HashSet<>(guestCapacity, 1f);
    }

    /**
     * Removes the specified guest from the building.
     *
     * @param npc guest to be removed from the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean removeGuest(NPC npc) {
        return guests.remove(npc);
    }

    /**
     * Adds the specified guest to the building.
     *
     * @param npc guest to be added to the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean addGuest(NPC npc) {
        if (guests.size() < guestCapacity) {
            return guests.add(npc);
        }
        return false;
    }

    public void provideServices() {
        for (NPC guest : guests) {
            if (guest != null) {
                service.applyEffects(guest.getStats(), super.employeesInside);
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
        return guests.size();
    }
}
