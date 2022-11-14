package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;

public class Job {
    public void assign(NPC assignee, ProductionBuilding workplace) {}

    public void onExit(NPC assignee, ProductionBuilding workplace) {}

    public Recipe getRecipe() {return new Recipe();}

    public Object getPoI() {return null;}

    public int getRange() {return 0;}
}
