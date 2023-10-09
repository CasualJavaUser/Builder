package com.boxhead.builder.game_objects.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.*;
import com.boxhead.builder.Logistics;
import com.boxhead.builder.Resource;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;

public class ConstructionSite extends BuildSite {
    public ConstructionSite(Building.Type type, Vector2i gridPosition, int totalLabour) {
        this(type, gridPosition, totalLabour, new BoxCollider());
    }

    public ConstructionSite(Building.Type type, Vector2i gridPosition, int totalLabour, BoxCollider fieldCollider) {
        super(type, gridPosition, totalLabour, fieldCollider);

        Resource.updateStoredResources(type.buildCost.negative());
        Logistics.requestTransport(this, type.buildCost.negative(), Logistics.USE_STORAGE);
        reserveSpace(type.buildCost.sum());
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        drawIndicator(batch);
    }

    @Override
    public String getName() {
        return "construction site\n(" + type.name + ")";
    }

    @Override
    public Object getCharacteristic() {
        return BuildSite.class;
    }

    @Override
    public boolean isFree() {
        return inventory.isFull() && assigned.size() < capacity;
    }

    @Override
    public void work() {
        progress += currentlyWorking;

        if (progress >= totalLabour) {
            World.removeFieldWorks(this);
            if (type instanceof FarmBuilding.Type farmType)
                World.placeFarm(farmType, gridPosition, fieldCollider);
            else
                World.placeBuilding(type, gridPosition);

            for (Villager villager : assigned.keySet()) {
                villager.getWorkplace().dissociateFieldWork(villager);
                villager.giveOrder(Villager.Order.Type.GO_TO, villager.getWorkplace());
            }
        }
    }

    @Override
    public void setWork(Villager villager) {
        if (assigned.containsKey(villager)) {
            assigned.replace(villager, true);
            updateCurrentlyWorking();
            villager.setAnimation(Villager.Animation.HAMMERING, gridPosition.x < villager.getGridPosition().x);
        }
    }

    private void updateCurrentlyWorking() {
        currentlyWorking = 0;
        for (Boolean working : assigned.values()) {
            if (working)
                currentlyWorking++;
        }
    }

    protected void drawIndicator(SpriteBatch batch) {
        if (false)  //TODO check if demolishing
            drawIndicator(Textures.get(Textures.Ui.DEMOLISHING), batch);
    }

    public void setFieldCollider(BoxCollider fieldCollider) {
        this.fieldCollider = fieldCollider;
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = type.getConstructionSite();
    }
}
