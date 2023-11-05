package com.boxhead.builder;

import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.BuildSite;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public interface FieldWork extends WorldObject, Serializable {
    Object getCharacteristic();

    void assignWorker(Villager villager);

    void dissociateWorker(Villager villager);

    boolean isFree();

    void work();

    void setWork(Villager villager);

    BoxCollider getCollider();

    static Optional<FieldWork> findFieldWorkInRange(Object characteristic, Vector2i gridPosition, int range) {
        Stream<FieldWork> stream;
        if (characteristic == BuildSite.class) {
            int rangeSquared = range * range;
            stream = World.getBuildings().stream()
                    .filter(building -> building instanceof BuildSite)
                    .map(FieldWork.class::cast)
                    .filter(fw -> fw.getCollider().distanceSquared(gridPosition) <= rangeSquared);
        } else if (characteristic instanceof Harvestable.Characteristic) {
            BoxCollider searchArea = new BoxCollider(gridPosition.plus(-range, -range), range * 2, range * 2);
            stream = World.findHarvestables(searchArea).stream()
                    .filter(harvestable -> harvestable.getCharacteristic() == characteristic)
                    .map(FieldWork.class::cast);

        } else throw new IllegalArgumentException("Unrecognised FieldWork characteristic: " + characteristic.getClass().getName());

        return stream
                .filter(FieldWork::isFree)
                .min(Comparator.comparingInt(fieldWork -> fieldWork.getGridPosition().distanceSquared(gridPosition)));
    }
}
