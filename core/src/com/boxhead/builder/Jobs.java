package com.boxhead.builder;

import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;

import java.util.Optional;

public class Jobs {
    public static final Job UNEMPLOYED = new Job() {};
    public static final Job DOCTOR = new Job() {};
    public static final Job LUMBERJACK = new Job() {
        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
            if (workplace.getInventory().remainingCapacity() < NPC.INVENTORY_SIZE)
                return;

            Optional<FieldWork> optional = FieldWork.findFieldWork(Harvestable.Characteristic.TREE, workplace.getEntrancePosition());
            if (optional.isPresent()) {
                FieldWork fieldWork = optional.get();
                fieldWork.assignWorker(assignee);
                workplace.getAssignedFieldWork().put(assignee, fieldWork);
                workplace.getInventory().put(Resource.NOTHING, NPC.INVENTORY_SIZE);
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
                workplace.getInventory().put(Resource.NOTHING, -NPC.INVENTORY_SIZE);
            }
            if (!assignee.getInventory().isEmpty()) {
                assignee.giveOrder(NPC.Order.Type.GO_TO, workplace);
                assignee.giveOrder(NPC.Order.Type.ENTER, workplace);
                assignee.giveResourceOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, Resource.WOOD);
            }
        }

        @Override
        public Object getPoI() {
            return Harvestable.Characteristic.TREE;
        }

        @Override
        public int getRange() {
            return 10;
        }
    };
    public static final Job BUILDER = new Job() {
        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
            Optional<FieldWork> optional = FieldWork.findFieldWork(ConstructionSite.class, workplace.getEntrancePosition());
            if (optional.isPresent()) {
                FieldWork fieldWork = optional.get();
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
            }
        }

        @Override
        public Object getPoI() {
            return ConstructionSite.class;
        }
    };
}
