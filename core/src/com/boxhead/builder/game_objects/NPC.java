package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.FieldWork;
import com.boxhead.builder.Jobs;
import com.boxhead.builder.World;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NPC extends GameObject implements Clickable {
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    private static final int STEP_INTERVAL = 50;

    private final String name, surname;
    private final int[] stats = new int[Stats.values().length];
    private Jobs job;
    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private boolean inBuilding = false;

    private Vector2i prevPosition;
    private final Vector2 spritePosition;
    private int nextStep;

    private Vector2i[] path = null;
    private int pathStep;
    private boolean waitingForPath = false;

    private final LinkedList<NPC.Order> orderList = new LinkedList<>();

    public enum Stats {
        AGE,
        HEALTH
    }

    public NPC(TextureRegion texture, Vector2i position) {
        super(texture, position);
        prevPosition = position;
        spritePosition = position.toVector2();
        job = Jobs.UNEMPLOYED;
        name = NAMES[(int) (Math.random() * NAMES.length)];
        surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];
    }

    public void seekJob() {
        ProductionBuilding bestJob = null;
        int bestQuality = Integer.MIN_VALUE;
        for (Building building : World.getBuildings()) {
            if (building instanceof ProductionBuilding && ((ProductionBuilding) building).isHiring() && ((ProductionBuilding) building).getJobQuality() > bestQuality) {
                bestJob = (ProductionBuilding) building;
                bestQuality = bestJob.getJobQuality();
            }
        }

        if (bestJob == null) {
            return; //no free workplaces
        }
        if (workplace == null) {
            bestJob.addEmployee(this);
            workplace = bestJob;
            job = workplace.getJob();
        } else if (bestQuality > workplace.getJobQuality()) {
            workplace.removeEmployee(this);
            bestJob.addEmployee(this);
            workplace = bestJob;
            job = workplace.getJob();
        }
    }

    public void seekHouse() {
        Vector2i location;
        if (workplace == null) {
            location = gridPosition;
        } else {
            location = workplace.getEntrancePosition();
        }

        ResidentialBuilding closest = null;
        double smallestDistance = Double.MAX_VALUE;
        for (Building building : World.getBuildings()) {
            if (building instanceof ResidentialBuilding && ((ResidentialBuilding) building).hasFreePlaces() && building.getGridPosition().distance(location) < smallestDistance) {
                closest = (ResidentialBuilding) building;
                smallestDistance = building.getGridPosition().distance(location);
            }
        }

        if (closest == null) {
            return; //no free houses
        }
        if (home != null) {
            home.removeResident(this);
        }
        closest.addResident(this);
        home = closest;
    }

    public void enterBuilding(EnterableBuilding building) {
        if (gridPosition.equals(building.getEntrancePosition())) {
            gridPosition.set(building.getGridPosition());
            inBuilding = true;
        }
    }

    public void exitBuilding() {
        EnterableBuilding building = EnterableBuilding.getByCoordinates(gridPosition);
        if (building != null) {
            gridPosition.set(building.getEntrancePosition());
        }
        inBuilding = false;
    }

    private void navigateTo(Vector2i tile) {
        alignSprite();
        path = null;
        waitingForPath = true;
        executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition, tile);
            pathStep = 0;
            nextStep = STEP_INTERVAL;
            waitingForPath = false;
        });
    }

    private void navigateTo(BoxCollider collider) {
        alignSprite();
        path = null;
        waitingForPath = true;
        executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition, collider);
            pathStep = 0;
            nextStep = STEP_INTERVAL;
            waitingForPath = false;
        });
    }

    /**
     * Follows along a path created with the {@code navigateTo()} method.
     *
     * @return <b>true</b> if destination is reached, <b>false</b> otherwise.
     */

    private boolean followPath() {
        if (path == null || waitingForPath) {
            return false;
        }

        if (nextStep >= STEP_INTERVAL) {
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
        spritePosition.add((gridPosition.x - prevPosition.x) * (1f / (float) STEP_INTERVAL),
                (gridPosition.y - prevPosition.y) * (1f / (float) STEP_INTERVAL));
        return false;
    }

    private void alignSprite() {
        spritePosition.set(gridPosition.x, gridPosition.y);
        prevPosition.set(gridPosition);
    }

    @Override
    public boolean isClicked() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = BuilderGame.getGameScreen().getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            return mousePos.x >= gridPosition.x * World.TILE_SIZE && mousePos.x < (gridPosition.x * World.TILE_SIZE + texture.getRegionWidth()) &&
                    mousePos.y >= gridPosition.y * World.TILE_SIZE && mousePos.y < (gridPosition.y * World.TILE_SIZE + texture.getRegionHeight());
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showNPCStatWindow(this);
    }

    public static class Pathfinding {
        public static Vector2i[] findPath(final Vector2i start, final Vector2i destination) {
            if (start.equals(destination) || !World.getNavigableTiles().contains(start)) {
                return new Vector2i[]{start};
            }
            HashSet<Vector2i> unvisitedTiles = new HashSet<>(World.getNavigableTiles());
            HashMap<Vector2i, Double> distanceToTile = new HashMap<>(unvisitedTiles.size(), 1f);
            HashMap<Vector2i, Vector2i> parentTree = new HashMap<>();
            for (Vector2i tile : unvisitedTiles) {
                distanceToTile.put(tile, Double.MAX_VALUE);
            }
            distanceToTile.replace(start, 0d);

            int x = start.x, y = start.y;
            Vector2i currentTile = start.clone();
            Vector2i tempTile;

            while (!currentTile.equals(destination)) {
                tempTile = new Vector2i(x + 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x - 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x + 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x - 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x + 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x - 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));

                unvisitedTiles.remove(currentTile);

                boolean pathExists = false;
                for (Vector2i tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < Double.MAX_VALUE) {
                        pathExists = true;
                        break;
                    }
                }
                if (!pathExists) {
                    return new Vector2i[]{start};   //no path
                }

                Vector2i smallestDistanceTile = null;
                double smallestDistance = Double.MAX_VALUE;
                for (Vector2i tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < smallestDistance) {
                        smallestDistanceTile = tile;
                        smallestDistance = distanceToTile.get(tile);
                    }
                }
                currentTile = smallestDistanceTile;
                x = currentTile.x;
                y = currentTile.y;
            }

            int totalDistance = 1;
            while (!currentTile.equals(start)) {
                currentTile = parentTree.get(currentTile);
                totalDistance++;
            }

            currentTile = destination;
            Vector2i[] finalPath = new Vector2i[totalDistance];
            for (int i = totalDistance - 1; i >= 0; i--) {
                finalPath[i] = currentTile;
                currentTile = parentTree.get(currentTile);
            }
            return finalPath;
        }

        public static Vector2i[] findPath(final Vector2i start, final BoxCollider area) {
            if (area.overlap(start) || !World.getNavigableTiles().contains(start)) {
                return new Vector2i[]{start};
            }
            HashSet<Vector2i> unvisitedTiles = new HashSet<>(World.getNavigableTiles());
            Vector2i colliderTile = new Vector2i();
            for (int y = 0; y < area.getHeight() / World.TILE_SIZE; y++) {
                for (int x = 0; x < area.getWidth() / World.TILE_SIZE; x++) {
                    colliderTile.set(x + area.getGridPosition().x, y + area.getGridPosition().y);
                    unvisitedTiles.add(colliderTile.clone());
                }
            }
            HashMap<Vector2i, Double> distanceToTile = new HashMap<>(unvisitedTiles.size(), 1f);
            HashMap<Vector2i, Vector2i> parentTree = new HashMap<>();
            for (Vector2i tile : unvisitedTiles) {
                distanceToTile.put(tile, Double.MAX_VALUE);
            }
            distanceToTile.replace(start, 0d);

            int x = start.x, y = start.y;
            Vector2i currentTile = start.clone();
            Vector2i tempTile;

            while (!area.overlap(currentTile)) {
                tempTile = new Vector2i(x + 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x - 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Vector2i(x + 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x - 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x + 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Vector2i(x - 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));

                unvisitedTiles.remove(currentTile);

                boolean pathExists = false;
                for (Vector2i tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < Double.MAX_VALUE) {
                        pathExists = true;
                        break;
                    }
                }
                if (!pathExists) {
                    return new Vector2i[]{start};   //no path
                }

                Vector2i smallestDistanceTile = null;
                double smallestDistance = Double.MAX_VALUE;
                for (Vector2i tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < smallestDistance) {
                        smallestDistanceTile = tile;
                        smallestDistance = distanceToTile.get(tile);
                    }
                }
                currentTile = smallestDistanceTile;
                x = currentTile.x;
                y = currentTile.y;
            }

            final Vector2i destination = parentTree.get(currentTile);
            int totalDistance = 0;
            while (!currentTile.equals(start)) {
                currentTile = parentTree.get(currentTile);
                totalDistance++;
            }

            currentTile = destination;
            Vector2i[] finalPath = new Vector2i[totalDistance];
            for (int i = totalDistance - 1; i >= 0; i--) {
                finalPath[i] = currentTile;
                currentTile = parentTree.get(currentTile);
            }
            return finalPath;
        }

        private static void calcDistance(HashSet<Vector2i> unvisitedTiles,
                                         HashMap<Vector2i, Double> distanceToTile,
                                         HashMap<Vector2i, Vector2i> parentTree,
                                         Vector2i currentTile,
                                         Vector2i tempTile,
                                         double distance) {
            if (unvisitedTiles.contains(tempTile) && distanceToTile.get(tempTile) > distanceToTile.get(currentTile) + distance) {
                distanceToTile.replace(tempTile, distanceToTile.get(currentTile) + distance);
                parentTree.remove(tempTile);
                parentTree.put(tempTile, currentTile);
            }
        }
    }

    public static abstract class Order {
        abstract void execute();

        public enum Type {
            GO_TO,
            ENTER,
            EXIT
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
                if (!waitingForPath && (path == null || !path[path.length - 1].equals(tile))) {
                    navigateTo(tile);
                } else if (followPath()) {
                    orderList.removeFirst();
                }
            }
        });
    }

    public void giveOrder(Order.Type type, EnterableBuilding building) {
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
                            inBuilding = false;
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
                        if (!waitingForPath && (path == null || fieldWork.getCollider().distance(path[path.length - 1]) > Math.sqrt(2d))) {
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
                            orderList.removeFirst();
                        }
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
            case EXIT:  //if building is known, giveOrder(Order.Type, EnterableBuilding) should be used instead
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        exitBuilding();
                        orderList.removeFirst();
                    }
                });
                break;
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

    public Jobs getJob() {
        return job;
    }

    public ResidentialBuilding getHome() {
        return home;
    }

    public int[] getStats() {
        return stats;
    }

    public boolean isInBuilding() {
        return inBuilding;
    }
}