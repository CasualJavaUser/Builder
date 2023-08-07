package com.boxhead.builder.game_objects;

import com.boxhead.builder.Jobs;
import com.boxhead.builder.Stat;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashSet;
import java.util.Set;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class ServiceBuilding extends ProductionBuilding {

    private final Set<Villager> guests;
    private int reserved = 0;

    public ServiceBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        guests = new HashSet<>(type.guestCapacity);
    }

    public void provideServices() {
        for (Villager employee : employees) {
            if (employee.isClockedIn()) {
                type.service.applyEffects(employee, 1);
            }
        }
        for (Villager guest : guests) {
            if (guest != null) {
                type.service.applyEffects(guest, super.employeesInside);

                boolean isReadyToLeave = true;
                for (Stat stat : type.service.getEffects().keySet()) {
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
        return reserved < type.guestCapacity;
    }

    public boolean canProvideService() {
        return employeesInside > 0/* && !inventory.isEmpty()*/;
    }
}
