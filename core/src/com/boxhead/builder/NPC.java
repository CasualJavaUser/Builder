package com.boxhead.builder;

import com.sun.tools.javac.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

public class NPC {
    String name;
    int age, health;
    Jobs job;
    ResidentialBuilding home;

    public static class Pathfinding {
        private static final HashSet<Pair<Integer, Integer>> navigableTiles = new HashSet<>();

        public static void reset(int gridWidth, int gridHeight) {
            navigableTiles.clear();
            for (int i = 0; i < gridWidth; i++) {
                for (int j = 0; j < gridHeight; j++) {
                    navigableTiles.add(new Pair<>(i, j));
                }
            }
        }

        public static void makeUnnavigable(int x, int y) {
            navigableTiles.remove(new Pair<>(x, y));
        }

        public static void makeNavigable(int x, int y) {
            navigableTiles.add(new Pair<>(x, y));
        }

        public static Pair<Integer, Integer>[] findPath(Pair<Integer, Integer> start, Pair<Integer, Integer> destination) {     //Dijkstra's algorithm
            if (start.hashCode() == destination.hashCode()) {
                return new Pair[]{start};
            }
            HashSet<Pair<Integer, Integer>> unvisitedTiles = new HashSet<>(navigableTiles);
            HashMap<Pair<Integer, Integer>, Double> distanceToTile = new HashMap<>();
            HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> parentTree = new HashMap<>();
            for (Pair<Integer, Integer> tile : navigableTiles) {
                distanceToTile.put(tile, Double.MAX_VALUE);
            }
            distanceToTile.remove(start);
            distanceToTile.put(start, 0d);

            int x = start.fst, y = start.snd;
            Pair<Integer, Integer> currentTile = new Pair<>(x, y);
            Pair<Integer, Integer> tempTile;

            while (currentTile.hashCode() != destination.hashCode()) {
                tempTile = new Pair<>(x + 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Pair<>(x - 1, y);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Pair<>(x, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Pair<>(x, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, 1);
                tempTile = new Pair<>(x + 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Pair<>(x - 1, y + 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Pair<>(x + 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));
                tempTile = new Pair<>(x - 1, y - 1);
                calcDistance(unvisitedTiles, distanceToTile, parentTree, currentTile, tempTile, Math.sqrt(2));

                unvisitedTiles.remove(currentTile);

                boolean pathExists = false;
                for (Pair<Integer, Integer> tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < Double.MAX_VALUE) {
                        pathExists = true;
                        break;
                    }
                }
                if (!pathExists) {
                    return new Pair[]{start};   //no path
                }

                Pair<Integer, Integer> smallestDistanceTile = null;
                double smallestDistance = Double.MAX_VALUE;
                for (Pair<Integer, Integer> tile : unvisitedTiles) {
                    if (distanceToTile.get(tile) < smallestDistance) {
                        smallestDistanceTile = tile;
                        smallestDistance = distanceToTile.get(tile);
                    }
                }
                currentTile = smallestDistanceTile;
                x = currentTile.fst;
                y = currentTile.snd;
            }

            int totalDistance = 1;
            while (currentTile.hashCode() != start.hashCode()) {
                currentTile = parentTree.get(currentTile);
                totalDistance++;
            }

            currentTile = destination;
            Pair<Integer, Integer>[] finalPath = new Pair[totalDistance];
            for (int i = totalDistance - 1; i >= 0; i--) {
                finalPath[i] = currentTile;
                currentTile = parentTree.get(currentTile);
            }
            return finalPath;
        }

        private static void calcDistance(HashSet<Pair<Integer, Integer>> unvisitedTiles,
                                         HashMap<Pair<Integer, Integer>, Double> distanceToTile,
                                         HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> parentTree,
                                         Pair<Integer, Integer> currentTile,
                                         Pair<Integer, Integer> tempTile,
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