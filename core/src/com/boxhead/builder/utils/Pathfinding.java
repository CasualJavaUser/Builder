package com.boxhead.builder.utils;

import com.boxhead.builder.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class Pathfinding {
    public static Vector2i[] findPath(final Vector2i start, final Vector2i destination) {
        return dijkstra(start, Predicate.isEqual(destination));
    }

    public static Vector2i[] findPath(final Vector2i start, final BoxCollider area) {
        return dijkstra(start, area.extended()::overlaps);
    }

    private static Vector2i[] dijkstra(Vector2i start, Predicate<Vector2i> destination) {
        if (destination.test(start) || !World.getNavigableTiles().contains(start)) {
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

        while (!destination.test(currentTile)) {
            float distanceModifier = 1f / World.getTile(currentTile).speed;

            tempTile = new Vector2i(x + 1, y);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x - 1, y);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x, y + 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x, y - 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x + 1, y + 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2) * distanceModifier);
            tempTile = new Vector2i(x - 1, y + 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2) * distanceModifier);
            tempTile = new Vector2i(x + 1, y - 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2) * distanceModifier);
            tempTile = new Vector2i(x - 1, y - 1);
            calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2) * distanceModifier);

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

        Vector2i lastTile = currentTile;
        int totalDistance = 1;
        while (!currentTile.equals(start)) {
            currentTile = parentTree.get(currentTile);
            totalDistance++;
        }

        currentTile = lastTile;
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
