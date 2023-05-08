package com.boxhead.builder.game_objects;

import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ResidentialBuilding extends StorageBuilding {

    private final Set<Villager> residents;

    public ResidentialBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        residents = new HashSet<>(type.npcCapacity, 1f);
    }

    public boolean addResident(Villager villager) {
        if (residents.size() < type.npcCapacity) {
            return residents.add(villager);
        }
        return false;
    }

    public boolean removeResident(Villager villager) {
        return residents.remove(villager);
    }

    public boolean hasFreePlaces() {
        return residents.size() < type.npcCapacity;
    }

    public int getResidentCapacity() {
        return type.npcCapacity;
    }

    public Set<Villager> getResidents() {
        return residents;
    }
}
