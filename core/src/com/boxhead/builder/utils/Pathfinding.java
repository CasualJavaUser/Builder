package com.boxhead.builder.utils;

import com.boxhead.builder.Tile;
import com.boxhead.builder.World;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class Pathfinding {
    private static final Map<Pair<Vector2i, Vector2i>, Pair<Vector2i[], Integer>> cache = new HashMap<>();
    public static final float SQRT_2 = 1.41421353816986083984375f;

    public static Vector2i[] findPath(Vector2i start, Vector2i destination) {
        Pair<Vector2i, Vector2i> pair = Pair.of(start, destination);

        if (cache.containsKey(pair)) {
            cache.get(pair).second++;
            return cache.get(pair).first;
        }

        Vector2i[] path = A_star(start, Predicate.isEqual(destination), new HashSet<>(World.getNavigableTiles()), destination::distance);
        cache.put(pair, Pair.of(path, 1));
        return path;
    }

    public static Vector2i[] findPathNoCache(Vector2i start, Vector2i destination) {
        return A_star(start, Predicate.isEqual(destination), new HashSet<>(World.getNavigableTiles()), destination::distance);
    }

    public static Vector2i[] findPath(Vector2i start, BoxCollider area) {
        return A_star(start, area.extended()::overlaps, new HashSet<>(World.getNavigableTiles()), area::distance);
    }

    public static void removeUnusedPaths() {
        for (Object pair : cache.keySet().toArray()) {
            if (cache.get(pair).second.equals(0))
                cache.remove(pair);
        }
        for (Pair<Vector2i, Vector2i> pair : cache.keySet()) {
            cache.get(pair).second--;
        }
    }

    /**
     * Removes all paths containing the given tile from the cache
     */
    public static void updateCache(Vector2i gridPosition) {
        for (Object pair : cache.values().toArray()) {
            Vector2i[] array = ((Pair<Vector2i[], Integer>) pair).first;

            for (Vector2i tile : array) {
                if (tile.equals(gridPosition)) {
                    cache.remove(Pair.of(array[0], array[array.length - 1]));
                    break;
                }
            }
        }
    }

    /**
     * Removes all paths crossing the given area from the cache
     */
    public static void updateCache(BoxCollider collider) {
        for (Object pair : cache.values().toArray()) {
            Vector2i[] array = ((Pair<Vector2i[], Integer>) pair).first;

            for (Vector2i tile : array) {
                if (collider.overlaps(tile)) {
                    cache.remove(Pair.of(array[0], array[array.length - 1]));
                    break;
                }
            }
        }
    }

    private static Vector2i[] A_star(Vector2i start, Predicate<Vector2i> destination, Set<Vector2i> navigableTiles, ToDoubleFunction<Vector2i> distance) {
        Map<Vector2i, Double> distanceToTile = new HashMap<>(navigableTiles.size());
        Map<Vector2i, Vector2i> parentTree = new HashMap<>((int) distance.applyAsDouble(start) * 3);
        SortedSet<Vector2i> semiVisited = new TreeSet<>(Comparator.comparingDouble(vector -> (distance.applyAsDouble(vector) * Tile.minDistanceModifier) + distanceToTile.get(vector))); //tiles to which distances are known

        for (Vector2i tile : navigableTiles) {
            distanceToTile.put(tile, Double.MAX_VALUE);
        }
        distanceToTile.put(start, 0d);
        semiVisited.add(start);

        int x = start.x, y = start.y;
        Vector2i currentTile = start.clone();
        Vector2i tempTile;

        while (!destination.test(currentTile)) {
            float distanceModifier = 1f / World.getTile(currentTile).speed;
            float diagonalDistanceModifier = SQRT_2 * distanceModifier;

            tempTile = new Vector2i(x + 1, y);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x - 1, y);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x, y + 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x, y - 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, distanceModifier);
            tempTile = new Vector2i(x + 1, y + 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, diagonalDistanceModifier);
            tempTile = new Vector2i(x - 1, y + 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, diagonalDistanceModifier);
            tempTile = new Vector2i(x + 1, y - 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, diagonalDistanceModifier);
            tempTile = new Vector2i(x - 1, y - 1);
            calcDistance(navigableTiles, distanceToTile, parentTree, semiVisited, currentTile, tempTile, diagonalDistanceModifier);

            navigableTiles.remove(currentTile);
            semiVisited.remove(currentTile);

            if (semiVisited.isEmpty()) return new Vector2i[]{start};

            currentTile = semiVisited.first();
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

    private static void calcDistance(Set<Vector2i> navigableTiles,
                                     Map<Vector2i, Double> distanceToTile,
                                     Map<Vector2i, Vector2i> parentTree,
                                     Set<Vector2i> semiVisited,
                                     Vector2i currentTile,
                                     Vector2i tempTile,
                                     double distance) {
        if (navigableTiles.contains(tempTile)) {
            double currentDistance = distanceToTile.get(currentTile) + distance;
            if (distanceToTile.get(tempTile) > currentDistance) {
                semiVisited.remove(tempTile);
                distanceToTile.put(tempTile, currentDistance);
                while (semiVisited.contains(tempTile)) {    //finding the next closest float
                    long cast = Double.doubleToRawLongBits(currentDistance);
                    currentDistance = Double.longBitsToDouble(++cast);
                    distanceToTile.put(tempTile, currentDistance);
                }
                semiVisited.add(tempTile);
                parentTree.put(tempTile, currentTile);
            }
        }
    }
}
