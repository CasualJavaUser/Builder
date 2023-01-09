package com.boxhead.builder.game_objects;

import com.boxhead.builder.Service;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

public class ServiceBuilding extends ProductionBuilding {

    private transient Service service;
    private final Set<NPC> guests;
    private int serviceCounter;

    public ServiceBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.service = type.service;
        guests = new HashSet<>(type.guestCapacity, 1f);
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
        if (guests.size() < type.guestCapacity) {
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

    public int getGuestsInside() {
        return guests.size();
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Buildings.Type.valueOf(ois.readUTF());
        texture = type.getTexture();
        job = type.job;
        service = type.service;
        instantiateIndicator();
    }
}
