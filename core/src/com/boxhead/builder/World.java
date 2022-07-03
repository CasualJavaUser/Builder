package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class World {

    public final static int TILE_SIZE = 16;
    public final static int MAX_TIME = 1440;

    private int time;
    private int temperature;
    private Vector2i worldSize;

    private Tiles.Type[] tiles;
    private ArrayList<Building> buildings;

    public World(Vector2i worldSize) {
        this.worldSize = worldSize;
        tiles = new Tiles.Type[worldSize.x * worldSize.y];
        buildings = new ArrayList<>();
    }

    public void generateMap() {
        Arrays.fill(tiles, Tiles.Type.DIRT);
    }

    public void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tiles[i].texture, i%worldSize.x * TILE_SIZE, i/worldSize.x * TILE_SIZE);
        }
    }

    public boolean add(Building building) {
        return buildings.add(building);
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public int getWidth() {
        return worldSize.x * 16;
    }

    public int getHeight() {
        return worldSize.y * 16;
    }

    public void debug() {
        Arrays.fill(tiles, Tiles.Type.DEFAULT);
    }
}
