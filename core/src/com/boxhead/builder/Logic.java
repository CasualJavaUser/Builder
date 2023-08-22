package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;
import com.boxhead.builder.game_objects.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class Logic {
    public static final float NORMAL_SPEED = 0.005f;
    public static final float SPEED_X2 = 0.0025f;
    public static final float SPEED_X3 = 0.00125f;

    private static boolean isPaused = false;
    private static float tickSpeed = NORMAL_SPEED;

    /**
     * All the ShiftTimes ordered by their start times (which needs not be the case for ShiftTime.values()).
     */
    private static final Job.ShiftTime[] orderedShifts = Arrays.stream(Job.ShiftTime.values()).sorted(Comparator.comparingInt(shift -> shift.start)).toArray(Job.ShiftTime[]::new);
    private static int nextShift = 0;
    private static int currentShift = 0;

    private static final Timer.Task task = new Timer.Task() {
        @Override
        public void run() {
            if (World.getTime() == orderedShifts[nextShift].start) {
                currentShift = (nextShift + orderedShifts.length - 2) % orderedShifts.length;
                startWorkday(orderedShifts[nextShift]);
                endWorkday(orderedShifts[currentShift]);
                nextShift = (nextShift + 1) % orderedShifts.length;
            }
            while (!Harvestable.timeTriggers.isEmpty() && Harvestable.timeTriggers.get(0).first <= World.getDate()) {
                Harvestable.timeTriggers.remove(0).second.nextPhase();
            }

            produceResources();

            if (World.getTime() == 0) {
                reproduceVillagers();
            }

            for (int i = 0; i < World.getVillagers().size(); i++) {
                Villager villager = World.getVillagers().get(i);
                villager.executeOrders();
                villager.incrementAge();
                villager.progressStats();

                if (villager.ageInTicks() == Villager.RETIREMENT_AGE * World.YEAR) {
                    villager.retire();
                }
                else if (villager.ageInYears() >= Villager.RETIREMENT_AGE) {
                    if (World.getDate() > villager.getDayOfDecease()) {
                        villager.die();
                    }
                }
                else if (villager.ageInYears() > Villager.WORKING_AGE) {
                    if (!villager.hasJob() && World.getTime() == orderedShifts[currentShift].end) {
                        villager.seekJobOrSchool();
                    } else if (villager.hasWorkplace() && World.getTime() == villager.getWorkplace().getShift(villager).getShiftTime().end) {
                        villager.seekJobOrSchool();
                    } else if (villager.hasSchool() && World.getTime() == villager.getSchool().getStudentShift(villager).getShiftTime().end) {
                        villager.seekJobOrSchool();
                    }
                }
            }

            for (Animal animal : World.getAnimals()) {
                animal.wander();
                if (animal instanceof FarmAnimal) {
                    ((FarmAnimal) animal).respawn();
                }
            }

            World.incrementTime();
        }
    };

    private static final Timer.Task intermittentTask = new Timer.Task() {
        @Override
        public void run() {
            Logistics.pairRequests();
            for (Villager villager : World.getVillagers()) {
                if (villager.getPartner() == null && villager.ageInYears() >= Villager.AGE_OF_CONSENT && villager.ageInYears() < Villager.INFERTILITY_AGE) {
                    villager.findPartner();
                }

                villager.seekHouse();
                villager.fulfillNeeds();
            }
        }
    };

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

    public static void alignShifts() {
        while (!(World.getTime() > orderedShifts[(nextShift + orderedShifts.length - 1) % orderedShifts.length].start
                && World.getTime() < orderedShifts[nextShift].start)) {
            int wrappingIndex = (nextShift + orderedShifts.length - 2) % orderedShifts.length;
            startWorkday(orderedShifts[nextShift]);
            endWorkday(orderedShifts[wrappingIndex]);
            nextShift = (nextShift + 1) % orderedShifts.length;
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

            if (building instanceof ServiceBuilding service) {
                service.provideServices();
            }
            if (building instanceof ProductionBuilding workplace) {
                workplace.business();
                for (Object fieldWork : workplace.getAssignedFieldWork().values().toArray()) {
                    ((FieldWork) fieldWork).work();
                }
            }
            if (building instanceof SchoolBuilding school) {
                school.teach();
            }
        }

        World.removeFieldWorks();
        Logistics.clearFieldWorkRequests();
        Logistics.clearOrderRequests();
    }

    private static void reproduceVillagers() {
        for (Villager villager : World.getVillagers()) {
            if (villager.getPartner() != null &&
                    villager.getGender() &&
                    villager.getHome() != null &&
                    !villager.isLivingWithParents() &&
                    villager.getHome().equals(villager.getPartner().getHome()) &&
                    villager.getHappiness() > 80
            ) {
                double lifespan = Villager.INFERTILITY_AGE - Villager.AGE_OF_CONSENT;
                double daysPerYear = (double)World.YEAR / (double)World.FULL_DAY;
                double probability = Villager.AVERAGE_NUM_OF_CHILDREN / (lifespan * daysPerYear);
                if(World.getRandom().nextDouble() < probability)
                    villager.reproduce();
            }
        }
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
