package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int FULL_DAY = 86400;

    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static int[] storage = new int[Resources.values().length];
    private static int[] maxStorage = new int[Resources.values().length];

    private static Tiles.Types[] tiles;
    private static final ArrayList<Building> buildings = new ArrayList<>();
    private static final ArrayList<NPC> npcs = new ArrayList<>();
    private static final ArrayList<Harvestable> harvestables = new ArrayList<>();

    private static final HashSet<Vector2i> navigableTiles = new HashSet<>();


    public static void initWorld(Vector2i worldSize) {
        World.worldSize = worldSize;
        tiles = new Tiles.Types[worldSize.x * worldSize.y];
        resetNavigable(worldSize);
        //initNPCs();
    }

    public static void generateMap() {
        Arrays.fill(tiles, Tiles.Types.DIRT);
    }

    public static void resetNavigable(Vector2i gridDimensions) {
        navigableTiles.clear();
        for (int i = 0; i < gridDimensions.x; i++) {
            for (int j = 0; j < gridDimensions.y; j++) {
                navigableTiles.add(new Vector2i(i, j));
            }
        }
    }

    public static void makeUnnavigable(Vector2i gridPosition) {
        navigableTiles.remove(gridPosition);
    }

    public static void makeUnnavigable(BoxCollider area) {
        Vector2i tile = new Vector2i();
        for (int y = 0; y < area.getHeight() / World.TILE_SIZE; y++) {
            for (int x = 0; x < area.getWidth() / World.TILE_SIZE; x++) {
                tile.set(x + area.getGridPosition().x, y + area.getGridPosition().y);
                navigableTiles.remove(tile);
            }
        }
    }

    public static void makeNavigable(Vector2i gridPosition) {
        navigableTiles.add(gridPosition);
    }

    public static boolean startConstruction(Buildings.Types type, Vector2i gridPosition) {
        if (navigableTiles.contains(gridPosition)) {
            ConstructionSite site = new ConstructionSite("construction site (" + Buildings.get(type).getName() + ')', type, 100);
            site.setPosition(gridPosition);
            buildings.add(site);
            makeUnnavigable(site.getCollider());
            return true;
        }
        return false;
    }

    public static void placeBuilding(Buildings.Types type, Vector2i gridPosition) {
        Building building = Buildings.get(type);
        building.setPosition(gridPosition);
        buildings.add(building);
        if (building instanceof StorageBuilding) updateMaxStorage();
    }

    public static void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tiles[i].texture, i % worldSize.x * TILE_SIZE, (float) (i / worldSize.x) * TILE_SIZE);
        }
    }

    public static int getMaxStorage(Resources resource) {
        return maxStorage[resource.ordinal()];
    }

    public static int getStored(Resources resource) {
        updateStorage();
        return storage[resource.ordinal()];
    }

    public static void updateMaxStorage() {
        Arrays.fill(maxStorage, 0);
        for (Building storageBuilding : buildings) {
            if (storageBuilding instanceof StorageBuilding) {
                for (int i = 0; i < maxStorage.length; i++) {
                    maxStorage[i] += ((StorageBuilding) storageBuilding).getMaxStorage(i);
                }
            }
        }
    }

    public static void updateStorage() {
        Arrays.fill(storage, 0);
        for (Building storageBuilding : buildings) {
            if (storageBuilding instanceof StorageBuilding) {
                for (int i = 0; i < storage.length; i++) {
                    storage[i] += ((StorageBuilding) storageBuilding).getStored(i);
                }
            }
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

    public static ArrayList<Harvestable> getHarvestables() {
        return harvestables;
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
        Arrays.fill(tiles, Tiles.Types.DEFAULT);
    }

    private static void initNPCs() {
        spawnNPC(new NPC(Textures.getNPC("funguy"), new Vector2i(worldSize.x / 2, worldSize.y / 2)));
    }
}
