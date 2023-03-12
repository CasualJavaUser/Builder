package com.boxhead.builder;

import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.util.Optional;

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
            harvesterAssign(assignee, workplace, Logistics.assignedFieldWork(workplace));
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
            harvesterAssign(assignee, workplace, Logistics.assignedFieldWork(workplace));
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
            assignee.giveOrder(Villager.Order.Type.EXIT, workplace);
            assignee.giveOrder(Villager.Order.Type.GO_TO, fieldWork);
            assignee.giveOrder(Villager.Order.Type.ENTER, fieldWork);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            if (workplace.getAssignedFieldWork().containsKey(assignee)) {
                FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);
                assignee.giveOrder(Villager.Order.Type.EXIT, fieldWork);
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

            Logistics.removeOrder(order, Villager.INVENTORY_SIZE);
            Logistics.getDeliveryList().put(assignee, order);
            assignee.giveOrder(Villager.Order.Type.EXIT, workplace);
            assignee.giveOrder(Villager.Order.Type.GO_TO, order.sender);
            assignee.giveOrder(Villager.Order.Type.ENTER, order.sender);
            assignee.giveOrder(Villager.Order.Type.TAKE_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(Villager.Order.Type.EXIT, order.sender);
            assignee.giveOrder(Villager.Order.Type.GO_TO, order.recipient);
            assignee.giveOrder(Villager.Order.Type.ENTER, order.recipient);
            assignee.giveOrder(Villager.Order.Type.PUT_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(Villager.Order.Type.EXIT, order.recipient);
            assignee.giveOrder(Villager.Order.Type.END_DELIVERY);
            assignee.giveOrder(Villager.Order.Type.GO_TO, workplace);
            assignee.giveOrder(Villager.Order.Type.ENTER, workplace);
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            Logistics.Order order = Logistics.getDeliveryList().get(assignee);

            if (order == null)
                return;

            if (assignee.getInventory().isEmpty()) {
                assignee.giveOrder(Villager.Order.Type.GO_TO, order.sender);
                assignee.giveOrder(Villager.Order.Type.ENTER, order.sender);
                assignee.giveOrder(Villager.Order.Type.TAKE_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            }
            assignee.giveOrder(Villager.Order.Type.EXIT, order.sender);
            assignee.giveOrder(Villager.Order.Type.GO_TO, order.recipient);
            assignee.giveOrder(Villager.Order.Type.ENTER, order.recipient);
            assignee.giveOrder(Villager.Order.Type.PUT_RESERVED_RESOURCES, order.resource, Villager.INVENTORY_SIZE);
            assignee.giveOrder(Villager.Order.Type.EXIT, order.recipient);
            assignee.giveOrder(Villager.Order.Type.END_DELIVERY);
        }

        @Override
        public String toString() {
            return "carrier";
        }
    };

    public static final Job FARMER = new Job() {
        @Override
        public void assign(Villager assignee, ProductionBuilding workplace) {
            if (workplace.getAssignedFieldWork().containsKey(assignee) || assignee.hasOrders())
                return;

            FarmBuilding<? extends FieldWork> employingFarm = (FarmBuilding<? extends FieldWork>) workplace;

            //if reserved then harvest
            if (workplace.hasReserved(assignee)) {
                Optional<? extends FieldWork> fieldWorkOptional = employingFarm.findWorkableFieldWork();
                if(fieldWorkOptional.isPresent() && assignee.getInventory().getAvailableCapacity() >= employingFarm.getYield()) {
                    FieldWork fieldWork = fieldWorkOptional.get();
                    fieldWork.assignWorker(assignee);
                    workplace.getAssignedFieldWork().put(assignee, fieldWork);
                    assignee.giveOrder(Villager.Order.Type.EXIT, workplace);
                    assignee.giveOrder(Villager.Order.Type.GO_TO, fieldWork);
                    assignee.giveOrder(Villager.Order.Type.ENTER, fieldWork);
                }
                else {
                    Resource resource = employingFarm.getResource();
                    int resourceUnits = assignee.getInventory().getResourceAmount(resource);

                    assignee.giveOrder(Villager.Order.Type.GO_TO, workplace);
                    assignee.giveOrder(Villager.Order.Type.ENTER, workplace);
                    assignee.giveOrder(Villager.Order.Type.PUT_RESERVED_RESOURCES, resource, resourceUnits);
                    assignee.giveOrder(Villager.Order.Type.REQUEST_TRANSPORT, resource, resourceUnits);
                    assignee.giveOrder(Villager.Order.Type.REMOVE_RESERVATION);
                }
                return;
            }

            //if plantation then plant
            if (employingFarm instanceof PlantationBuilding plantation) {
                for (Vector2i tile : employingFarm.getFieldCollider().toVector2iList()) {
                    if (plantation.isArable(tile)) {
                        Harvestable newHarvestable = Harvestables.create(plantation.getCrop(), tile);
                        plantation.addFieldWork(newHarvestable);

                        assignee.giveOrder(Villager.Order.Type.EXIT, workplace);
                        assignee.giveOrder(tile);
                        assignee.giveOrder(newHarvestable);
                        //TODO wait?
                        return;
                    }
                }
            }

            //if not reserved then reserve
            if (    !employingFarm.hasReserved(assignee) &&
                    workplace.getInventory().getAvailableCapacity() >= Villager.INVENTORY_SIZE &&
                    employingFarm.findWorkableFieldWork().isPresent() &&
                    workplace.reserveSpace(Villager.INVENTORY_SIZE)
            ) {
                workplace.addReservation(assignee);
                return;
            }

            //return to workplace
            if (!assignee.isInBuilding(workplace)) {
                assignee.giveOrder(Villager.Order.Type.GO_TO, workplace);
                assignee.giveOrder(Villager.Order.Type.ENTER, workplace);
            }
        }

        @Override
        public void onExit(Villager assignee, ProductionBuilding workplace) {
            harvesterOnExit(assignee, workplace, ((PlantationBuilding) workplace).getCrop().characteristic.resource);
        }

        @Override
        public Recipe getRecipe(ProductionBuilding workplace) {
            return ((FarmBuilding<?>) workplace).getRecipe();
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

    private static void harvesterAssign(Villager assignee, ProductionBuilding workplace, FieldWork fieldWork) {
        if (workplace.getAssignedFieldWork().get(assignee) == null) {
            if (!workplace.hasReserved(assignee)) {
                if (fieldWork.isFree() &&
                        workplace.getInventory().getAvailableCapacity() >= Villager.INVENTORY_SIZE &&
                        workplace.reserveSpace(Villager.INVENTORY_SIZE)
                ) {
                    fieldWork.assignWorker(assignee);
                    workplace.getAssignedFieldWork().put(assignee, fieldWork);
                    workplace.addReservation(assignee);
                    assignee.giveOrder(Villager.Order.Type.EXIT, workplace);
                    assignee.giveOrder(Villager.Order.Type.GO_TO, fieldWork);
                    assignee.giveOrder(Villager.Order.Type.ENTER, fieldWork);
                }
            } else {
                Resource resource = Resource.NOTHING;
                int resourceUnits = 0;
                if (!assignee.getInventory().isEmpty()) {
                    resource = assignee.getInventory().getStoredResources().iterator().next();
                    resourceUnits = assignee.getInventory().getResourceAmount(resource);
                }

                assignee.giveOrder(Villager.Order.Type.GO_TO, workplace);
                assignee.giveOrder(Villager.Order.Type.ENTER, workplace);
                assignee.giveOrder(Villager.Order.Type.PUT_RESERVED_RESOURCES, resource, resourceUnits);
                assignee.giveOrder(Villager.Order.Type.REQUEST_TRANSPORT, resource, resourceUnits);
                assignee.giveOrder(Villager.Order.Type.REMOVE_RESERVATION);
            }
        }
    }

    private static void harvesterOnExit(Villager assignee, ProductionBuilding workplace, Resource resource) {
        assignee.giveOrder(Villager.Order.Type.GO_TO, workplace);
        assignee.giveOrder(Villager.Order.Type.ENTER, workplace);

        if (workplace.hasReserved(assignee)) {
            if (workplace.getAssignedFieldWork().containsKey(assignee)) {
                FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);
                assignee.giveOrder(Villager.Order.Type.EXIT, fieldWork);
                workplace.dissociateFieldWork(assignee);
            }

            int resourceUnits = assignee.getInventory().getResourceAmount(resource);

            assignee.giveOrder(Villager.Order.Type.PUT_RESERVED_RESOURCES, resource, resourceUnits);
            assignee.giveOrder(Villager.Order.Type.REQUEST_TRANSPORT, resource, resourceUnits);
            assignee.giveOrder(Villager.Order.Type.REMOVE_RESERVATION);
        }
    }
}
