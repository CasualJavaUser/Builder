package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.Recipe;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

public class ResidentialBuilding extends Building {
    public static class Type extends Building.Type {
        protected static Type[] values;

        public static final Type LOG_CABIN = new Type(
                Textures.Building.LOG_CABIN,
                "log cabin",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 4, 2),
                new Recipe(Pair.of(Resource.WOOD, 20))
        );

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost) {
            super(texture, name, entrancePosition, relativeCollider, buildCost);
        }

        public static Type[] values() {
            return values;
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException();
        }
    }

    private final Set<Villager> residents;

    public ResidentialBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        residents = new HashSet<>();
    }

    @Override
    public void emptyOccupants() {
        for (Villager resident : residents) {
            if (resident.isInBuilding(this)) resident.giveOrder(Villager.Order.Type.EXIT, this);
        }
    }

    @Override
    public Type getType() {
        return ((Type) type);
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
