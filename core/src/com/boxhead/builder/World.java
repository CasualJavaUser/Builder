package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.ui.UI;
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

    private static final int SEED = 60;
    private static Random random;

    private static final int[] storedResources = new int[Resource.values().length];

    private static Tile[] tiles;
    private static TextureRegion[] tileTextures;
    private static List<Building> buildings;
    private static List<NPC> npcs;
    private static Set<FieldWork> fieldWorks;
    private static SortedList<GameObject> gameObjects;

    private static final Comparator<GameObject> comparator = Comparator.comparingInt(o -> ((worldSize.x - o.getGridPosition().x) + o.getGridPosition().y * worldSize.x));

    private static final Set<Vector2i> navigableTiles = new HashSet<>();

    public static void init(Vector2i worldSize) {
        World.worldSize = worldSize;
        tiles = new Tile[worldSize.x * worldSize.y];
        tileTextures = new TextureRegion[tiles.length];
        buildings = new ArrayList<>();
        npcs = new ArrayList<>();
        fieldWorks = new HashSet<>();
        gameObjects = new SortedList<>(comparator);

        random = new Random(SEED);

        resetNavigability(worldSize);

        generateTiles();
        generateObjects();

        //temp
        Vector2i constructionOfficePos = new Vector2i((int) (worldSize.x * 0.45f), (int) (worldSize.y * 0.45));
        placeBuilding(Buildings.Type.CONSTRUCTION_OFFICE, constructionOfficePos);
        makeUnnavigable(new BoxCollider(constructionOfficePos, 2, 2));
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
        Arrays.fill(tiles, Tile.GRASS);
        generateRiver();

        for (int i = 0; i < tiles.length; i++) {
            tileTextures[i] = tiles[i].textures[random.nextInt(tiles[i].textures.length)];
        }
    }

    private static void generateObjects() {
        Vector2i pos = new Vector2i();
        float treeSmallNoiseFreq = 5.9f, treeBigNoiseFreq = 99.2f;
        float stoneSmallNoiseFreq = 5.9f, stoneBigNoiseFreq = 199.2f;

        for (int y = worldSize.y - 1; y >= 0; y--) {
            for (int x = worldSize.x - 1; x >= 0; x--) {
                pos.set(x, y);
                generateTree(pos.clone(), treeSmallNoiseFreq, treeBigNoiseFreq);
                generateStone(pos.clone(), stoneSmallNoiseFreq, stoneBigNoiseFreq);
            }
        }
    }

    private static void generateTree(Vector2i pos, float smallFreq, float bigFreq) {
        Harvestables.Type type = Harvestables.Type.BIG_TREE;  //TODO randomise tree types
        double dx = (double) pos.x / worldSize.x;
        double dy = (double) pos.y / worldSize.y;
        double smallNoise = PerlinNoise.noise3D(dx * smallFreq, dy * smallFreq, SEED);
        double bigNoise = PerlinNoise.noise3D(dx * bigFreq, dy * bigFreq, SEED);
        int textureId = random.nextInt(type.getTextures().length);

        int width = type.getTextures()[textureId].getRegionWidth() / TILE_SIZE;
        int height = type.getTextures()[textureId].getRegionHeight() / TILE_SIZE;
        int trunkX = pos.x + width / 2;
        boolean isLocationValid = pos.x + width <= worldSize.x && pos.y + height <= worldSize.y && tiles[pos.y * worldSize.y + trunkX] != Tile.WATER;
        if(isLocationValid && smallNoise > 0.1f && bigNoise > 0.21f) {
            placeFieldWork(Harvestables.create(type, pos, textureId));
        }
    }

    private static void generateStone(Vector2i pos, float smallFreq, float bigFreq) {
        Harvestables.Type type = Harvestables.Type.STONE;
        double dx = (double) pos.x / worldSize.x;
        double dy = (double) pos.y / worldSize.y;
        double smallNoise = PerlinNoise.noise3D(dx * smallFreq, dy * smallFreq, SEED);
        double bigNoise = PerlinNoise.noise3D(dx * bigFreq, dy * bigFreq, SEED);
        int textureId = random.nextInt(type.getTextures().length);

        if(isBuildable(pos) && smallNoise > -0.05f && bigNoise > 0.35f) {
            placeFieldWork(Harvestables.create(type, pos, textureId));
        }
    }

    private static void generateRiver() {
        double noiseFrequency = random.nextDouble() * 2 + 3;    //amount of curves (3 <= x < 5)
        double curveMultiplier = random.nextDouble() * 4 + 16;  //curve size (16 <= x < 20)
        int width = 2;                                          //river width
        double bias = random.nextDouble() - 0.5f;               //the general direction in which the river is going (positive - right, negative - left) (-0.5 <= x < 0.5)
        float minDistanceFromEdge = 0.3f;

        int[] steps = new int[worldSize.y];
        double startX = random.nextInt((int)(worldSize.x * (1-2*minDistanceFromEdge))) + (int)(worldSize.x * minDistanceFromEdge);
        for (int i = 0; i < steps.length; i++) {
            double di = (double) i / worldSize.y;
            double noise = PerlinNoise.noise2D(di * noiseFrequency, SEED) * curveMultiplier;
            steps[i] = (int)startX + (int)noise;
            startX += bias;
        }
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                if(x >= steps[y] - width && x <= steps[y] + width) {
                    tiles[y * worldSize.y + x] = Tile.WATER;
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
        for (int y = 0; y < area.getHeight(); y++) {
            for (int x = 0; x < area.getWidth(); x++) {
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
        for (int y = 0; y < area.getHeight(); y++) {
            for (int x = 0; x < area.getWidth(); x++) {
                tile.set(x + area.getGridPosition().x, y + area.getGridPosition().y);
                navigableTiles.add(tile.clone());
            }
        }
    }

    public static void placeBuilding(Buildings.Type type, Vector2i gridPosition) {
        Building building = Buildings.create(type, gridPosition);
        buildings.add(building);
        gameObjects.add(building);
    }

    public static void removeBuilding(Building building) {
        makeNavigable(building.getCollider());
        if (building instanceof ConstructionSite) fieldWorks.remove(building);
        buildings.remove(building);
        gameObjects.remove(building);
    }

    public static void placeFieldWork(FieldWork fieldWork) {
        makeUnnavigable(fieldWork.getCollider());
        if (fieldWork instanceof Building) buildings.add((Building) fieldWork);
        fieldWorks.add(fieldWork);
        gameObjects.add((GameObject) fieldWork);
    }

    public static void removeFieldWorks() {
        for (FieldWork fieldWork : fieldWorks) {
            if (fieldWork.isRemoved()) {
                if (fieldWork instanceof Harvestable) makeNavigable(fieldWork.getCollider());
                else if (fieldWork instanceof ConstructionSite) buildings.remove(fieldWork);
                gameObjects.remove((GameObject) fieldWork);
            }
        }
        fieldWorks.removeIf(FieldWork::isRemoved);
    }

    public static void spawnNPC(NPC npc) {
        npcs.add(npc);
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
        for (GameObject o : gameObjects) {
            o.draw(batch);
        }
    }

    public static void showBuildableTiles(SpriteBatch batch) {
        Vector2i pos = new Vector2i();
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                pos.set(x, y);
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                if(!isBuildable(pos)) batch.draw(Textures.get(Textures.Tile.DEFAULT), x * TILE_SIZE, y * TILE_SIZE);
                batch.setColor(UI.DEFAULT_COLOR);
            }
        }
    }

    public static boolean isBuildable(Vector2i position) {
        Tile tile = getTile(position);
        return tile != Tile.WATER && navigableTiles.contains(position);
    }

    public static int getStored(Resource resource) {
        updateStoredResources();
        return storedResources[resource.ordinal()];
    }

    public static void updateStoredResources() {
        Arrays.fill(storedResources, 0);
        for (Building building : buildings) {
            for (int i = 0; i < storedResources.length; i++) {
                storedResources[i] += building.getInventory().getResourceAmount(Resource.values()[i]);
            }
        }
    }

    public static void setTime(int time) {
        World.time = time;
    }

    public static void addTime(int shift) {
        time = (time + shift) % FULL_DAY;
    }

    public static List<Building> getBuildings() {
        return buildings;
    }

    public static List<NPC> getNpcs() {
        return npcs;
    }

    public static Set<FieldWork> getFieldWorks() {
        return fieldWorks;
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

    public static Set<Vector2i> getNavigableTiles() {
        return navigableTiles;
    }

    public static Tile getTile(Vector2i gridPosition) {
        return tiles[worldSize.x * gridPosition.y + gridPosition.x];
    }

    private static void debug() {
        Arrays.fill(tiles, Tile.DEFAULT);
    }

    private static void initNPCs() {
        spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), new Vector2i(worldSize.x / 2, worldSize.y / 2)));
    }
}
