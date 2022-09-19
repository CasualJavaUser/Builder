package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.PerlinNoise;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.util.*;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int FULL_DAY = 86400;

    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static final int SEED = 45;
    private static Random random;
    private static final int treeNoiseFrequency = 6;

    private static final int[] storage = new int[Resource.values().length];

    private static Tile.Type[] tiles;
    private static TextureRegion[] tileTextures;
    private static  SortedList<Building> buildings;
    private static SortedList<NPC> npcs;
    private static SortedList<Harvestable> harvestables;

    private static final Comparator<? extends GameObject> comparator = Comparator.comparingInt(o -> (o.getGridPosition().x + o.getGridPosition().y * worldSize.x));

    private static final HashSet<Vector2i> navigableTiles = new HashSet<>();


    public static void init(Vector2i worldSize) {
        World.worldSize = worldSize;
        tiles = new Tile.Type[worldSize.x * worldSize.y];
        tileTextures = new TextureRegion[tiles.length];
        buildings = new SortedList<>((Comparator<Building>) comparator);
        npcs = new SortedList<>((Comparator<NPC>) comparator);
        harvestables = new SortedList<>((Comparator<Harvestable>) comparator);

        random = new Random(SEED);

        generateTiles();
        generateTrees();

        resetNavigability(worldSize);
        placeBuilding(Buildings.Type.CONSTRUCTION_OFFICE, new Vector2i(45, 45));
        makeUnnavigable(new BoxCollider(new Vector2i(45, 45), 2 * TILE_SIZE, 2 * TILE_SIZE));
        harvestables.add(new Harvestable(Textures.get(Textures.Environment.SMALL_TREE), new Vector2i(45, 35), Harvestable.Type.TREE, 10));
    }

    public static void handleNpcsAndBuildingsOnClick() {
        for (NPC npc : npcs) {
            if (npc.isClicked()) {
                npc.onClick();
                return;
            }
        }
        for (Building building : buildings) {
            if (building.isClicked()) {
                building.onClick();
                return;
            }
        }
    }

    private static void generateTiles() {
        Arrays.fill(tiles, Tile.Type.GRASS);
        for (int i = 0; i < tiles.length; i++) {
            tileTextures[i] = tiles[i].textures[random.nextInt(tiles[i].textures.length)];
        }
    }

    private static void generateTrees() {
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                double dx = (double) x / worldSize.x;
                double dy = (double) y / worldSize.y;
                double smallNoise = PerlinNoise.noise(dx * treeNoiseFrequency, dy * treeNoiseFrequency, SEED);
                double bigNoise = PerlinNoise.noise(dx * treeNoiseFrequency * 100, dy * treeNoiseFrequency * 100, SEED);

                if(smallNoise > 0.1f && bigNoise > .2f) {
                    harvestables.add(new Harvestable(Textures.get(Textures.Environment.SMALL_TREE), new Vector2i(x, y), Harvestable.Type.TREE, 10));
                }
            }
        }
    }

    public static void resetNavigability(Vector2i gridDimensions) {
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

    public static void makeNavigable(BoxCollider area) {
        Vector2i tile = new Vector2i();
        for (int y = 0; y < area.getHeight() / World.TILE_SIZE; y++) {
            for (int x = 0; x < area.getWidth() / World.TILE_SIZE; x++) {
                tile.set(x + area.getGridPosition().x, y + area.getGridPosition().y);
                navigableTiles.add(tile);
            }
        }
    }

    public static boolean startConstruction(Buildings.Type type, Vector2i gridPosition) {
        if (navigableTiles.contains(gridPosition)) {
            ConstructionSite site = new ConstructionSite("construction site (" + Buildings.get(type).getName() + ')', type, 100);
            site.setGridPosition(gridPosition);
            buildings.add(site);
            makeUnnavigable(site.getCollider());
            return true;
        }
        return false;
    }

    public static void placeBuilding(Buildings.Type type, Vector2i gridPosition) {
        Building building = Buildings.get(type);
        building.setGridPosition(gridPosition);
        buildings.add(building);
    }

    public static void drawMap(SpriteBatch batch) {
        for (int i = 0; i < tiles.length; i++) {
            batch.draw(tileTextures[i], i % worldSize.x * TILE_SIZE, (float) (i / worldSize.x) * TILE_SIZE);
        }
    }

    public static void drawObjects(SpriteBatch batch) {
        for (NPC npc : npcs) {
            npc.draw(batch);
        }

        for (Harvestable harvestable : harvestables) {
            harvestable.draw(batch);
        }

        for (Building building : buildings) {
            building.draw(batch);
        }
    }

    public static int getStored(Resource resource) {
        updateStorage();
        return storage[resource.ordinal()];
    }

    public static void updateStorage() {
        Arrays.fill(storage, 0);
        for (Building storageBuilding : buildings) {
            if (storageBuilding instanceof StorageBuilding) {
                for (int i = 0; i < storage.length; i++) {
                    storage[i] += ((StorageBuilding) storageBuilding).getStored(Resource.values()[i]);
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

    public static SortedList<Building> getBuildings() {
        return buildings;
    }

    public static SortedList<NPC> getNpcs() {
        return npcs;
    }

    public static SortedList<Harvestable> getHarvestables() {
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

    private static void debug() {
        Arrays.fill(tiles, Tile.Type.DEFAULT);
    }

    private static void initNPCs() {
        spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), new Vector2i(worldSize.x / 2, worldSize.y / 2)));
    }
}
