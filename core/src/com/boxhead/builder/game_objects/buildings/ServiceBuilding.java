package com.boxhead.builder.game_objects.buildings;

import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class ServiceBuilding extends ProductionBuilding {
    public static class Type extends ProductionBuilding.Type {
        public final Service service;
        public final int guestCapacity, serviceInterval;

        protected static Type[] values;

        public static final Type PUB = new Type(
                Textures.Building.PUB,
                "pub",
                new Vector2i(3, -1),
                new BoxCollider(0, 0, 5, 3),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.BARTENDER,
                2,
                200,
                Service.BARTENDING,
                20,
                5
        );
        public static final Type HOSPITAL = new Type(
                Textures.Building.HOSPITAL,
                "hospital",
                new Vector2i(4, -1),
                new BoxCollider(0, 0, 9, 4),
                new Recipe(Pair.of(Resource.WOOD, 30)),
                Jobs.DOCTOR,
                3,
                Service.HEALTHCARE,
                10,
                100
        );

        static {
            Arrays.fill(HOSPITAL.shifts, true);
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                    Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, int range,
                    Service service, int guestCapacity, int serviceInterval,
                    Function<Set<Building>, Float> updateEfficiency
        ) {
            super(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, productionInterval, range, updateEfficiency);
            this.service = service;
            this.guestCapacity = guestCapacity;
            this.serviceInterval = serviceInterval;
        }

        protected Type(
                Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, Service service,
                int guestCapacity, int serviceInterval
        ) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, productionInterval, 0, service, guestCapacity, serviceInterval, (b) -> 1f);
        }

        protected Type(
                Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                Recipe buildCost, Job job, int maxEmployeeCapacity, Service service, int guestCapacity,
                int serviceInterval
        ) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, 0, 0, service, guestCapacity, serviceInterval, (b) -> 1f);
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

    private final Set<Villager> guests;
    private int reserved = 0;

    public ServiceBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        guests = new HashSet<>(type.guestCapacity);
    }


    @Override
    public void emptyOccupants() {
        super.emptyOccupants(); //ProductionBuilding

        for (Villager guest : guests) {
            if (guest.isInBuilding(this)) guest.giveOrder(EXIT, this);
        }
    }

    @Override
    public Type getType() {
        return ((Type) type);
    }

    public void provideServices() {
        for (Villager employee : employees) {
            if (employee.isClockedIn()) {
                getType().service.applyEffects(employee, 1);
            }
        }
        for (Villager guest : guests) {
            if (guest != null) {
                getType().service.applyEffects(guest, super.employeesInside);

                boolean isReadyToLeave = true;
                for (Stat stat : getType().service.getEffects().keySet()) {
                    int acceptableStage;
                    if (guest.isWorkTime()) {
                        acceptableStage = stat.mild;
                    } else {
                        acceptableStage = stat.isIncreasing ? 0 : 100;
                    }
                    boolean condition;
                    if (stat.isIncreasing)
                        condition = guest.getStats()[stat.ordinal()] < acceptableStage;
                    else
                        condition = guest.getStats()[stat.ordinal()] > acceptableStage;

                    if (!condition) {
                        isReadyToLeave = false;
                        break;
                    }
                }
                if (isReadyToLeave) {
                    guest.giveOrder(EXIT, this);
                    if (guest.getJob() != Jobs.UNEMPLOYED && guest.isWorkTime()) {    //back to work
                        guest.giveOrder(GO_TO, guest.getWorkplace());
                        guest.giveOrder(CLOCK_IN);
                    } else if (guest.getHome() != null) {
                        guest.giveOrder(GO_TO, guest.getHome());
                    }
                }
            }
        }
    }

    public void guestEnter(Villager villager) {
        guests.add(villager);
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

    public boolean hasFreeSpaces() {
        return reserved < getType().guestCapacity;
    }

    public boolean canProvideService() {
        return employeesInside > 0/* && !inventory.isEmpty()*/;
    }
}
