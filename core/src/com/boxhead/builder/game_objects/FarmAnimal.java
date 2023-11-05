package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class FarmAnimal extends Animal implements FieldWork {
    private static final int PRODUCTION_INTERVAL = 50;
    private BoxCollider pen;
    private Villager assigned = null;
    private boolean worked = false;
    private long harvestDate;
    private long respawnDate;
    private boolean dead = false;
    private int productionCounter = 0;
    private int amountLeft;
    private final BoxCollider collider;

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
        if (!dead && assigned == null && (path == null || followPath())) {
            if (BuilderGame.generalPurposeRandom().nextInt(360) == 0) {
                navigateTo(pen.randomPosition());
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
            prevPosition.set(gridPosition);
        } else throw new IllegalArgumentException();
    }

    @Override
    public void dissociateWorker(Villager villager) {
        if (assigned == villager) {
            assigned = null;
            worked = false;
            villager.setAnimation(Villager.Animation.WALK);
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null && World.getDate() >= harvestDate;
    }

    @Override
    public void work() {
        if (!worked) return;

        if (!assigned.getInventory().isFull()) {
            if (productionCycle()) {
                assigned.getInventory().put(type.resource, 1);
                Resource.updateStoredResources(type.resource, 1);
            }
            if (amountLeft <= 0) {
                harvestDate = World.calculateDate(type.growthTime);
                amountLeft = type.yield;
                if (type.slaughtered) {
                    respawnDate = World.calculateDate(type.growthTime / 3);
                    dead = true;
                    gridPosition.set(pen.getGridPosition());
                    prevPosition.set(pen.getGridPosition());
                    spritePosition.set(pen.getGridPosition().x, pen.getGridPosition().y);
                }
                exit();
            }
        } else exit();
    }

    public void respawn() {
        if (dead && World.getDate() >= respawnDate)
            dead = false;
    }

    protected boolean productionCycle() {
        productionCounter++;
        if (productionCounter >= PRODUCTION_INTERVAL) {
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
    public void draw(SpriteBatch batch) {
        if (!dead)
            super.draw(batch);
    }

    @Override
    public void setWork(Villager villager) {
        if (villager == assigned) worked = true;
        villager.setAnimation(Villager.Animation.HARVESTING, gridPosition.x < villager.getGridPosition().x);
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
    }
}
