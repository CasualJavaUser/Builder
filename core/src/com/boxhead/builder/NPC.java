package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.HashSet;

public class NPC {
    private final Texture texture;
    String name;
    int age, health;
    Jobs job;
    ResidentialBuilding home;

    private Vector2i position;
    private int pathStep;   //how far into Vector2i[] path has been travelled
    private Vector2i[] path = null;

    public NPC(Texture texture, Vector2i position) {
        this.texture = texture;
        this.position = position;
    }

    public void navigateTo(Vector2i gridTile) {
        path = Pathfinding.findPath(position, gridTile);
        pathStep = 0;
    }

    public void navigateTo(Jobs job, World world) {
        double distance = Double.MAX_VALUE;
        Vector2i closestBuilding = null;
        for (Building workplace : world.getBuildings()) {
            if (workplace instanceof ProductionBuilding && ((ProductionBuilding) workplace).job == job) {
                if (position.distance(workplace.position) < distance && ((ProductionBuilding) workplace).employeeCount < ((ProductionBuilding) workplace).employeeCapacity) {
                    distance = position.distance(workplace.position);
                    closestBuilding = workplace.position;
                }
            }
            if (closestBuilding != null) {
                path = Pathfinding.findPath(position, new Vector2i(closestBuilding.x / World.TILE_SIZE, closestBuilding.y / World.TILE_SIZE));
                pathStep = 0;
            } else {
                path = null; //no free workplaces
            }
        }
    }

    public void navigateTo(EnterableBuilding building) {
        path = Pathfinding.findPath(position, building.getEntrancePosition());
        pathStep = 0;
    }

    /**
     * Follows along a path specified by the {@code navigateTo()} method.
     *
     * @return true if the NPC moved, false if no path is specified or the end of path is reached
     */

    public boolean followPath() {
        if (path == null) {
            return false;   //no path specified
        } else if (pathStep == path.length - 1) {
            path = null;
            return false;   //reached the destination
        } else if (!Pathfinding.navigableTiles.contains(path[pathStep + 1])) {
            path = Pathfinding.findPath(position, path[path.length - 1]);
            pathStep = 0;
        }
        pathStep++;
        position = path[pathStep];
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
                entered = ((ProductionBuilding) building).addEmployee(this);
            }

            if (entered) {
                path = null;
                position = building.getPosition();
            }
        }
    }

    public void exitBuilding(World world) {
        for (Building building : world.getBuildings()) {
            if (building.getPosition().equals(position) && building instanceof EnterableBuilding) {
                position = building.getPosition().clone();
            }
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector2i getPosition() {
        return position;
    }

    public static class Pathfinding {
        private static final HashSet<Vector2i> navigableTiles = new HashSet<>();

        public static void reset(int gridWidth, int gridHeight) {
            navigableTiles.clear();
            for (int i = 0; i < gridWidth; i++) {
                for (int j = 0; j < gridHeight; j++) {
                    navigableTiles.add(new Vector2i(i, j));
                }
            }
        }

        public static void makeUnnavigable(int x, int y) {
            navigableTiles.remove(new Vector2i(x, y));
        }

        public static void makeUnnavigable(Vector2i gridPosition) {
            navigableTiles.remove(gridPosition);
        }

        public static void makeNavigable(int x, int y) {
            navigableTiles.add(new Vector2i(x, y));
        }

        public static void makeNavigable(Vector2i gridPosition) {
            navigableTiles.add(gridPosition);
        }

        public static Vector2i[] findPath(Vector2i start, Vector2i destination) {     //Dijkstra's algorithm
            if (start.equals(destination) || !navigableTiles.contains(start)) {
                return new Vector2i[]{start};
            }
            HashSet<Vector2i> unvisitedTiles = new HashSet<>(navigableTiles);
            HashMap<Vector2i, Double> distanceToTile = new HashMap<>();
            HashMap<Vector2i, Vector2i> parentTree = new HashMap<>();
            for (Vector2i tile : navigableTiles) {
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
                distanceToTile.remove(tempTile);
                distanceToTile.put(tempTile, distanceToTile.get(currentTile) + distance);
                parentTree.remove(tempTile);
                parentTree.put(tempTile, currentTile);
            }
        }
    }
}