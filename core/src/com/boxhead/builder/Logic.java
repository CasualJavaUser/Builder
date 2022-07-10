package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;

public class Logic {

    private static final Timer.Task task = new Timer.Task() {
        @Override
        public void run() {
            World.addTime(1);

            if (World.getTime() == 420) {   //7:00
                for (NPC npc : World.getNpcs()) {
                    if (npc.getJob() != Jobs.UNEMPLOYED) {
                        npc.exitBuilding();
                        npc.navigateTo(npc.getWorkplace());
                    }
                }
            } else if (World.getTime() == 960) { //16:00
                for (NPC npc : World.getNpcs()) {
                    if (npc.getJob() != Jobs.UNEMPLOYED) {
                        npc.exitBuilding();
                        if (npc.getHome() != null) {
                            npc.navigateTo(npc.getHome());
                        }
                    }
                }
            }

            for (NPC npc : World.getNpcs()) {
                npc.followPath();

                    npc.seekJob();  //todo
                    npc.seekHouse();
            }

            for (Building building : World.getBuildings()) {
                if (building instanceof ProductionBuilding) {
                    ((ProductionBuilding) building).produceResources();
                }
            }
        }
    };

    public static Timer.Task getTask() {
        return task;
    }
}
