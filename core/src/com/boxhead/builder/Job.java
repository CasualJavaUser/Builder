package com.boxhead.builder;

import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.ProductionBuilding;

public class Job {
    private final Recipe recipe = new Recipe();

    public void assign(Villager assignee, ProductionBuilding workplace) {}

    public void onExit(Villager assignee, ProductionBuilding workplace) {}

    public Recipe getRecipe() {return recipe;}

    public Object getPoI() {return null;}
}
