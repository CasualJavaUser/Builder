package com.boxhead.builder.game_objects;

import com.boxhead.builder.Service;
import com.boxhead.builder.Stat;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import static com.boxhead.builder.game_objects.Villager.Order.Type.EXIT;
import static com.boxhead.builder.game_objects.Villager.Order.Type.GO_TO;

public class ServiceBuilding extends ProductionBuilding {

    private transient Service service;
    private final Set<Villager> guests;
    private int reserved = 0;

    public ServiceBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.service = type.service;
        guests = new HashSet<>(type.guestCapacity);
    }

    public void provideServices() {
        for (Villager guest : guests) {
            if (guest != null) {
                service.applyEffects(guest, super.employeesInside);

                boolean isFulfilled = true;
                for (Stat stat : service.getEffects().keySet()) {
                    boolean condition;
                    if (stat.isIncreasing)
                        condition = guest.getStats()[stat.ordinal()] < 99; //TODO
                    else
                        condition = guest.getStats()[stat.ordinal()] > 1; //TODO

                    if (!condition) {
                        isFulfilled = false;
                    }
                }
                if (isFulfilled) {
                    guest.giveOrder(EXIT, this);
                    if (guest.getHome() != null) {
                        guest.giveOrder(GO_TO, guest.getHome());
                    }
                }
            }
        }
    }

    public void guestEnter(Villager villager) {
        if (canProvideService()) {
            guests.add(villager);
        }
    }

    public void reserve() {
        reserved++;
    }

    public void guestExit(Villager villager) {
        guests.remove(villager);
        reserved--;
    }

    public Set<Villager> getGuests() {
        return guests;
    }

    public boolean canProvideService() {
        return reserved < type.guestCapacity && employeesInside > 0 && !inventory.isEmpty();
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
