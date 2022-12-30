package com.boxhead.builder;

import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.Pair;

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
        private final Recipe recipe = new Recipe(Pair.of(Resource.WOOD, NPC.INVENTORY_SIZE));

        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
            FieldWork fieldWork = Logistics.assignedFieldWork(workplace);

            if (fieldWork == null || !fieldWork.isFree() || workplace.getInventory().getAvailableCapacity() < NPC.INVENTORY_SIZE)
                return;

            if (workplace.reserveSpace(NPC.INVENTORY_SIZE)) {
                fieldWork.assignWorker(assignee);
                workplace.getAssignedFieldWork().put(assignee, fieldWork);
                assignee.giveOrder(NPC.Order.Type.EXIT, workplace);
                assignee.giveOrder(NPC.Order.Type.GO_TO, fieldWork);
                assignee.giveOrder(NPC.Order.Type.ENTER, fieldWork);
            }
        }

        @Override
        public void onExit(NPC assignee, ProductionBuilding workplace) {
            if (workplace.getAssignedFieldWork().containsKey(assignee)) {
                FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);
                assignee.giveOrder(NPC.Order.Type.EXIT, fieldWork);
                workplace.dissociateFieldWork(assignee);
                workplace.cancelReservation(NPC.INVENTORY_SIZE);

                if (!assignee.getInventory().isEmpty()) {
                    int woodUnits = assignee.getInventory().getResourceAmount(Resource.WOOD);

                    assignee.giveOrder(NPC.Order.Type.GO_TO, workplace);
                    assignee.giveOrder(NPC.Order.Type.ENTER, workplace);
                    assignee.giveOrder(NPC.Order.Type.PUT_RESOURCES_TO_BUILDING, Resource.WOOD, woodUnits);
                    assignee.giveOrder(NPC.Order.Type.REQUEST_TRANSPORT, Resource.WOOD, woodUnits);
                }
            }
        }

        @Override
        public Recipe getRecipe() {
            return recipe;
        }

        @Override
        public Object getPoI() {
            return Harvestable.Characteristic.TREE;
        }

        @Override
        public int getRange() {
            return 10;
        }

        @Override
        public String toString() {
            return "lumberjack";
        }
    };
    public static final Job BUILDER = new Job() {
        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
            FieldWork fieldWork = Logistics.assignedFieldWork(workplace);
            if (fieldWork == null || !fieldWork.isFree())
                return;

            fieldWork.assignWorker(assignee);
            workplace.getAssignedFieldWork().put(assignee, fieldWork);
            assignee.giveOrder(NPC.Order.Type.EXIT, workplace);
            assignee.giveOrder(NPC.Order.Type.GO_TO, fieldWork);
            assignee.giveOrder(NPC.Order.Type.ENTER, fieldWork);
        }

        @Override
        public void onExit(NPC assignee, ProductionBuilding workplace) {
            if (workplace.getAssignedFieldWork().containsKey(assignee)) {
                FieldWork fieldWork = workplace.getAssignedFieldWork().get(assignee);
                assignee.giveOrder(NPC.Order.Type.EXIT, fieldWork);
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

    public static final Job CARRIER = new Job() {
        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
            Logistics.Order order = Logistics.assignedOrder(workplace);

            if (order == null || order.amount < NPC.INVENTORY_SIZE)
                return;

            Logistics.removeOrder(order, NPC.INVENTORY_SIZE);
            Logistics.getDeliveryList().put(assignee, order);
            assignee.giveOrder(NPC.Order.Type.EXIT, workplace);
            assignee.giveOrder(NPC.Order.Type.GO_TO, order.sender);
            assignee.giveOrder(NPC.Order.Type.ENTER, order.sender);
            assignee.giveOrder(NPC.Order.Type.TAKE_RESERVED_RESOURCES, order.resource, NPC.INVENTORY_SIZE);
            assignee.giveOrder(NPC.Order.Type.EXIT, order.sender);
            assignee.giveOrder(NPC.Order.Type.GO_TO, order.recipient);
            assignee.giveOrder(NPC.Order.Type.ENTER, order.recipient);
            assignee.giveOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, order.resource, NPC.INVENTORY_SIZE);
            assignee.giveOrder(NPC.Order.Type.EXIT, order.recipient);
            assignee.giveOrder(NPC.Order.Type.END_DELIVERY);
            assignee.giveOrder(NPC.Order.Type.GO_TO, workplace);
            assignee.giveOrder(NPC.Order.Type.ENTER, workplace);
        }

        @Override
        public void onExit(NPC assignee, ProductionBuilding workplace) {
            Logistics.Order order = Logistics.getDeliveryList().get(assignee);

            if (order == null)
                return;

            if (assignee.getInventory().isEmpty()) {
                assignee.giveOrder(NPC.Order.Type.GO_TO, order.sender);
                assignee.giveOrder(NPC.Order.Type.ENTER, order.sender);
                assignee.giveOrder(NPC.Order.Type.TAKE_RESERVED_RESOURCES, order.resource, NPC.INVENTORY_SIZE);
            }
            assignee.giveOrder(NPC.Order.Type.EXIT, order.sender);
            assignee.giveOrder(NPC.Order.Type.GO_TO, order.recipient);
            assignee.giveOrder(NPC.Order.Type.ENTER, order.recipient);
            assignee.giveOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, order.resource, NPC.INVENTORY_SIZE);
            assignee.giveOrder(NPC.Order.Type.EXIT, order.recipient);
            assignee.giveOrder(NPC.Order.Type.END_DELIVERY);
        }

        @Override
        public String toString() {
            return "carrier";
        }
    };
}
