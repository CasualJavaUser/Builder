package com.boxhead.builder.game_objects.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;

public class DemolitionSite extends BuildSite {
    private static final int INVENTORY_CAPACITY = 300;

    public DemolitionSite(Building building, int totalLabour) {
        super(building, totalLabour, INVENTORY_CAPACITY);

        World.removeBuilding(building);
        building.emptyOccupants();

        Inventory removedInventory = building.getInventory();
        Recipe returnResources = type.buildCost.half();
        returnResources.roundDown();
        Resource.updateStoredResources(returnResources);
        removedInventory.transferAllResourcesTo(inventory);
        inventory.put(returnResources);
        if (building.getType() == Type.STORAGE_BARN)
            Logistics.getStorages().remove(building);

        for (Resource resource : inventory.getStoredResources()) {
            int inInventory = inventory.getResourceAmount(resource);
            int clippedResources = inInventory % Logistics.THE_UNIT;
            Logistics.requestTransport(this, resource, inInventory - clippedResources, Logistics.USE_STORAGE);
            inventory.put(resource, -clippedResources);
            Resource.updateStoredResources(resource, -clippedResources);
        }
    }

    @Override
    public String getName() {
        return "demolition site\n(" + type.name + ")";
    }

    @Override
    public Object getCharacteristic() {
        return BuildSite.class;
    }

    @Override
    public boolean isFree() {
        return inventory.isEmpty() && assigned.size() < capacity;
    }

    @Override
    public void work() {
        progress += currentlyWorking;

        if (progress >= totalLabour) {
            World.removeFieldWorks(this);

            for (Villager villager : assigned.keySet()) {
                villager.getWorkplace().dissociateFieldWork(villager);
                villager.giveOrder(Villager.Order.Type.GO_TO, villager.getWorkplace());
            }
        }
    }

    @Override
    protected void checkAndDrawIndicator(SpriteBatch batch) {
        drawIndicator(Indicator.DEMOLISHING, batch);
    }
}
