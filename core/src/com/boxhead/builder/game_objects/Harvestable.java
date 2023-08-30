package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.buildings.PlantationBuilding;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Comparator;

public class Harvestable extends GameObject implements FieldWork {
    public static final SortedList<Pair<Long, Harvestable>> timeTriggers = new SortedList<>(Comparator.comparing(pair -> pair.first, Comparator.reverseOrder()));

    protected transient Harvestables.Type type;
    protected transient TextureRegion[] textureBundle;
    protected transient TextureRegion currentTexture;
    protected int currentPhase = -1;
    protected BoxCollider collider;
    protected final int productionInterval = 50;
    protected int productionCounter = 0;
    protected int amountLeft;

    protected Villager assigned = null;
    protected boolean worked = false;

    public enum Characteristic {
        TREE(Resource.WOOD),
        ROCK(Resource.STONE),
        FIELD_CROP(Resource.GRAIN);

        public final Resource resource;

        Characteristic(Resource resource) {
            this.resource = resource;
        }
    }

    public Harvestable(Harvestables.Type type, Vector2i gridPosition) {
        super(type.textureId, gridPosition);
        this.type = type;
        textureBundle = Textures.getBundle(type.textureId);
        currentTexture = textureBundle[0];
        amountLeft = type.yield;

        collider = switch (type.characteristic) {
            case TREE -> new BoxCollider(
                    new Vector2i(gridPosition.x + getTexture().getRegionWidth() / World.TILE_SIZE / 2, gridPosition.y),
                    1,
                    1);

            case FIELD_CROP -> new BoxCollider(getGridPosition().x, getGridPosition().y, 0, 0);

            default -> getDefaultCollider();
        };
    }

    public Harvestables.Type getType() {
        return type;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public void nextPhase() {
        currentTexture = textureBundle[++currentPhase];
        if (currentPhase != textureBundle.length - 1) {
            Harvestable.timeTriggers.add(Pair.of(World.calculateDate(type.growthTime / (textureBundle.length - 1)), this));
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(currentTexture, gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }

    @Override
    public Object getCharacteristic() {
        return type.characteristic;
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
            villager.setAnimation(Villager.Animation.WALK);
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null && currentPhase >= textureBundle.length - 1;
    }

    @Override
    public void work() {
        if (worked) {
            Resource resource = type.characteristic.resource;
            if (!assigned.getInventory().isFull()) {
                if (productionCycle()) {
                    assigned.getInventory().put(resource, 1);
                    Resource.updateStoredResources(resource, 1);
                }
                if (amountLeft <= 0) {
                    World.removeFieldWorks(this);
                    if (assigned.getWorkplace() instanceof PlantationBuilding farm) farm.removeHarvestable(this);
                    exit();
                }
            } else exit();
        }
    }

    @Override
    public void setWork(Villager villager) {
        if (villager == assigned) {
            worked = true;
            Villager.Animation anim = switch ((Characteristic) getCharacteristic()) {
                case TREE:
                    yield Villager.Animation.CHOPPING;
                case ROCK:
                    yield Villager.Animation.MINING;
                case FIELD_CROP:
                    yield Villager.Animation.SOWING;
            };
            villager.setAnimation(anim, gridPosition.x < villager.gridPosition.x);
        }
    }

    @Override
    public BoxCollider getCollider() {
        return collider;
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

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Harvestables.Type.valueOf(ois.readUTF());
        textureBundle = Textures.getBundle(type.textureId);
        currentTexture = textureBundle[Math.max(currentPhase, 0)];
    }
}
