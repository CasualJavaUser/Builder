package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ResidentialBuilding extends EnterableBuilding {

    private final int residentCapacity;
    private final Set<NPC> residents;

    public ResidentialBuilding(String name, TextureRegion texture, int residentCapacity, Vector2i entrancePosition) {
        super(name, texture, entrancePosition);
        this.residentCapacity = residentCapacity;
        residents = new HashSet<>(residentCapacity, 1f);
    }

    public boolean addResident(NPC npc) {
        if (residents.size() < residentCapacity) {
            return residents.add(npc);
        }
        return false;
    }

    public boolean removeResident(NPC npc) {
        return residents.remove(npc);
    }

    public boolean hasFreePlaces() {
        return residents.size() < residentCapacity;
    }

    public int getResidentCapacity() {
        return residentCapacity;
    }

    public Set<NPC> getResidents() {
        return residents;
    }
}
