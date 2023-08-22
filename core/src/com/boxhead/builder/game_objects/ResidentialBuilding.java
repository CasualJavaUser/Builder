package com.boxhead.builder.game_objects;

import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ResidentialBuilding extends StorageBuilding {
    private final Set<Villager> residents;

    public ResidentialBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        residents = new HashSet<>(type.familyCapacity);
    }

    public boolean addResident(Villager villager) {
        return residents.add(villager);
    }

    public boolean removeResident(Villager villager) {
        return residents.remove(villager);
    }

    public boolean isEmpty() {
        return residents.isEmpty();
    }

    public Set<Villager> getResidents() {
        return residents;
    }
}
