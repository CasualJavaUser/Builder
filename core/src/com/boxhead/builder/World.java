package com.boxhead.builder;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int FULL_DAY = 86400;

    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static Tiles.Type[] tiles;
    private static final ArrayList<Building> buildings = new ArrayList<>();
    private static final ArrayList<NPC> npcs = new ArrayList<>();

    public static final int[] resourceStorage = new int[Resources.values().length];

    private static final HashSet<Vector2i> navigableTiles = new HashSet<>();


    public static void initWorld(Vector2i worldSize) {
        World.worldSize = worldSize;
        tiles = new Tiles.Type[worldSize.x * worldSize.y];
        resetNavigable(worldSize);
        //initNPCs();
    }

    public static void generateMap() {
        Arrays.fill(tiles, Tiles.Type.DIRT);
    }

    public static void resetNavigable(Vector2i gridDimensions) {
        navigableTiles.clear();
        for (int i = 0; i < gridDimensions.x; i++) {
            for (int j = 0; j < gridDimensions.y; j++) {
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

    public static boolean placeBuilding(Buildings.Types type, Vector2i gridPosition) {
        if (navigableTiles.contains(gridPosition)) {
            Building building = Buildings.get(type);
            building.setPosition(gridPosition);
            buildings.add(building);
            navigableTiles.remove(gridPosition);    //todo
            return true;
        }
        return false;
    }

    public static void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tiles[i].texture, i % worldSize.x * TILE_SIZE, (float) (i / worldSize.x) * TILE_SIZE);
        }
    }

    public static boolean spawnNPC(NPC npc) {
        return npcs.add(npc);
    }

    public static void setTime(int time) {
        World.time = time;
    }

    public static void addTime(int shift) {
        time = (time + shift) % FULL_DAY;
    }

    public static ArrayList<Building> getBuildings() {
        return buildings;
    }

    public static ArrayList<NPC> getNpcs() {
        return npcs;
    }

    public static int getWidth() {
        return worldSize.x * TILE_SIZE;
    }

    public static int getHeight() {
        return worldSize.y * TILE_SIZE;
    }

    public static int getGridWidth() {
        return worldSize.x;
    }

    public static int getGridHeight() {
        return worldSize.y;
    }

    public static int getTime() {
        return time;
    }

    public static HashSet<Vector2i> getNavigableTiles() {
        return navigableTiles;
    }

    public static void debug() {
        Arrays.fill(tiles, Tiles.Type.DEFAULT);
    }

    private static void initNPCs() {
        spawnNPC(new NPC(new Texture("funguy.png"), new Vector2i(worldSize.x / 2,worldSize.y / 2)));
    }
}
