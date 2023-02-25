package com.boxhead.builder.game_objects;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class FieldHarvestable extends Harvestable {
    private int currentPhase = 0;
    private final int[] phaseTimes;

    public FieldHarvestable(Harvestables.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        phaseTimes = type.phaseTimes;
    }

    public void changePhase() {
        currentPhase++;
        texture = Textures.get(type.textures[currentPhase]);
        amountLeft = type.size;
        if (currentPhase != phaseTimes.length - 1) {
            Harvestable.timeTriggers.add(Pair.of(World.calculateDate(phaseTimes[currentPhase]), this));
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null && (currentPhase == 0 || currentPhase == phaseTimes.length - 1);
    }

    @Override
    public boolean isNavigable() {
        return true;
    }

    @Override
    public void work() {
        if (worked) {
            boolean exit = false;
            boolean harvest = currentPhase == phaseTimes.length - 1;
            Resource resource = Resource.NOTHING;

            if (currentPhase == 0) {    //sowing
                productionCycle();
            } else if (harvest) { //harvesting
                resource = type.resource;
                if (!assigned.getInventory().isFull()) {
                    if (productionCycle())
                        assigned.getInventory().put(resource, 1);
                } else exit = true;
            }

            if (amountLeft <= 0) {
                exit = true;
                if (harvest) {
                    World.removeFieldWorks(this);
                    ((FarmBuilding) assigned.getWorkplace()).removeFieldHarvestable(this);
                } else
                    changePhase();
            }

            if (exit)
                exit(resource);
        }
    }

    @Override
    public BoxCollider getCollider() {
        return new BoxCollider(gridPosition, 1, 1);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Harvestables.Type.valueOf(ois.readUTF());
        texture = Textures.get(type.textures[currentPhase]);
    }
}
