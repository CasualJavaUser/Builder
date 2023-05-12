package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
import java.util.*;

import static com.boxhead.builder.Stat.*;

public class Villager extends NPC implements Clickable {
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    public static final int INVENTORY_SIZE = 10;

    private final String name, surname;
    private final float[] stats = new float[Stat.values().length];
    private final int skin;

    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private StorageBuilding buildingIsIn = null;
    private boolean clockedIn = false;

    private final LinkedList<Order> orderList = new LinkedList<>();

    private final Inventory inventory = new Inventory(INVENTORY_SIZE);

    public Villager(int skin, Vector2i gridPosition) {
        super(Textures.Npc.valueOf("IDLE" + skin), gridPosition);
        this.skin = skin;
        name = NAMES[(int) (Math.random() * NAMES.length)];
        surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];

        for (int i = 0; i < stats.length; i++) {
            stats[i] = Stat.values()[i].initVal;
        }

        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT" + skin));
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT" + skin));
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!isInBuilding()) {
            super.draw(batch);
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

    public ServiceBuilding seekNearestService(Buildings.Type type) {
        if (!type.isService())
            throw new IllegalArgumentException("Given type is not service");

        Optional<ServiceBuilding> bestServiceOptional = World.getBuildings().stream()
                .filter(building -> building instanceof ServiceBuilding)
                .filter(building -> building.type == type)
                .map(building -> (ServiceBuilding) building)
                .filter(ServiceBuilding::canProvideService)
                .filter(ServiceBuilding::hasFreeSpaces)
                .min(Comparator.comparingInt(building -> building.getGridPosition().distanceScore(gridPosition)));

        return bestServiceOptional.orElse(null);
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

    @Override
    public void wander() {
        if (World.getRandom().nextInt(360) == 0) {
            Vector2i randomPos = randomPosInRange(5);
            if (World.getNavigableTiles().contains(randomPos))
                giveOrder(randomPos);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isMouseOver() {
        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        return mousePos.x >= spritePosition.x * World.TILE_SIZE && mousePos.x < (spritePosition.x * World.TILE_SIZE + currentTexture.getRegionWidth()) &&
                mousePos.y >= spritePosition.y * World.TILE_SIZE && mousePos.y < (spritePosition.y * World.TILE_SIZE + currentTexture.getRegionHeight());
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
            CLOCK_IN,
            CLOCK_OUT,
            PUT_RESOURCES_TO_BUILDING,
            PUT_RESERVED_RESOURCES,
            TAKE_RESERVED_RESOURCES,
            REQUEST_TRANSPORT,
            END_DELIVERY,
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
            case GO_TO -> giveOrder(building.getEntrancePosition());
            case ENTER -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (gridPosition.equals(building.getEntrancePosition())) {
                        if (building == workplace) {
                            workplace.employeeEnter(Villager.this);
                        }
                        enterBuilding(building);
                    }
                    orderList.removeFirst();
                }
            });
            case EXIT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (gridPosition.equals(building.getGridPosition())) {
                        if (building == workplace) {
                            workplace.employeeExit();
                        }
                        if (building instanceof ServiceBuilding serviceBuilding && serviceBuilding.getGuests().contains(Villager.this)) {
                            serviceBuilding.guestExit(Villager.this);
                        }
                        gridPosition.set(building.getEntrancePosition());
                        buildingIsIn = null;
                    }
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(ServiceBuilding serviceBuilding) {
        orderList.addLast(new Order() {
            @Override
            void execute() {
                if (gridPosition.equals(serviceBuilding.getEntrancePosition())) {
                    serviceBuilding.guestEnter(Villager.this);
                    enterBuilding(serviceBuilding);
                }
                orderList.removeFirst();
            }
        });
    }

    public void giveOrder(Order.Type type, FieldWork fieldWork) {
        switch (type) {
            case GO_TO -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (pathfinding == null || pathfinding.isDone() && (path == null || fieldWork.getCollider().distance(path[path.length - 1]) > Math.sqrt(2d))) {
                        navigateTo(fieldWork.getCollider());
                    } else if (followPath()) {
                        orderList.removeFirst();
                    }
                }
            });
            case ENTER -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (fieldWork.getCollider().distance(gridPosition) <= Math.sqrt(2d)) {
                        fieldWork.setWork(Villager.this, true);
                    }
                    orderList.removeFirst();
                }
            });
            case EXIT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    fieldWork.dissociateWorker(Villager.this);
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(Order.Type type) {
        switch (type) {
            case EXIT ->  //if building is known, giveOrder(Order.Type, StorageBuilding) should be used instead
                    orderList.addLast(new Order() {
                        @Override
                        void execute() {
                            exitBuilding();
                            orderList.removeFirst();
                        }
                    });
            case END_DELIVERY -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    Logistics.getDeliveryList().remove(Villager.this);
                    orderList.removeFirst();
                }
            });
            case CLOCK_IN -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    clockedIn = true;
                    orderList.removeFirst();
                }
            });
            case CLOCK_OUT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    clockedIn = false;
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(Order.Type type, Resource resource, int amount) {
        switch (type) {
            case PUT_RESOURCES_TO_BUILDING -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null || !inventory.hasResourceAmount(resource, amount))
                        throw new IllegalStateException();

                    inventory.moveResourcesTo(buildingIsIn.getInventory(), resource, amount);
                    orderList.removeFirst();
                }
            });
            case TAKE_RESERVED_RESOURCES -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    buildingIsIn.moveReservedResources(Villager.this, buildingIsIn.inventory, inventory, resource, amount);
                    orderList.removeFirst();
                }
            });
            case PUT_RESERVED_RESOURCES -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    buildingIsIn.moveReservedResources(Villager.this, inventory, buildingIsIn.inventory, resource, amount);
                    orderList.removeFirst();
                }
            });
            case REQUEST_TRANSPORT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    Logistics.requestTransport(buildingIsIn, resource, amount);
                    orderList.removeFirst();
                }
            });
            default -> throw new IllegalArgumentException();
        }
    }

    public void giveOrder(Harvestable harvestable) {
        orderList.addLast(new Order() {
            @Override
            void execute() {
                World.placeFieldWork(harvestable);
                orderList.removeFirst();
            }
        });
    }

    public void progressStats() {
        stats[HUNGER.ordinal()] += HUNGER.rate;

        if (clockedIn)
            stats[TIREDNESS.ordinal()] += TIREDNESS.rate * 2;
        else if (home != null && home.equals(buildingIsIn))
            stats[TIREDNESS.ordinal()] -= TIREDNESS.rate * 2;
        else
            stats[TIREDNESS.ordinal()] += TIREDNESS.rate;

        stats[HEALTH.ordinal()] += HEALTH.rate;
    }

    public void fulfillNeeds() {
        for (int i = 0; i < stats.length; i++) {
            Stat stat = Stat.values()[i];
            float threshold = isClockedIn() ? stat.critical : stat.urgent;

            boolean condition;
            if (stat.isIncreasing) {
                condition = stats[i] >= threshold;  //for stats that increase over time (e.g. hunger)
            } else {
                condition = stats[i] <= threshold;  //for stats that decrease over time (e.g. health)
            }

            if (condition) {
                stat.fulfillNeed(this);
            }
        }
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

    public float[] getStats() {
        return stats;
    }

    public boolean isInBuilding() {
        return buildingIsIn != null;
    }

    public boolean isInBuilding(StorageBuilding building) {
        return buildingIsIn == building;
    }

    public boolean isClockedIn() {
        return clockedIn;
    }

    public StorageBuilding getCurrentBuilding() {
        return buildingIsIn;
    }

    public boolean hasOrders() {
        return !orderList.isEmpty();
    }

    @Override
    public String toString() {
        String homeType = null;
        String workplaceType = null;
        if (home != null) homeType = home.getType().toString();
        if (workplace != null) workplaceType = workplace.getType().toString();

        String s =  name + " " + surname + "\nage: " + ageInYears();

        String stat;
        for (int i = 0; i < Stat.values().length; i++) {
            stat = Stat.values()[i].toString().toLowerCase() + ": " + (int)stats[i];
            s = s.concat("\n" + stat);
        }

        s += "\norder list size: " + orderList.size() +
                "\nclocked in: " + clockedIn +
                "\nhome: " + homeType +
                "\nworkplace: " + workplaceType +
                "\nis in building: " + isInBuilding() +
                "\nbuilding is in: " + buildingIsIn +
                "\ninventory: " + inventory;

        return s;
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = Textures.Npc.valueOf("IDLE" + skin);
        walkLeft = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_LEFT" + skin));
        walkRight = Textures.getAnimation(Enum.valueOf(Textures.NpcAnimation.class, "WALK_RIGHT" + skin));
        currentTexture = getTexture();
    }
}
