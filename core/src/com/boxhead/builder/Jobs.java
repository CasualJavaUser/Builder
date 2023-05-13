package com.boxhead.builder;

import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.Optional;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class Jobs {
    public static final Job UNEMPLOYED = new Job() {
        @Override
        public String toString() {
            return "unemployed";
        }
    };

    public static final Job DOCTOR = new Job() {
        @Override
        public String toString() {
            return "doctor";
        }
    };

    public static final Job LUMBERJACK = new Job() {
        final Recipe recipe = new Recipe(Pair.of(Resource.WOOD, Villager.INVENTORY_SIZE));

        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            harvesterAssign(assignee, workplace, (Harvestable) Logistics.assignedFieldWork(workplace));
        }

        @Override
        public void continuousTask(Villager assignee, ProductionBuilding workplace) {
            harvesterContinuous(assignee, workplace, Resource.WOOD);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            harvesterOnExit(assignee, workplace, Resource.WOOD);
        }

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            return recipe;
        }

        @Override
        public Object getPoI() {
            return Harvestable.Characteristic.TREE;
        }

        @Override
        public String toString() {
            return "lumberjack";
        }
    };

    public static final Job STONEMASON = new Job() {
        private final Recipe recipe = new Recipe(Pair.of(Resource.STONE, Villager.INVENTORY_SIZE));

        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            harvesterAssign(assignee, workplace, (Harvestable) Logistics.assignedFieldWork(workplace));
        }

        @Override
        public void continuousTask(Villager assignee, ProductionBuilding workplace) {
            harvesterContinuous(assignee, workplace, Resource.STONE);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            harvesterOnExit(assignee, workplace, Resource.STONE);
        }

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            return recipe;
        }

        @Override
        public Object getPoI() {
            return Harvestable.Characteristic.ROCK;
        }

        @Override
        public String toString() {
            return "stonemason";
        }
    };

    public static final Job BUILDER = new Job() {
        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            FieldWork fieldWork = Logistics.assignedFieldWork(workplace);
            if (fieldWork == null || !fieldWork.isFree())
                return;

            fieldWork.assignWorker(assignee);
            workplace.getAssignedFieldWork().put(assignee, fieldWork);
            assignee.giveOrder(EXIT, workplace);
            assignee.giveOrder(GO_TO, fieldWork);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            if (workplace.getAssignedFieldWork().containsKey(assignee)) {
                FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);
                assignee.giveOrder(EXIT, fieldWork);
                workplace.dissociateFieldWork(assignee);
            }
        }

        @Override
        public Object getPoI() {
            return ConstructionSite.class;
        }

        @Override
        public String toString() {
            return "builder";
        }
    };

    public static final Job MINER = new Job() {
        private final Recipe recipe = new Recipe(Pair.of(Resource.IRON, 5));

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            return recipe;
        }

        @Override
        public String toString() {
            return "miner";
        }
    };

    public static final Job CARRIER = new Job() {
        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            Logistics.Order order = Logistics.assignedOrder(workplace);

            if (order == null || order.amount < Villager.INVENTORY_SIZE)
                return;

            Logistics.removeOrder(order, assignee, Villager.INVENTORY_SIZE);
            assignee.giveOrder(EXIT, workplace);
            assignee.giveOrder(GO_TO, order.sender);
            assignee.giveOrder(TAKE_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(EXIT, order.sender);
            assignee.giveOrder(GO_TO, order.recipient);
            assignee.giveOrder(PUT_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(EXIT, order.recipient);
            assignee.giveOrder(END_DELIVERY);
            assignee.giveOrder(GO_TO, workplace);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            Logistics.Order order = Logistics.getDeliveryList().get(assignee);

            if (order == null)
                return;

            if (assignee.getInventory().isEmpty()) {
                assignee.giveOrder(GO_TO, order.sender);
                assignee.giveOrder(TAKE_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            }
            assignee.giveOrder(EXIT, order.sender);
            assignee.giveOrder(GO_TO, order.recipient);
            assignee.giveOrder(PUT_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(EXIT, order.recipient);
            assignee.giveOrder(END_DELIVERY);
        }

        @Override
        public String toString() {
            return "carrier";
        }
    };

    public static final Job FARMER = new Job() {
        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            farmerAssign(assignee, workplace);
        }

        @Override
        public void continuousTask(Villager assignee, ProductionBuilding workplace) {
            if (assignee.hasOrders() || assignee.isInBuilding() || workplace.getAssignedFieldWork().containsKey(assignee))
                return;

            if (!farmerAssign(assignee, workplace)) {
                assignee.giveOrder(GO_TO, workplace);
                if (!assignee.getInventory().isEmpty()) {
                    Resource resource = ((FarmBuilding<? extends FieldWork>) workplace).getResource();
                    int units = assignee.getInventory().getResourceAmount(resource);
                    assignee.giveOrder(PUT_RESERVED_RESOURCES, resource, units);
                    assignee.giveOrder(REQUEST_TRANSPORT, resource, units);
                }
            }
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            harvesterOnExit(assignee, workplace, ((PlantationBuilding) workplace).getCrop().characteristic.resource);
        }

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            if (workplace != null)
                return ((FarmBuilding<?>) workplace).getRecipe();
            else
                return new Recipe(Pair.of(Resource.GRAIN, 10));
        }

        @Override
        public Object getPoI() {
            return Harvestable.Characteristic.FIELD_CROP;
        }

        @Override
        public String toString() {
            return "farmer";
        }
    };

    public static final Job BARTENDER = new Job() {
        private final Recipe recipe = new Recipe(
                Pair.of(Resource.GRAIN, -3),
                Pair.of(Resource.ALCOHOL, 1)
        );

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            return recipe;
        }

        @Override
        public String toString() {
            return "bartender";
        }
    };

    private static void harvesterAssign(Villager assignee, ProductionBuilding workplace, Harvestable harvestable) {
        if (harvestable == null) return;

        int defaultYield = Math.min(harvestable.getType().yield, Villager.INVENTORY_SIZE);
        if (!(harvestable.isFree() && workplace.getFreeSpace() >= defaultYield))
            return;

        workplace.reserveSpace(assignee, defaultYield);
        harvestable.assignWorker(assignee);
        workplace.getAssignedFieldWork().put(assignee, harvestable);
        assignee.giveOrder(EXIT, workplace);
        assignee.giveOrder(GO_TO, harvestable);
    }

    private static void harvesterContinuous(Villager assignee, ProductionBuilding workplace, Resource resource) {
        boolean readyToReturn = !assignee.isInBuilding(workplace) && !workplace.getAssignedFieldWork().containsKey(assignee);

        if (!assignee.hasOrders() && (assignee.getInventory().isFull() || readyToReturn)) {
            int resourceAmount = assignee.getInventory().getResourceAmount(resource);
            assignee.giveOrder(GO_TO, workplace);
            assignee.giveOrder(PUT_RESERVED_RESOURCES, resource, resourceAmount);
            assignee.giveOrder(REQUEST_TRANSPORT, resource, resourceAmount);
        }
    }

    private static void harvesterOnExit(Villager assignee, ProductionBuilding workplace, Resource resource) {
        FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);

        if (fieldWork != null) {
            assignee.giveOrder(EXIT, fieldWork);
            workplace.dissociateFieldWork(assignee);
        }

        int resourceUnits = assignee.getInventory().getResourceAmount(resource);
        if (resourceUnits != 0) {
            assignee.giveOrder(GO_TO, workplace);
            assignee.giveOrder(PUT_RESERVED_RESOURCES, resource, resourceUnits);
            assignee.giveOrder(REQUEST_TRANSPORT, resource, resourceUnits);
        } else if (workplace.hasReserved(assignee, resource)){
            workplace.cancelReservation(assignee);
        }
    }

    private static boolean farmerAssign(Villager assignee, ProductionBuilding workplace) {
        FarmBuilding<? extends FieldWork> employingFarm = (FarmBuilding<? extends FieldWork>) workplace;
        Optional<? extends FieldWork> fieldWorkOptional = employingFarm.findWorkableFieldWork();

        if (fieldWorkOptional.isPresent() &&
                assignee.getInventory().getAvailableCapacity() >= employingFarm.getYield() &&
                workplace.reserveSpace(assignee, employingFarm.getYield())) {
            FieldWork fieldWork = fieldWorkOptional.get();
            fieldWork.assignWorker(assignee);
            workplace.getAssignedFieldWork().put(assignee, fieldWork);
            assignee.giveOrder(EXIT, workplace);
            assignee.giveOrder(GO_TO, fieldWork);
            return true;
        } else if (workplace instanceof PlantationBuilding plantation) {
            for (Vector2i tile : employingFarm.getFieldCollider()) {
                if (plantation.isArable(tile)) {
                    Harvestable newHarvestable = Harvestables.create(plantation.getCrop(), tile);
                    plantation.addFieldWork(newHarvestable);

                    assignee.giveOrder(EXIT, workplace);
                    assignee.giveOrder(tile);
                    assignee.giveOrder(newHarvestable);
                    return true;
                }
            }
        }
        return false;
    }
}
