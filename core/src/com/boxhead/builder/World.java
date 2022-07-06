package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Arrays;

public class World {

    public final static int TILE_SIZE = 16;
    public final static int FULL_DAY = 1440;

    private int time;
    private int temperature;
    private Vector2i worldSize;

    private Tiles.Type[] tiles;
    private final ArrayList<Building> buildings = new ArrayList<>();
    private final ArrayList<NPC> npcs = new ArrayList<>();

    private Timer.Task tick = new Timer.Task() {
        @Override
        public void run() {
            addTime(1);
        }
    };

    public final int[] resourceStorage = new int[Resources.values().length];

    public World(Vector2i worldSize) {
        this.worldSize = worldSize;
        tiles = new Tiles.Type[worldSize.x * worldSize.y];
        NPC.Pathfinding.reset(worldSize.x, worldSize.y);

        Timer.schedule(tick, 0, 0.1f);
    }

    public void generateMap() {
        Arrays.fill(tiles, Tiles.Type.DIRT);
    }

    public void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tiles[i].texture, i % worldSize.x * TILE_SIZE, i / worldSize.x * TILE_SIZE);
        }
    }

    public boolean addBuilding(Building building) {
        return buildings.add(building);
    }

    public boolean addNPC(NPC npc) {
        return npcs.add(npc);
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void addTime(int shift) {
        time = (time + shift) % FULL_DAY;
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public ArrayList<NPC> getNpcs() {
        return npcs;
    }

    public int getWidth() {
        return worldSize.x * TILE_SIZE;
    }

    public int getHeight() {
        return worldSize.y * TILE_SIZE;
    }

    public int getTime() {
        return time;
    }

    public void debug() {
        Arrays.fill(tiles, Tiles.Type.DEFAULT);
    }
}
