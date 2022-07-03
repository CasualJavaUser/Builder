package com.boxhead.builder;

import java.util.HashMap;
import java.util.HashSet;

public class NPC {
    String name;
    int age, health;
    Jobs job;
    ResidentialBuilding home;

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
            if (start.hashCode() == destination.hashCode()) {
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