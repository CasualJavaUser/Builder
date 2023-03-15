package com.boxhead.builder.game_objects;

import com.boxhead.builder.FieldWork;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class FarmAnimal extends Animal implements FieldWork {
    private BoxCollider pen;

    private Villager assigned = null;
    private boolean worked = false;
    private long harvestDate;
    private final int productionInterval = 50;
    private int productionCounter = 0;
    private int amountLeft;
    private BoxCollider collider;

    public FarmAnimal(Animals.Type type, Vector2i gridPosition, BoxCollider pen) {
        super(type, gridPosition);
        this.pen = pen;
        this.harvestDate = World.calculateDate(type.growthTime);
        this.amountLeft = type.yield;
        collider = new BoxCollider(gridPosition, 0, 0);
    }

    public void setPen(BoxCollider pen) {
        this.pen = pen;
    }

    @Override
    public void wander() {
        if (assigned == null && (path == null || followPath())) {
            if (World.getRandom().nextInt(360) == 0) {
                navigateTo(pen.toVector2iList().get(World.getRandom().nextInt(pen.getArea())));
            }
        }
    }

    @Override
    public Object getCharacteristic() {
        return this.getClass();
    }

    @Override
    public void assignWorker(Villager villager) {
        if (isFree()) {
            assigned = villager;
        } else throw new IllegalArgumentException();
    }

    @Override
    public void dissociateWorker(Villager villager) {
        if (assigned == villager) {
            assigned = null;
            worked = false;
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null && World.getDate() >= harvestDate;
    }

    @Override
    public void work() {
        if (worked) {
            if (!assigned.getInventory().isFull()) {
                if (productionCycle()) {
                    assigned.getInventory().put(type.resource, 1);
                    World.updateStoredResources(type.resource, 1);
                }
                if (amountLeft <= 0) {
                    harvestDate = World.calculateDate(type.growthTime);
                    amountLeft = type.yield;
                    exit();
                }
            } else exit();
        }
    }

    protected boolean productionCycle() {
        productionCounter++;
        if (productionCounter >= productionInterval) {
            amountLeft--;
            productionCounter = 0;
            return true;
        }
        return false;
    }

    protected void exit() {
        assigned.getWorkplace().dissociateFieldWork(assigned);
        worked = false;
        assigned = null;
    }

    @Override
    public void setWork(Villager villager, boolean b) {
        if (villager == assigned) worked = b;
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
    }
}
