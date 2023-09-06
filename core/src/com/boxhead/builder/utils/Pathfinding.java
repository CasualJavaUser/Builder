package com.boxhead.builder.utils;

import com.boxhead.builder.Tile;
import com.boxhead.builder.World;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class Pathfinding {
    public static final float SQRT_2 = 1.41421353816986083984375f;

    public static Vector2i[] findPath(Vector2i start, Vector2i destination) {
        return A_star(start, Predicate.isEqual(destination), World.getNavigableTiles().clone(), destination::distanceApprox);
    }

    public static Vector2i[] findPath(Vector2i start, BoxCollider area) {
        return A_star(start, area.extended()::overlaps, World.getNavigableTiles().clone(), area::distance);
    }

    private static Vector2i[] A_star(Vector2i start, Predicate<Vector2i> destination, boolean[] navigableTiles, ToDoubleFunction<Vector2i> distance) {
        float[] distanceToTile = new float[navigableTiles.length];
        Vector2i[] parentTree = new Vector2i[navigableTiles.length];
        SortedSet<Vector2i> semiVisited = new TreeSet<>(Comparator.comparingDouble(vector -> (distance.applyAsDouble(vector) * Tile.minDistanceModifier) + ((double) dereferenceArray(distanceToTile, vector)))); //tiles to which distances are known

        Arrays.fill(distanceToTile, Float.MAX_VALUE);
        writeArray(distanceToTile, start, 0f);
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

            World.writeArray(navigableTiles, currentTile, false);
            semiVisited.remove(currentTile);

            if (semiVisited.isEmpty()) return new Vector2i[]{start};

            currentTile = semiVisited.first();
            x = currentTile.x;
            y = currentTile.y;
        }

        Vector2i lastTile = currentTile;
        int totalDistance = 1;
        while (!currentTile.equals(start)) {
            currentTile = dereferenceArray(parentTree, currentTile);
            totalDistance++;
        }

        currentTile = lastTile;
        Vector2i[] finalPath = new Vector2i[totalDistance];
        for (int i = totalDistance - 1; i >= 0; i--) {
            finalPath[i] = currentTile;
            currentTile = dereferenceArray(parentTree, currentTile);
        }
        return finalPath;
    }

    private static void calcDistance(boolean[] navigableTiles,
                                     float[] distanceToTile,
                                     Vector2i[] parentTree,
                                     Set<Vector2i> semiVisited,
                                     Vector2i currentTile,
                                     Vector2i tempTile,
                                     float distance) {
        if (World.dereferenceArray(navigableTiles, tempTile)) {
            float currentDistance = dereferenceArray(distanceToTile, currentTile) + distance;
            if (dereferenceArray(distanceToTile, tempTile) > currentDistance) {
                semiVisited.remove(tempTile);
                writeArray(distanceToTile, tempTile, currentDistance);
                while (semiVisited.contains(tempTile)) {    //finding the next closest float
                    int cast = Float.floatToRawIntBits(currentDistance);
                    currentDistance = Float.intBitsToFloat(++cast);
                    writeArray(distanceToTile, tempTile, currentDistance);
                }
                semiVisited.add(tempTile);
                writeArray(parentTree, tempTile, currentTile);
            }
        }
    }

    private static float dereferenceArray(float[] array, Vector2i position) {
        int index = position.y * World.getGridWidth() + position.x;
        return array[index];
    }

    private static void writeArray(float[] array, Vector2i position, float value) {
        int index = position.y * World.getGridWidth() + position.x;
        array[index] = value;
    }

    private static Vector2i dereferenceArray(Vector2i[] array, Vector2i position) {
        int index = position.y * World.getGridWidth() + position.x;
        return array[index];
    }

    private static void writeArray(Vector2i[] array, Vector2i position, Vector2i value) {
        int index = position.y * World.getGridWidth() + position.x;
        array[index] = value;
    }
}
