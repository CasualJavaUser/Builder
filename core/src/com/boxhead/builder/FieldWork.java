package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.Comparator;
import java.util.Optional;

public interface FieldWork extends WorldObject {
    Object getCharacteristic();

    void assignWorker(NPC npc);

    void dissociateWorker(NPC npc);

    boolean isFree();

    void work();

    void setWork(NPC npc, boolean b);

    BoxCollider getCollider();

    static Optional<FieldWork> findFieldWork(Object characteristic, Vector2i gridPosition) {
        Optional<FieldWork> fieldWork = World.getBuildings().stream()
                .filter(building -> building instanceof FieldWork)
                .map(FieldWork.class::cast)
                .filter(fw -> fw.getCharacteristic().equals(characteristic))
                .filter(FieldWork::isFree)
                .min(Comparator.comparingDouble(building -> building.getGridPosition().distance(gridPosition)));
        if (fieldWork.isPresent()) {
            return fieldWork;
        }
        fieldWork = World.getHarvestables().stream()
                .filter(fw -> fw.getCharacteristic().equals(characteristic))
                .filter(FieldWork::isFree)
                .min(Comparator.comparingDouble(harvestable -> harvestable.getGridPosition().distance(gridPosition)))
                .map(FieldWork.class::cast);

        return fieldWork;
    }
}