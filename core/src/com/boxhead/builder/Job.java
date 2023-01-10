package com.boxhead.builder;

import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;

import java.util.Set;

public class Job {
    private final Recipe recipe = new Recipe();

    public void assign(NPC assignee, ProductionBuilding workplace) {}

    public void onExit(NPC assignee, ProductionBuilding workplace) {}

    public Recipe getRecipe() {return recipe;}

    public Object getPoI() {return null;}

    public int getRange() {return 0;}

    public float getEfficiency(Set<Building> buildingsInRange) {return 1;}
}
