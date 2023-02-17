package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;
import com.boxhead.builder.utils.Pathfinding;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NPC extends GameObject implements Clickable {
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    public static final int INVENTORY_SIZE = 10;
    private static final int STEP_INTERVAL = 50;

    private final String name, surname;
    private final int[] stats = new int[Stats.values().length];
    private final int textureId;
    private transient TextureRegion idleTexture;
    private transient Animation<TextureRegion> walkLeft;
    private transient Animation<TextureRegion> walkRight;

    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private StorageBuilding buildingIsIn = null;

    private Vector2i prevPosition;
    private final Vector2 spritePosition;
    private int nextStep;

    private Vector2i[] path = null;
    private int pathStep;
    private transient Future<?> pathfinding;

    private final LinkedList<NPC.Order> orderList = new LinkedList<>();

    private final Inventory inventory = new Inventory(INVENTORY_SIZE);

    float stateTime = 0;

    public static final int SIZE = 16;

    public enum Stats {
        AGE,
        HEALTH
    }

    public NPC(int textureId, Vector2i position) {
        super(Textures.get(Enum.valueOf(Textures.Npc.class, "IDLE" + textureId)), position);
        this.textureId = textureId;
        prevPosition = position;
        spritePosition = position.toVector2();
        name = NAMES[(int) (Math.random() * NAMES.length)];
        surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];

        idleTexture = texture;
        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT" + textureId));
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT" + textureId));
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!isInBuilding()) {
            float x = spritePosition.x * World.TILE_SIZE;
            float y = spritePosition.y * World.TILE_SIZE;
            Vector3 pos = GameScreen.worldToScreenPosition(x, y);

            if (pos.x + SIZE / GameScreen.camera.zoom > 0 && pos.x < Gdx.graphics.getWidth() &&
                    pos.y + SIZE / GameScreen.camera.zoom > 0 && pos.y < Gdx.graphics.getHeight()) {
                if (prevPosition.equals(gridPosition)) {
                    stateTime = 0;
                    texture = idleTexture;
                }
                else if(!Logic.isPaused()) {
                    stateTime += .01f / (Logic.getTickSpeed() * 200);

                    if (prevPosition.x > gridPosition.x) {
                        texture = walkLeft.getKeyFrame(stateTime, true);
                    } else {
                        texture = walkRight.getKeyFrame(stateTime, true);
                    }
                }

                batch.draw(texture, x, y);
            }
        }
    }

    public void seekJob() {
        Optional<ProductionBuilding> bestJobOptional = World.getBuildings().stream()
                .filter(building -> building instanceof ProductionBuilding)
                .map(building -> (ProductionBuilding) building)
                .filter(ProductionBuilding::isHiring)
                .max(Comparator.comparing(ProductionBuilding::getJobQuality));

        if (bestJobOptional.isPresent()) {
            final ProductionBuilding bestJob = bestJobOptional.get();

            if (workplace == null) {
                bestJob.addEmployee(this);
                workplace = bestJob;
            } else if (bestJob.getJobQuality() > workplace.getJobQuality()) {
                workplace.removeEmployee(this);
                bestJob.addEmployee(this);
                workplace = bestJob;
            }
        }
    }

    public void seekHouse() {
        Optional<ResidentialBuilding> bestHouseOptional = World.getBuildings().stream()
                .filter(building -> building instanceof ResidentialBuilding)
                .map(building -> (ResidentialBuilding) building)
                .filter(ResidentialBuilding::hasFreePlaces)
                .min(Comparator.comparingInt(building -> building.getGridPosition().distanceScore(gridPosition)));

        if (bestHouseOptional.isPresent()) {
            ResidentialBuilding bestHouse = bestHouseOptional.get();

            if (home == null) {
                bestHouse.addResident(this);
                home = bestHouse;
            } else if (workplace != null &&
                    home.getGridPosition().distanceScore(workplace.getGridPosition()) < bestHouse.getGridPosition().distanceScore(workplace.getGridPosition())) {
                home.removeResident(this);
                bestHouse.addResident(this);
                home = bestHouse;
            }
        }
    }

    public void enterBuilding(StorageBuilding building) {
        if (gridPosition.equals(building.getEntrancePosition())) {
            gridPosition.set(building.getGridPosition());
            buildingIsIn = building;
        }
    }

    public void exitBuilding() {
        StorageBuilding building = StorageBuilding.getByCoordinates(gridPosition);
        if (building != null) {
            gridPosition.set(building.getEntrancePosition());
        }
        buildingIsIn = null;
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void navigateTo(Vector2i tile) {
        alignSprite();
        path = null;
        pathfinding = executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition.clone(), tile);
            pathStep = 0;
            nextStep = STEP_INTERVAL;
        });
    }

    private void navigateTo(BoxCollider collider) {
        alignSprite();
        path = null;
        pathfinding = executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition.clone(), collider.cloneAndTranslate(Vector2i.zero()));
            pathStep = 0;
            nextStep = STEP_INTERVAL;
        });
    }

    /**
     * Follows along a path created with the {@code navigateTo()} method.
     *
     * @return <b>true</b> if destination is reached, <b>false</b> otherwise.
     */
    private boolean followPath() {
        if (path == null || !pathfinding.isDone()) {
            return false;
        }

        float speedModifier = World.getTile(gridPosition).speed;
        if (nextStep >= STEP_INTERVAL / speedModifier) {
            if (pathStep == path.length - 1) {
                alignSprite();
                return true;
            }
            if (!World.getNavigableTiles().contains(path[pathStep + 1])) {
                navigateTo(path[path.length - 1]);
                return false;
            }
            pathStep++;
            prevPosition = gridPosition.clone();
            gridPosition.set(path[pathStep]);
            nextStep = 0;
        }
        nextStep++;
        spritePosition.add(((gridPosition.x - prevPosition.x) / (float) STEP_INTERVAL) * speedModifier,
                ((gridPosition.y - prevPosition.y) / (float) STEP_INTERVAL) * speedModifier);
        return false;
    }

    private void alignSprite() {
        spritePosition.set(gridPosition.x, gridPosition.y);
        prevPosition.set(gridPosition);
    }

    @Override
    public boolean isMouseOver() {
        Vector3 mousePos = GameScreen.getMouseWorldPosition();
        return mousePos.x >= spritePosition.x * World.TILE_SIZE && mousePos.x < (spritePosition.x * World.TILE_SIZE + texture.getRegionWidth()) &&
                mousePos.y >= spritePosition.y * World.TILE_SIZE && mousePos.y < (spritePosition.y * World.TILE_SIZE + texture.getRegionHeight());
    }

    @Override
    public void onClick() {
        UI.showNPCStatWindow(this);
    }

    public static abstract class Order implements Serializable {
        abstract void execute();

        public enum Type {
            GO_TO,
            ENTER,
            EXIT,
            PUT_RESOURCES_TO_BUILDING,
            PUT_RESERVED_RESOURCES,
            TAKE_RESERVED_RESOURCES,
            REQUEST_TRANSPORT,
            END_DELIVERY
        }
    }

    public void executeOrders() {
        if (!orderList.isEmpty()) {
            orderList.getFirst().execute();
        }
    }

    public void clearOrderQueue() {
        orderList.clear();
    }

    public void giveOrder(Vector2i tile) {
        orderList.addLast(new Order() {
            @Override
            void execute() {
                if (pathfinding == null || pathfinding.isDone() && (path == null || !path[path.length - 1].equals(tile))) {
                    navigateTo(tile);
                } else if (followPath()) {
                    orderList.removeFirst();
                }
            }
        });
    }

    public void giveOrder(Order.Type type, StorageBuilding building) {
        switch (type) {
            case GO_TO:
                giveOrder(building.getEntrancePosition());
                break;
            case ENTER:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (gridPosition.equals(building.getEntrancePosition())) {
                            if (building == workplace) {
                                workplace.employeeEnter(NPC.this);
                            }
                            enterBuilding(building);
                        }
                        orderList.removeFirst();
                    }
                });
                break;
            case EXIT:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (gridPosition.equals(building.getGridPosition())) {
                            if (building == workplace) {
                                workplace.employeeExit();
                            }
                            gridPosition.set(building.getEntrancePosition());
                            buildingIsIn = null;
                        }
                        orderList.removeFirst();
                    }
                });
                break;
        }
    }

    public void giveOrder(Order.Type type, FieldWork fieldWork) {
        switch (type) {
            case GO_TO:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (pathfinding == null || pathfinding.isDone() && (path == null || fieldWork.getCollider().distance(path[path.length - 1]) > Math.sqrt(2d))) {
                            navigateTo(fieldWork.getCollider());
                        } else if (followPath()) {
                            orderList.removeFirst();
                        }
                    }
                });
                break;
            case ENTER:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (fieldWork.getCollider().distance(gridPosition) <= Math.sqrt(2d)) {
                            fieldWork.setWork(NPC.this, true);
                        }
                        orderList.removeFirst();
                    }
                });
                break;
            case EXIT:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        fieldWork.dissociateWorker(NPC.this);
                        orderList.removeFirst();
                    }
                });
                break;
        }
    }

    public void giveOrder(Order.Type type) {
        switch (type) {
            case EXIT:  //if building is known, giveOrder(Order.Type, StorageBuilding) should be used instead
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        exitBuilding();
                        orderList.removeFirst();
                    }
                });
                break;
            case END_DELIVERY:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        Logistics.getDeliveryList().remove(NPC.this);
                        orderList.removeFirst();
                    }
                });
                break;
        }
    }

    public void giveOrder(Order.Type type, Resource resource, int amount) {
        switch (type) {
            case PUT_RESOURCES_TO_BUILDING:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (buildingIsIn == null || !inventory.hasResourceAmount(resource, amount))
                            throw new IllegalStateException();

                        inventory.moveResourcesTo(buildingIsIn.getInventory(), resource, amount);
                        orderList.removeFirst();
                    }
                });
                break;
            case TAKE_RESERVED_RESOURCES:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (buildingIsIn == null)
                            throw new IllegalStateException();

                        buildingIsIn.moveReservedResourcesTo(inventory, resource, amount, NPC.INVENTORY_SIZE);
                        orderList.removeFirst();
                    }
                });
                break;
            case PUT_RESERVED_RESOURCES:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (buildingIsIn == null)
                            throw new IllegalStateException();

                        buildingIsIn.moveReservedResourcesTo(inventory, resource, -amount, NPC.INVENTORY_SIZE);
                        orderList.removeFirst();
                    }
                });
                break;
            case REQUEST_TRANSPORT:
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (buildingIsIn == null)
                            throw new IllegalStateException();

                        Logistics.requestTransport(buildingIsIn, resource, amount);
                        orderList.removeFirst();
                    }
                });
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Vector2 getSpritePosition() {
        return spritePosition;
    }

    public ProductionBuilding getWorkplace() {
        return workplace;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Job getJob() {
        if (workplace == null)
            return Jobs.UNEMPLOYED;
        else
            return workplace.getJob();
    }

    public ResidentialBuilding getHome() {
        return home;
    }

    public int[] getStats() {
        return stats;
    }

    public boolean isInBuilding() {
        return buildingIsIn != null;
    }

    public StorageBuilding getCurrentBuilding() {
        return buildingIsIn;
    }

    public TextureRegion getIdleTexture() {
        return idleTexture;
    }

    public boolean hasOrders() {
        return !orderList.isEmpty();
    }

    @Override
    public String toString() {
        return name + " " + surname;
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        idleTexture = Textures.get(Enum.valueOf(Textures.Npc.class, "IDLE" + textureId));
        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT" + textureId));
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT" + textureId));
        texture = idleTexture;
    }
}
