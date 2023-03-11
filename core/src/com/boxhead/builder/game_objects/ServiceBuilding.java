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
    private final Set<Villager> guests;
    private int serviceCounter;

    public ServiceBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.service = type.service;
        guests = new HashSet<>(type.guestCapacity, 1f);
    }

    /**
     * Removes the specified guest from the building.
     *
     * @param villager guest to be removed from the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean removeGuest(Villager villager) {
        return guests.remove(villager);
    }

    /**
     * Adds the specified guest to the building.
     *
     * @param villager guest to be added to the building
     * @return true if the array of guests changed as a result of the call
     */
    public boolean addGuest(Villager villager) {
        if (guests.size() < type.guestCapacity) {
            return guests.add(villager);
        }
        return false;
    }

    public void provideServices() {
        for (Villager guest : guests) {
            if (guest != null) {
                service.applyEffects(guest.getStats(), super.employeesInside);
            }
        }
    }

    public boolean provides(Villager.Stats stat) {
        int it = 0;
        for (Villager.Stats s : service.getStats()) {
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
        textureId = type.texture;
        job = type.job;
        service = type.service;
        instantiateIndicator();
    }
}
