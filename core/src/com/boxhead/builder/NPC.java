package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NPC implements Clickable{
    public static final int NEED_SATISFACTION = 50;
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    private final TextureRegion texture;
    private String name, surname;
    private final int[] stats = new int[Stats.values().length];
    private Jobs job;
    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private boolean inBuilding;

    private Vector2i position, prevPosition;
    private Vector2 spritePosition;
    private int pathStep;   //how far into Vector2i[] path has been travelled
    private Vector2i[] path = null;
    private int stepInterval, nextStep;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);

    public enum Stats {
        AGE,
        HEALTH
    }

    public NPC(TextureRegion texture, Vector2i position) {
        this.texture = texture;
        this.position = position;
        prevPosition = position;
        spritePosition = position.toVector2();
        job = Jobs.UNEMPLOYED;
        stepInterval = 50;
        name = NAMES[(int)(Math.random() * NAMES.length)];
        surname = SURNAMES[(int)(Math.random() * SURNAMES.length)];
    }

    public void navigateTo(Vector2i gridTile) {
        executor.submit(() -> {
            path = Pathfinding.findPath(position, gridTile);
            pathStep = 0;
            nextStep = stepInterval;
        });
    }

    public void navigateTo(EnterableBuilding building) {
        executor.submit(() -> {
            if (building != null) {
                path = Pathfinding.findPath(position, building.getEntrancePosition());
                path[path.length - 1] = building.getPosition();
                pathStep = 0;
            } else {
                path = null;
            }
            nextStep = stepInterval;
        });
    }

    /**
     * Follows along a path specified by the {@code navigateTo()} method.
     *
     * @return true if the NPC moved, false if no path is specified or the end of path is reached
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
            if (pathStep == path.length - 2 && path[path.length - 1] != path[path.length - 2]) {
                EnterableBuilding building = EnterableBuilding.getByCoordinates(path[path.length - 1]);
                if (building == null) {
                    path = null;
                    return false;   //building was probably deleted or moved
                }
                enterBuilding(building);
                return true;
            }
            if (!World.getNavigableTiles().contains(path[pathStep + 1])) {
                navigateTo(EnterableBuilding.getByCoordinates(path[path.length - 1]));
            }
            pathStep++;
            prevPosition = position.clone();
            position = path[pathStep];
            nextStep = 0;
        }
        nextStep++;
        spritePosition.add((position.x - prevPosition.x) * (1f / (float) stepInterval),
                (position.y - prevPosition.y) * (1f / (float) stepInterval));

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
        boolean entered = true;
        if (position.equals(building.getEntrancePosition())) {
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
                position = building.getPosition();
            }
        }
    }

    public void exitBuilding() {
        if (!inBuilding) {
            return;
        } else if (position.equals(workplace.getPosition())) {
            workplace.employeeExit();
            position = workplace.getEntrancePosition();
            inBuilding = false;
            return;
        }

        for (Building building : World.getBuildings()) {
            if (building.getPosition().equals(position) && building instanceof EnterableBuilding) {
                position = building.getPosition().clone();
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
            home.removeResident();  //similar to seekJob()
        }

        ResidentialBuilding closestHouse = null;
        double distance = Double.MAX_VALUE;
        for (Building building : World.getBuildings()) {
            if (building instanceof ResidentialBuilding && ((ResidentialBuilding) building).getResidentCount() < ((ResidentialBuilding) building).getResidentCapacity()) {
                if (position.distance(building.getPosition()) < distance) {
                    closestHouse = (ResidentialBuilding) building;
                    distance = position.distance(building.getPosition());
                }
            }
        }

        if (closestHouse != null) {
            home = closestHouse;
            closestHouse.addResident();
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
                            ((ServiceBuilding) building).getGuestCount() < ((ServiceBuilding) building).getGuestCapacity() &&
                            position.distance(building.getPosition()) < closestDistance) {
                        closestService = (ServiceBuilding) building;
                        closestDistance = position.distance(closestService.getPosition());
                    }
                }
                if (closestService != null) {
                    navigateTo(closestService);
                    return;
                }
            }
        }
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public Vector2i getPosition() {
        return position;
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

    public void setHome(ResidentialBuilding house) {
        home = house;
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

    @Override
    public boolean isClicked() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = GameScreen.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            return mousePos.x >= position.x * World.TILE_SIZE && mousePos.x < (position.x * World.TILE_SIZE + texture.getRegionWidth()) &&
                    mousePos.y >= position.y * World.TILE_SIZE && mousePos.y < (position.y * World.TILE_SIZE + texture.getRegionHeight());
        }
        return false;
    }

    @Override
    public void onClick() {
        UI.showStatWindow(this);
    }

    public static class Pathfinding {

        public static Vector2i[] findPath(Vector2i start, Vector2i destination) {     //Dijkstra's algorithm
            if (start.equals(destination) || !World.getNavigableTiles().contains(start)) {
                return new Vector2i[]{start};
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
            while (currentTile.hashCode() != start.hashCode()) {
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
    }
}