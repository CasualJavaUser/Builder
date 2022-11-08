package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;

public abstract class Job {
    public void assign(NPC assignee, ProductionBuilding workplace) {}

    public void onExit(NPC assignee, ProductionBuilding workplace) {}

    public Object getPoI() {return null;}

    public int getRange() {return 0;}
}
