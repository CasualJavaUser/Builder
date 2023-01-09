package com.boxhead.builder.game_objects;

import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ResidentialBuilding extends EnterableBuilding {

    private final Set<NPC> residents;

    public ResidentialBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        residents = new HashSet<>(type.npcCapacity, 1f);
    }

    public boolean addResident(NPC npc) {
        if (residents.size() < type.npcCapacity) {
            return residents.add(npc);
        }
        return false;
    }

    public boolean removeResident(NPC npc) {
        return residents.remove(npc);
    }

    public boolean hasFreePlaces() {
        return residents.size() < type.npcCapacity;
    }

    public int getResidentCapacity() {
        return type.npcCapacity;
    }

    public Set<NPC> getResidents() {
        return residents;
    }
}
