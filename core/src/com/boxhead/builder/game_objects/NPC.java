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
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NPC extends GameObject implements Clickable {
    public static final int NEED_SATISFACTION = 50;
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    private final String name, surname;
    private final int[] stats = new int[Stats.values().length];
    private Jobs job;
    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private boolean inBuilding;

    private Vector2i prevPosition;
    private final Vector2 spritePosition;
    private int pathStep;   //how far into Vector2i[] path has been travelled
    private Vector2i[] path = null;
    private Pathfinding.Destination destination = null;
    private int stepInterval, nextStep;

    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

    public enum Stats {
        AGE,
        HEALTH
    }

    public NPC(TextureRegion texture, Vector2i position) {
        super(texture, position);
        prevPosition = position;
        spritePosition = position.toVector2();
        job = Jobs.UNEMPLOYED;
        stepInterval = 50;
        name = NAMES[(int) (Math.random() * NAMES.length)];
        surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];
    }

    public void navigateTo(Vector2i gridTile) {
        alignSprite();
        executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition, gridTile);
            pathStep = 0;
            nextStep = stepInterval;
        });
    }

    public void navigateTo(EnterableBuilding building) {
        alignSprite();
        executor.submit(() -> {
            if (building != null) {
                path = Pathfinding.findPath(gridPosition, building.getEntrancePosition());
                path[path.length - 1] = building.getGridPosition();
                pathStep = 0;
            } else {
                path = null;
            }
            nextStep = stepInterval;
        });
    }

    public void navigateTo(Object interest) {
        alignSprite();
        Vector2i destination = null;
        EnterableBuilding enterable = null;
        for (Building building : World.getBuildings()) {
            if (building instanceof FieldWork && ((FieldWork) building).getCharacteristic() == interest && ((FieldWork) building).isFree()) {
                ((FieldWork) building).assignWorker(this);
                destination = ((EnterableBuilding) building).getEntrancePosition();
                enterable = (EnterableBuilding) building;
                break;
            }
        }
        for (Harvestable harvestable : World.getHarvestables()) {
            if (harvestable.getCharacteristic() == interest && harvestable.isFree()) {
                harvestable.assignWorker(this);
                destination = harvestable.getGridPosition();
                break;
            }
        }

        if (destination == null) return;    //no PoI available

        Vector2i finalDestination = destination;   //lambdas require effectively final arguments
        EnterableBuilding finalEnterable = enterable;
        executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition, finalDestination);
            pathStep = 0;
            if (finalEnterable != null) {
                path[path.length - 1].set(finalEnterable.getGridPosition());
            }
        });
    }

    /**
     * Follows along a path specified by the {@code navigateTo()} method.
     *
     * @return <b>true</b> if the NPC moved, <b>false</b> if no path is specified or the end of path is reached
     */

    public boolean followPath() {
        if (path == null) {
            return false;   //no path specified
        }
        if (pathStep >= path.length - 1) {
            path = null;
            return false;   //reached the destination
        }

        if (nextStep >= stepInterval) {
            if (pathStep == path.length - 2 && path[path.length - 1] != path[path.length - 2]) {    //enter building
                if (destination == Pathfinding.Destination.SERVICE) {
                    enterBuilding(EnterableBuilding.getByCoordinates(path[path.length - 1]), true);
                } else if (destination == Pathfinding.Destination.HOME || destination == Pathfinding.Destination.WORK) {
                    enterBuilding(EnterableBuilding.getByCoordinates(path[path.length - 1]));
                } else if (destination == Pathfinding.Destination.FIELD_WORK) {
                    Harvestable harvestable = Harvestable.getByCoordinates(path[path.length - 1]);
                    if (harvestable != null) {
                        harvestable.setWork(this, true);
                    }
                    EnterableBuilding building = EnterableBuilding.getByCoordinates(path[path.length - 1]);
                    if (building != null) {
                        ((FieldWork) building).setWork(this, true);
                    }
                }
                path = null;
                destination = null;
                return true;
            }
            if (!World.getNavigableTiles().contains(path[pathStep + 1])) {
                navigateTo(path[path.length - 1]);
            }
            pathStep++;
            prevPosition = gridPosition.clone();
            gridPosition.set(path[pathStep]);
            nextStep = 0;
        }
        nextStep++;
        spritePosition.add((gridPosition.x - prevPosition.x) * (1f / (float) stepInterval),
                (gridPosition.y - prevPosition.y) * (1f / (float) stepInterval));

        return true;
    }

    /**
     * Makes this NPC enter the specified EnterableBuilding and assigns it as either an employee or a guest.
     * To enter, the NPC needs to be at the building's entrance tile.
     * If the building is full, call to this method has no effect.
     *
     * @param building the building to be entered
     * @param guest    (optional) {@code true} if the building is to be entered as a guest.
     */

    public void enterBuilding(EnterableBuilding building, boolean... guest) {
        if (building == null) return;

        boolean entered = true;
        if (gridPosition.equals(building.getEntrancePosition())) {
            if (building instanceof ServiceBuilding) {
                if (guest.length > 0 && guest[0]) {
                    entered = ((ServiceBuilding) building).addGuest(this);
                }
            } else if (building instanceof ProductionBuilding) {
                entered = ((ProductionBuilding) building).employeeEnter(this);
            }

            if (entered) {
                inBuilding = true;
                path = null;
                gridPosition.set(building.getGridPosition());
            }
        }
    }

    public void exitBuilding() {
        if (!inBuilding) {
            return;
        }

        for (Building building : World.getBuildings()) {
            if (building.getGridPosition().equals(gridPosition) && building instanceof EnterableBuilding) {
                gridPosition.set(((EnterableBuilding) building).getEntrancePosition());
                inBuilding = false;
            }
        }
    }

    public void seekJob() {
        if (workplace != null) {
            workplace.removeEmployee(this); //this NPC gets fired so that it's current job is included in the search
        }

        ProductionBuilding bestJob = null;
        int bestQuality = Integer.MIN_VALUE;
        for (Building building : World.getBuildings()) {
            if (building instanceof ProductionBuilding && ((ProductionBuilding) building).getEmployeeCount() < ((ProductionBuilding) building).getEmployeeCapacity()) {
                if (((ProductionBuilding) building).getJobQuality() > bestQuality) {
                    bestJob = (ProductionBuilding) building;
                    bestQuality = bestJob.getJobQuality();
                }
            }
        }

        if (bestJob != null) {
            workplace = bestJob;
            workplace.addEmployee(this);
            job = workplace.getJob();
        }   //else - no work is available
    }

    public void seekHouse() {
        if (home != null) {
            home.removeResident(this);  //similar to seekJob()
        }

        ResidentialBuilding closestHouse = null;
        double distance = Double.MAX_VALUE;
        for (Building building : World.getBuildings()) {
            if (building instanceof ResidentialBuilding && ((ResidentialBuilding) building).getResidentCount() < ((ResidentialBuilding) building).getResidentCapacity()) {
                if (gridPosition.distance(building.getGridPosition()) < distance) {
                    closestHouse = (ResidentialBuilding) building;
                    distance = gridPosition.distance(building.getGridPosition());
                }
            }
        }

        if (closestHouse != null) {
            home = closestHouse;
            closestHouse.addResident(this);
        }   //else - no houses available
    }

    public void seekService() {
        for (NPC.Stats stat : NPC.Stats.values()) {
            if (stats[stat.ordinal()] < NEED_SATISFACTION) {
                ServiceBuilding closestService = null;
                double closestDistance = Double.MAX_VALUE;
                for (Building building : World.getBuildings()) {
                    if (building instanceof ServiceBuilding &&
                            ((ServiceBuilding) building).provides(stat) &&
                            ((ServiceBuilding) building).getGuestsInside() < ((ServiceBuilding) building).getGuestCapacity() &&
                            gridPosition.distance(building.getGridPosition()) < closestDistance) {
                        closestService = (ServiceBuilding) building;
                        closestDistance = gridPosition.distance(closestService.getGridPosition());
                    }
                }
                if (closestService != null) {
                    navigateTo(closestService);
                    setDestination(Pathfinding.Destination.SERVICE);
                    return;
                }
            }
        }
    }

    public void setDestination(Pathfinding.Destination destination) {
        this.destination = destination;
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

    public Pathfinding.Destination getDestination() {
        return destination;
    }

    public Vector2i getDestinationTile() {
        if (path == null) return null;
        return path[path.length - 1];
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

        public static Vector2i[] findPath(Vector2i start, Vector2i destination) {     //Dijkstra's algorithm
            if (start.equals(destination) || !World.getNavigableTiles().contains(start)) {
                return new Vector2i[]{start, start};
            }
            HashSet<Vector2i> unvisitedTiles = new HashSet<>(World.getNavigableTiles());
            HashMap<Vector2i, Double> distanceToTile = new HashMap<>();
            HashMap<Vector2i, Vector2i> parentTree = new HashMap<>();
            for (Vector2i tile : World.getNavigableTiles()) {
                distanceToTile.put(tile, Double.MAX_VALUE);
            }
            distanceToTile.remove(start);
            distanceToTile.put(start, 0d);

            int x = start.x, y = start.y;
            Vector2i currentTile = new Vector2i(x, y);
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
                    return new Vector2i[]{start, start};   //no path
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
            while (currentTile.hashCode() != start.hashCode()) {    //todo fix NPE
                currentTile = parentTree.get(currentTile);
                totalDistance++;
            }

            currentTile = destination;
            Vector2i[] finalPath = new Vector2i[totalDistance + 1];
            for (int i = totalDistance - 1; i >= 0; i--) {
                finalPath[i] = currentTile;
                currentTile = parentTree.get(currentTile);
            }
            finalPath[totalDistance] = finalPath[totalDistance - 1].clone();
            return finalPath;
        }

        private static void calcDistance(HashSet<Vector2i> unvisitedTiles,
                                         HashMap<Vector2i, Double> distanceToTile,
                                         HashMap<Vector2i, Vector2i> parentTree,
                                         Vector2i currentTile,
                                         Vector2i tempTile,
                                         double distance) {
            if (unvisitedTiles.contains(tempTile) && distanceToTile.get(tempTile) > distanceToTile.get(currentTile) + distance) {
                distanceToTile.remove(tempTile);
                distanceToTile.put(tempTile, distanceToTile.get(currentTile) + distance);
                parentTree.remove(tempTile);
                parentTree.put(tempTile, currentTile);
            }
        }

        public enum Destination {
            HOME,
            WORK,
            FIELD_WORK,
            SERVICE
        }
    }
}