package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Arrays;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int FULL_DAY = 1440;

    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static Tiles.Type[] tiles;
    private static final ArrayList<Building> buildings = new ArrayList<>();
    private static final ArrayList<NPC> npcs = new ArrayList<>();

    /*private Timer.Task tick = new Timer.Task() {
        @Override
        public void run() {
            addTime(1);
        }
    };*/

    public static final int[] resourceStorage = new int[Resources.values().length];

    /*public World(Vector2i worldSize) {
        this.worldSize = worldSize;
        tiles = new Tiles.Type[worldSize.x * worldSize.y];
        NPC.Pathfinding.reset(worldSize.x, worldSize.y);

        Timer.schedule(tick, 0, 0.1f);
    }*/

    public static void initWorld(Vector2i worldSize) {
        worldSize = worldSize;
        tiles = new Tiles.Type[worldSize.x * worldSize.y];
        NPC.Pathfinding.reset(worldSize.x, worldSize.y);
    }

    public static void generateMap() {
        Arrays.fill(tiles, Tiles.Type.DIRT);
    }

    public static void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tiles[i].texture, i % worldSize.x * TILE_SIZE, i / worldSize.x * TILE_SIZE);
        }
    }

    public static boolean addBuilding(Building building) {
        return buildings.add(building);
    }

    public static boolean addNPC(NPC npc) {
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

    public static int getTime() {
        return time;
    }

    public static void debug() {
        Arrays.fill(tiles, Tiles.Type.DEFAULT);
    }
}
