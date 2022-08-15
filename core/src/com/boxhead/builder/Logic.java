package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;
import com.boxhead.builder.game_objects.*;

public class Logic {

    private static final Timer.Task task = new Timer.Task() {
        @Override
        public void run() {
            World.addTime(1);
            dailyCycle();
            NPCLife();
            produceResources();
        }
    };

    public static Timer.Task getTask() {
        return task;
    }

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
        }
    }

    private static void produceResources() {
        for (int i = 0; i < World.getBuildings().size(); i++) {
            if (World.getBuildings().get(i) instanceof ServiceBuilding) {
                ((ServiceBuilding) World.getBuildings().get(i)).provideServices();
            }
            if (World.getBuildings().get(i) instanceof ProductionBuilding) {
                ((ProductionBuilding) World.getBuildings().get(i)).produceResources();
            }
            if (World.getBuildings().get(i) instanceof FieldWork) {
                ((FieldWork) World.getBuildings().get(i)).work();
            }
        }

        for (Harvestable harvestable : World.getHarvestables()) {
            harvestable.work();
        }
    }

    private static void NPCLife() {
        for (NPC npc : World.getNpcs()) {
            npc.executeOrders();

            npc.seekJob();
            npc.seekHouse();
        }
    }
}
