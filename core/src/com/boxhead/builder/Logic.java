package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.ui.UI;
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
            while (!Harvestable.timeTriggers.isEmpty() && Harvestable.timeTriggers.get(0).first == World.getDate()) {
                Harvestable.timeTriggers.remove(0).second.nextPhase();
            }
            produceResources();
            for (NPC npc : World.getNpcs()) {
                npc.executeOrders();
            }
        }
    };

    private static final Timer.Task intermittentTask = new Timer.Task() {
        @Override
        public void run() {
            Logistics.pairRequests();
            UI.getResourceList().updateData();
            for (NPC npc : World.getNpcs()) {
                npc.seekJob();
                npc.seekHouse();
            }
        }
    };

    private static void dailyCycle() {
        if (World.getTime() == 25200) {   //7:00
            for (NPC npc : World.getNpcs()) {
                if (npc.getJob() != Jobs.UNEMPLOYED) {
                    npc.giveOrder(NPC.Order.Type.EXIT);
                    npc.giveOrder(NPC.Order.Type.GO_TO, npc.getWorkplace());
                    npc.giveOrder(NPC.Order.Type.ENTER, npc.getWorkplace());
                }
            }
        } else if (World.getTime() == 57600) { //16:00
            for (Building building : World.getBuildings()) {
                if (building instanceof ProductionBuilding) {
                    ((ProductionBuilding) building).endWorkday();
                }
            }
        } else if (World.getTime() == 0) {  //0:00
            Pathfinding.removeUnusedPaths();
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

    private static void NPCLife() {
        for (NPC npc : World.getNpcs()) {
            npc.executeOrders();

            npc.seekJob();  //todo make these into orders
            npc.seekHouse();
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
