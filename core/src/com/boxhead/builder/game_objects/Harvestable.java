package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
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

    protected NPC assigned = null;
    protected boolean worked = false;

    public enum Characteristic {
        TREE(Resource.WOOD),
        ROCK(Resource.STONE),
        IRON_ORE(Resource.IRON),
        WHEAT(Resource.GRAIN);

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

            case WHEAT -> new BoxCollider(getGridPosition().x, getGridPosition().y, 0, 0);

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
        if (currentPhase != textureBundle.length-1) {
            Harvestable.timeTriggers.add(Pair.of(World.calculateDate(type.growthTime/(textureBundle.length-1)), this));
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
    public void assignWorker(NPC npc) {
        if (isFree()) {
            assigned = npc;
        } else throw new IllegalArgumentException();
    }

    @Override
    public void dissociateWorker(NPC npc) {
        if (assigned == npc) {
            assigned = null;
            worked = false;
        }
    }

    @Override
    public boolean isFree() {
        return assigned == null && currentPhase >= textureBundle.length - 1;
    }

    @Override
    public void work() {
        if(worked) {
            Resource resource = type.characteristic.resource;
            if (!assigned.getInventory().isFull()) {
                if (productionCycle()) assigned.getInventory().put(resource, 1);
                if (amountLeft <= 0) {
                    World.removeFieldWorks(this);
                    if(assigned.getWorkplace() instanceof FarmBuilding farm) farm.removeHarvestable(this);
                    exit(resource);
                }
            } else exit(resource);
        }
    }

    @Override
    public void setWork(NPC npc, boolean b) {
        if (npc == assigned) worked = b;
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

    protected void exit(Resource resource) {
        assigned.getWorkplace().dissociateFieldWork(assigned);
        //assigned.giveOrder(NPC.Order.Type.GO_TO, assigned.getWorkplace());
        //assigned.giveOrder(NPC.Order.Type.ENTER, assigned.getWorkplace());
        //assigned.giveOrder(NPC.Order.Type.PUT_RESERVED_RESOURCES, resource, assigned.getInventory().getResourceAmount(resource));
        //assigned.giveOrder(NPC.Order.Type.REQUEST_TRANSPORT, resource, NPC.INVENTORY_SIZE);
        dissociateWorker(assigned);
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
