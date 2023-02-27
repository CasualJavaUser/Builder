package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

public interface FieldWork extends WorldObject, Serializable {
    Object getCharacteristic();

    void assignWorker(NPC npc);

    void dissociateWorker(NPC npc);

    boolean isFree();

    default boolean isNavigable() {
        return false;
    }

    void work();

    void setWork(NPC npc, boolean b);

    BoxCollider getCollider();

    static Optional<FieldWork> findFieldWork(Object characteristic, Vector2i gridPosition) {
        return World.getFieldWorks().stream()
                .filter(fw -> fw.getCharacteristic().equals(characteristic))
                .filter(FieldWork::isFree)
                .min(Comparator.comparingInt(harvestable -> harvestable.getGridPosition().distanceScore(gridPosition)))
                .map(FieldWork.class::cast);
    }

    static Optional<FieldWork> findFieldWorkInRange(Object characteristic, Vector2i gridPosition, int range) {
        return World.getFieldWorks().stream()
                .filter(fw -> fw.getCharacteristic().equals(characteristic))
                .filter(FieldWork::isFree)
                .filter(harvestable -> harvestable.getCollider().getGridPosition().distance(gridPosition) <= range)
                .min(Comparator.comparingInt(harvestable -> harvestable.getGridPosition().distanceScore(gridPosition)))
                .map(FieldWork.class::cast);
    }
}
