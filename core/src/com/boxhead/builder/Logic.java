package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.Pathfinding;

import java.util.Optional;

public class Logic {
    public static final float NORMAL_SPEED = 0.005f;
    public static final float SPEED_X2 = 0.0025f;
    public static final float SPEED_X3 = 0.00125f;

    private static boolean isPaused = false;
    private static float tickSpeed = NORMAL_SPEED;

    private static final Timer.Task task = new Timer.Task() {
        @Override
        public void run() {
            World.incrementTime();
            dailyCycle();
            while (!Harvestable.timeTriggers.isEmpty() && Harvestable.timeTriggers.get(0).first <= World.getDate()) {
                Harvestable.timeTriggers.remove(0).second.nextPhase();
            }
            produceResources();
            for (Villager villager : World.getVillagers()) {
                villager.executeOrders();
                villager.incrementAge();
                villager.progressStats();
            }
            for (Animal animal : World.getAnimals()) {
                animal.wander();
                if (animal instanceof FarmAnimal) {
                    ((FarmAnimal) animal).respawn();
                }
            }
        }
    };

    private static final Timer.Task intermittentTask = new Timer.Task() {
        @Override
        public void run() {
            Logistics.pairRequests();
            for (Villager villager : World.getVillagers()) {
                villager.seekJob();
                villager.seekHouse();
                villager.fulfillNeeds();
            }
        }
    };

    private static void dailyCycle() {
        switch (World.getTime()) {  //todo make it not-hardcoded
            case 10800:
                endWorkday(Job.ShiftTime.SEVEN_THREE);
                startWorkday(Job.ShiftTime.THREE_ELEVEN);
                break;
            case 28800:
                endWorkday(Job.ShiftTime.MIDNIGHT_EIGHT);
                startWorkday(Job.ShiftTime.EIGHT_FOUR);
                break;
            case 39600:
                startWorkday(Job.ShiftTime.ELEVEN_SEVEN);
                break;
            case 57600:
                endWorkday(Job.ShiftTime.EIGHT_FOUR);
                startWorkday(Job.ShiftTime.FOUR_MIDNIGHT);
                break;
            case 68400:
                endWorkday(Job.ShiftTime.ELEVEN_SEVEN);
                startWorkday(Job.ShiftTime.SEVEN_THREE);
                break;
            case 0:
                endWorkday(Job.ShiftTime.FOUR_MIDNIGHT);
                startWorkday(Job.ShiftTime.MIDNIGHT_EIGHT);
                Pathfinding.removeUnusedPaths();
        }
    }

    private static void startWorkday(Job.ShiftTime shift) {
        for (Building building : World.getBuildings()) {
            if (building instanceof ProductionBuilding workplace) {
                workplace.startShift(shift);
            }
        }
    }

    private static void endWorkday(Job.ShiftTime shift) {
        for (Building building : World.getBuildings()) {
            if (building instanceof ProductionBuilding workplace) {
                workplace.endShift(shift);
            }
        }
    }

    private static void produceResources() {
        for (Building building : World.getBuildings()) {
            if (building instanceof ProductionBuilding && ((ProductionBuilding) building).getJob().getPoI() != null &&
                    ((ProductionBuilding) building).canProduce()) {
                Optional<FieldWork> fieldWork = FieldWork.findFieldWork(((ProductionBuilding) building).getJob().getPoI(), ((StorageBuilding) building).getEntrancePosition());
                fieldWork.ifPresent(work -> Logistics.requestFieldWork((ProductionBuilding) building, work));
            }
        }

        for (ProductionBuilding transportOffice : Logistics.getTransportOffices()) {
            if (transportOffice.hasEmployeesInside()) {
                Optional<Logistics.Order> order = Logistics.findOrder(transportOffice.getEntrancePosition());
                order.ifPresent(o -> Logistics.takeOrder(transportOffice, o));
            }
        }
        for (int i = 0; i < World.getBuildings().size(); i++) {
            Building building = World.getBuildings().get(i);

            if (building instanceof ServiceBuilding) {
                ((ServiceBuilding) building).provideServices();
            }
            if (building instanceof ProductionBuilding workplace) {
                workplace.business();
                for (Object fieldWork : workplace.getAssignedFieldWork().values().toArray()) {
                    ((FieldWork) fieldWork).work();
                }
            }
        }

        World.removeFieldWorks();
        Logistics.clearFieldWorkRequests();
        Logistics.clearOrderRequests();
    }

    public static void init() {
        Timer.instance().scheduleTask(task, 0, NORMAL_SPEED);
        Timer.instance().scheduleTask(intermittentTask, 1f, 1f);
    }

    public static float getTickSpeed() {
        return tickSpeed;
    }

    public static void setTickSpeed(float tickSpeed) {
        if (tickSpeed == 0) {
            isPaused = true;
            Timer.instance().stop();
            return;
        }
        isPaused = false;
        Logic.tickSpeed = tickSpeed;
        Timer.instance().clear();
        Timer.instance().scheduleTask(task, 0, tickSpeed);
        Timer.instance().scheduleTask(intermittentTask, 0f, 1f);
        Timer.instance().start();
    }

    public static void pause(boolean pause) {
        isPaused = pause;
        if (pause) Timer.instance().stop();
        else Timer.instance().start();
    }

    public static boolean isPaused() {
        return isPaused;
    }
}
