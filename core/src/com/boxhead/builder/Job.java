package com.boxhead.builder;

import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.game_objects.ProductionBuilding;

import java.util.Optional;

public enum Job {
    UNEMPLOYED(),
    DOCTOR(),
    LUMBERJACK(new Sequence() {
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
    }, Harvestable.Characteristic.TREE),
    BUILDER(new Sequence() {
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
    }, ConstructionSite.class);

    public Sequence sequence;
    private final Object poi;

    private static final Sequence DESK_JOB = new Sequence() {
        @Override
        public void assign(NPC assignee, ProductionBuilding workplace) {
        }

        @Override
        public void onExit(NPC assignee, ProductionBuilding workplace) {
        }
    };

    Job(Sequence sequence, Object poi) {
        this.sequence = sequence;
        this.poi = poi;
    }

    Job() {
        poi = null;
    }

    static {
        UNEMPLOYED.sequence = DESK_JOB;
        DOCTOR.sequence = DESK_JOB;
    }

    public Object getPoI() {
        return poi;
    }

    public static abstract class Sequence {
        public abstract void assign(NPC assignee, ProductionBuilding workplace);

        public abstract void onExit(NPC assignee, ProductionBuilding workplace);
    }
}
