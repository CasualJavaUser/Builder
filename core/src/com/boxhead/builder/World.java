package com.boxhead.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int HOUR = 3600;
    public static final int FULL_DAY = 86400;
    public static final int YEAR = FULL_DAY;

    /**
     * The size (in tiles) of the biggest GameObject texture.
     */
    private static final int RENDER_BUFFER = 9;

    private static int day;
    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static int seed;
    private static Random random;
    private static final int noiseSize = 100;

    private static Tile[] tiles;
    private static Textures.Tile[] tileTextures;
    private static List<Building> buildings;
    private static List<Villager> villagers;
    private static List<Animal> animals;
    private static Set<FieldWork> removedFieldWorks;
    private static SortedList<GameObject> gameObjects;
    /**
     * Sum of GameObjects in all preceding horizontal lines.
     */
    private static int[] objectsSumUpToLine;
    private static boolean[] navigableTiles;

    /**
     * Tiles on which buildings have been built.
     */
    private static final Set<Vector2i> builtTiles = new HashSet<>();

    /**
     * A map of field works that have been either placed or removed. Value of each entry specifies if a given FieldWork has been placed (true) or removed (false).
     * Used for saving and retrieving the world from file.
     */
    private static final Map<FieldWork, Boolean> changedFieldWorks = new HashMap<>();
    private static final Map<Vector2i, Tile> changedTiles = new HashMap<>();

    public static void generate(int seed, Vector2i worldSize) {
        World.seed = seed;
        World.worldSize = worldSize;
        navigableTiles = new boolean[worldSize.x * worldSize.y];
        tiles = new Tile[worldSize.x * worldSize.y];
        tileTextures = new Textures.Tile[tiles.length];
        buildings = new ArrayList<>();
        villagers = new ArrayList<>();
        animals = new ArrayList<>();
        removedFieldWorks = new HashSet<>();
        gameObjects = new SortedList<>(GameObject.gridPositionComparator);
        objectsSumUpToLine = new int[worldSize.y + 1];

        random = new Random(World.seed);

        resetNavigability();

        LoadingScreen.setMessage("Generating Tiles...");
        generateTiles();
        LoadingScreen.setMessage("Generating Objects...");
        generateObjects();
    }

    public static void temp() {
        initVillagers(15);
        spawnVillager(new Villager(new Vector2i((int) (worldSize.x * 0.10), (int) (worldSize.y * 0.50) - 7)));
        Vector2i buildingPosition = new Vector2i((int) (worldSize.x * 0.45f)-2, (int) (worldSize.y * 0.45));
        BoxCollider collider = ProductionBuilding.Type.BUILDERS_HUT.relativeCollider.cloneAndTranslate(buildingPosition);
        placeBuilding(ProductionBuilding.Type.BUILDERS_HUT, buildingPosition);
        makeUnnavigable(collider);
        makeBuilt(collider);

        collider = Building.Type.STORAGE_BARN.relativeCollider;
        buildingPosition = buildingPosition.plus(-collider.getWidth() * 2, 0);
        collider = collider.cloneAndTranslate(buildingPosition);
        placeBuilding(Building.Type.STORAGE_BARN, buildingPosition);
        makeUnnavigable(collider);
        makeBuilt(collider);
        Building.getByCoordinates(buildingPosition).getInventory().put(Resource.WOOD, 100);
        Building.getByCoordinates(buildingPosition).getInventory().put(Resource.GRAIN, 50);
        Building.getByCoordinates(buildingPosition).getInventory().put(Resource.STONE, 50);
        Resource.storedResources[Resource.WOOD.ordinal()] = 100;
        Resource.storedResources[Resource.GRAIN.ordinal()] = 50;
        Resource.storedResources[Resource.STONE.ordinal()] = 50;

        collider = ProductionBuilding.Type.TRANSPORT_OFFICE.relativeCollider;
        buildingPosition = buildingPosition.plus(-collider.getWidth() * 2, 0);
        placeBuilding(ProductionBuilding.Type.TRANSPORT_OFFICE, buildingPosition);
        collider = collider.cloneAndTranslate(buildingPosition);
        makeUnnavigable(collider);
        makeBuilt(collider);
    }

    public static void handleObjectInteractions() {
        if (!InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) return;

        for (Building building : buildings) {
            if (building.isMouseOver()) {
                UI.showInfoWindow(building);
                return;
            }
        }

        for (Villager villager : villagers) {
            if (villager.isMouseOver()) {
                UI.showInfoWindow(villager);
                return;
            }
        }
    }

    public static void generateTiles() {
        Arrays.fill(tiles, Tile.GRASS);
        generateRiver();

        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].equals(Tile.WATER)) {
                /*
                0 1 2
                3   4
                5 6 7
                 */
                boolean[] neighbours = new boolean[8];
                int ni = 0;
                for (int y = 1; y >= -1; y--) {
                    for (int x = -1; x <= 1; x++) {
                        if (y == 0 && x == 0) continue;
                        int ti = i + y * worldSize.x + x;
                        if (ti >= 0 && ti < tiles.length)
                            neighbours[ni] = tiles[ti].equals(Tile.GRASS);
                        else
                            neighbours[ni] = false;
                        ni++;
                    }
                }

                StringBuilder textureName = new StringBuilder();

                if (neighbours[1] && neighbours[6]) {
                    tiles[i] = Tile.GRASS;
                    tileTextures[i] = Tile.GRASS.textures[random.nextInt(tiles[i].textures.length)];
                    continue;
                }

                if (neighbours[1]) textureName.append("T");
                else if (neighbours[6]) textureName.append("B");

                if (neighbours[4] || (neighbours[2] && neighbours[7])) textureName.append("R");
                else if (neighbours[3] || (neighbours[5] && neighbours[0])) textureName.append("L");

                if (textureName.isEmpty()) {
                    if (neighbours[2])
                        textureName.append("TRO");
                    else if (neighbours[7])
                        textureName.append("BRO");
                    else if (neighbours[5])
                        textureName.append("BLO");
                    else if (neighbours[0])
                        textureName.append("TLO");
                }

                if (!textureName.isEmpty()) tileTextures[i] = Textures.Tile.valueOf(textureName.insert(0, "BANK_").toString());
                else tileTextures[i] = Tile.WATER.textures[random.nextInt(tiles[i].textures.length)];
            }
            else
                tileTextures[i] = tiles[i].textures[random.nextInt(tiles[i].textures.length)];
        }
    }

    private static void generateObjects() {
        Vector2i pos = new Vector2i();
        float treeSmallNoiseFreq = 5.9f, treeBigNoiseFreq = 99.2f;
        float rockSmallNoiseFreq = 5.9f, rockBigNoiseFreq = 199.2f;

        for (int y = worldSize.y - 1; y >= 0; y--) {
            for (int x = worldSize.x - 1; x >= 0; x--) {
                pos.set(x, y);
                generateTree(pos.clone(), treeSmallNoiseFreq, treeBigNoiseFreq);
                generateRock(pos.clone(), rockSmallNoiseFreq, rockBigNoiseFreq);
            }
        }
    }

    private static void generateTree(Vector2i pos, float smallFreq, float bigFreq) {
        double dx = (double) pos.x / noiseSize;
        double dy = (double) pos.y / noiseSize;
        double smallNoise = PerlinNoise.noise3D(dx * smallFreq, dy * smallFreq, seed);
        double bigNoise = PerlinNoise.noise3D(dx * bigFreq, dy * bigFreq, seed);

        Harvestables.Type treeType = random.nextBoolean() ? Harvestables.Type.PINE_TREE : Harvestables.Type.OAK_TREE;
        Harvestable tree = Harvestables.create(treeType, pos);
        int width = tree.getTexture().getRegionWidth() / TILE_SIZE;
        Vector2i trunk = new Vector2i(pos.x + width / 2, pos.y);
        if (smallNoise > 0.1f && bigNoise > 0.21f && isNavigable(trunk) && isBuildable(trunk)) {
            makeUnnavigable(tree.getCollider());
            tree.nextPhase();
            tree.nextPhase();
            Harvestable.timeTriggers.clear();
            addGameObject(tree);
        }
    }

    private static void generateRock(Vector2i pos, float smallFreq, float bigFreq) {
        double dx = (double) pos.x / noiseSize;
        double dy = (double) pos.y / noiseSize;
        double smallNoise = PerlinNoise.noise3D(dx * smallFreq, dy * smallFreq, seed);
        double bigNoise = PerlinNoise.noise3D(dx * bigFreq, dy * bigFreq, seed);

        int typeId = random.nextInt(3) + 1;
        Harvestable rock = Harvestables.create(Harvestables.Type.valueOf("ROCK" + typeId), pos);
        if (smallNoise > -0.05f && bigNoise > 0.35f && isNavigable(pos) && isBuildable(pos)) {
            makeUnnavigable(rock.getCollider());
            rock.nextPhase();
            addGameObject(rock);
        }
    }

    private static void generateRiver() {
        double noiseFrequency = random.nextDouble() * 2 + 3;    //number of curves (3 <= x < 5)
        double curveMultiplier = random.nextDouble() * 4 + 16;  //curve size (16 <= x < 20)
        int width = 2;                                          //river width
        double bias = random.nextDouble() - 0.5d;               //the general direction in which the river is going (positive - right, negative - left) (-0.5 <= x < 0.5)

        int[] steps = new int[worldSize.y];
        double startX = worldSize.x/2f - bias * worldSize.y/2f;
        for (int i = 0; i < steps.length; i++) {
            double di = (double) i / noiseSize;
            double noise = PerlinNoise.noise2D(di * noiseFrequency, seed) * curveMultiplier;
            steps[i] = (int) startX + (int) noise;
            startX += bias;
        }
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                if (x >= steps[y] - width && x <= steps[y] + width) {
                    tiles[y * worldSize.x + x] = Tile.WATER;
                }
            }
        }
    }

    public static void resetNavigability() {
        Arrays.fill(navigableTiles, true);
    }

    public static void makeUnnavigable(Vector2i gridPosition) {
        writeArray(navigableTiles, gridPosition, false);
    }

    public static void makeUnnavigable(BoxCollider area) {
        for (Vector2i tile : area) {
            makeUnnavigable(tile);
        }
    }

    public static void makeNavigable(Vector2i gridPosition) {
        writeArray(navigableTiles, gridPosition, true);
    }

    public static void makeNavigable(BoxCollider area) {
        for (Vector2i tile : area) {
            makeNavigable(tile);
        }
    }

    public static void makeBuilt(BoxCollider area) {
        for (Vector2i tile : area) {
            builtTiles.add(tile);
        }
    }

    public static void makeUnbuilt(BoxCollider area) {
        for (Vector2i tile : area) {
            builtTiles.remove(tile);
        }
    }

    public static Building placeBuilding(Building.Type type, Vector2i gridPosition) {
        Building building = Buildings.create(type, gridPosition);
        placeBuilding(building);
        return building;
    }

    private static void placeBuilding(Building building) {
        Building.Type type = building.getType();
        buildings.add(building);
        addGameObject(building);
        makeUnnavigable(building.getCollider());

        if (type == ProductionBuilding.Type.TRANSPORT_OFFICE) {
            Logistics.getTransportOffices().add((ProductionBuilding) building);
        } else if (type == Building.Type.STORAGE_BARN) {
            Logistics.getStorages().add(building);
        }
        for (Building b : buildings) {
            if (b instanceof ProductionBuilding pb && pb.isBuildingInRange(building)) {
                pb.getBuildingsInRange().add(building);
                pb.updateEfficiency();
            }
        }
    }

    public static void placeFarm(FarmBuilding.Type type, Vector2i gridPosition, BoxCollider fieldCollider) {
        FarmBuilding<?> building = (FarmBuilding<?>) placeBuilding(type, gridPosition);
        building.setFieldCollider(fieldCollider);
        makeBuilt(fieldCollider);

        if (building instanceof RanchBuilding ranch) {
            ranch.spawnAnimals();
        }
    }

    public static void removeBuilding(Building building) {
        if (building instanceof FarmBuilding<?> farm) makeUnbuilt(farm.getFieldCollider());
        buildings.remove(building);
        removeGameObject(building);
        for (Building b : buildings) {
            if (b instanceof ProductionBuilding pb && pb.isBuildingInRange(building)) {
                pb.getBuildingsInRange().remove(building);
                pb.updateEfficiency();
            }
        }
    }

    public static void placeFieldWork(FieldWork fieldWork) {
        makeUnnavigable(fieldWork.getCollider());

        if (fieldWork instanceof BuildSite buildSite) {
            buildings.add(buildSite);
        } else if (fieldWork instanceof Harvestable harvestable) {
            harvestable.nextPhase();
        }
        changedFieldWorks.put(fieldWork, true);
        addGameObject((GameObject) fieldWork);
    }

    public static void removeFieldWorks() {
        for (FieldWork fieldWork : removedFieldWorks) {
            removeGameObject((GameObject) fieldWork);

            if (fieldWork instanceof BuildSite) {
                buildings.remove(fieldWork);
                if (fieldWork instanceof DemolitionSite)
                    makeUnbuilt(fieldWork.getCollider());
            } else if (fieldWork instanceof FarmAnimal)
                animals.remove(fieldWork);
        }
        removedFieldWorks.clear();
    }

    /**
     * Called to indicate that the given fieldwork is to be removed in this tick.
     */
    public static void removeFieldWorks(FieldWork fieldWork) {
        makeNavigable(fieldWork.getCollider());
        removedFieldWorks.add(fieldWork);
        if (changedFieldWorks.containsKey(fieldWork))
            changedFieldWorks.remove(fieldWork);
        else
            changedFieldWorks.put(fieldWork, false);
    }

    public static void removeFieldWorks(BoxCollider area) {
        List<Harvestable> list = findHarvestables(area);
        for (Harvestable harvestable : list) {
            removeFieldWorks(harvestable);
        }
    }

    public static void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        for (int y = gameObject.getGridPosition().y; y >= 0; y--) {
            objectsSumUpToLine[y]++;
        }
    }

    private static void removeGameObject(GameObject gameObject) {
        gameObjects.remove(gameObject);
        for (int y = gameObject.getGridPosition().y; y >= 0; y--) {
            objectsSumUpToLine[y]--;
        }
    }

    public static Harvestable findHarvestables(Vector2i gridPosition) { //todo binary-search-ise
        int i = objectsSumUpToLine[gridPosition.y] - 1;
        GameObject gameObject = gameObjects.get(i);
        Vector2i objectPosition = gameObject.getGridPosition();

        while (objectPosition.y == gridPosition.y && objectPosition.x <= gridPosition.x) {
            if (gameObject instanceof Harvestable harvestable && harvestable.getCollider().overlaps(gridPosition)) {
                return harvestable;
            }
            i--;
            gameObject = gameObjects.get(i);
            objectPosition = gameObject.getGridPosition();
        }
        return null;
    }

    public static List<Harvestable> findHarvestables(BoxCollider area) {
        List<Harvestable> list = new ArrayList<>();
        int rightBoundary = area.getGridPosition().x + area.getWidth() - 1;

        for (int y = area.getGridPosition().y + area.getHeight() - 1; y >= area.getGridPosition().y; y--) {
            int i = objectsSumUpToLine[y] - 1;
            Vector2i objectPosition = gameObjects.get(i).getGridPosition();
            while (objectPosition.y == y && objectPosition.x <= rightBoundary) {
                if (gameObjects.get(i) instanceof Harvestable harvestable && area.overlaps(harvestable.getCollider())) {
                    list.add(harvestable);
                }
                i--;
                objectPosition = gameObjects.get(i).getGridPosition();
            }
        }
        return list;
    }

    public static void spawnVillager(Villager villager) {
        villagers.add(villager);
    }

    public static void spawnAnimal(Animal animal) {
        animals.add(animal);
    }

    public static void drawMap(SpriteBatch batch) {
        Viewport viewport = GameScreen.viewport;

        Vector2i upperLeftCorner = new Vector2i(viewport.unproject(new Vector2()));
        Vector2i lowerRightCorner = new Vector2i(viewport.unproject(new Vector2((float) viewport.getScreenWidth(), (float) viewport.getScreenHeight())));

        Vector2i gridULC = upperLeftCorner.divide(TILE_SIZE);
        Vector2i gridLRC = lowerRightCorner.divide(TILE_SIZE);
        if (gridULC.x < 0) gridULC.x = 0;
        if (gridULC.y >= worldSize.y) gridULC.y = worldSize.y - 1;
        if (gridLRC.x >= worldSize.x) gridLRC.x = worldSize.x - 1;
        if (gridLRC.y < 0) gridLRC.y = 0;

        for (int x = gridULC.x; x <= gridLRC.x; x++) {
            for (int y = gridLRC.y; y <= gridULC.y; y++) {
                batch.draw(Textures.get(tileTextures[y * worldSize.x + x]), x * TILE_SIZE, y * TILE_SIZE);
            }
        }
    }

    public static void drawObjects(SpriteBatch batch) {
        Viewport viewport = GameScreen.viewport;

        Vector2i upperLeftCorner = new Vector2i(viewport.unproject(new Vector2()));
        Vector2i lowerRightCorner = new Vector2i(viewport.unproject(new Vector2((float) viewport.getScreenWidth(), (float) viewport.getScreenHeight())));

        Vector2i gridULC = upperLeftCorner.divide(TILE_SIZE);
        Vector2i gridLRC = lowerRightCorner.divide(TILE_SIZE);

        for (Villager villager : villagers) {
            Vector2 spritePosition = villager.getSpritePosition();
            if (spritePosition.x >= gridULC.x && spritePosition.x <= gridLRC.x &&
                    spritePosition.y >= gridLRC.y && spritePosition.y <= gridULC.y)
                villager.draw(batch);
        }

        for (Animal animal : animals) {
            Vector2 spritePosition = animal.getSpritePosition();
            if (spritePosition.x >= gridULC.x && spritePosition.x <= gridLRC.x &&
                    spritePosition.y >= gridLRC.y && spritePosition.y <= gridULC.y)
                animal.draw(batch);
        }

        Range<Integer> YRange = Range.between(0, worldSize.y - 1);
        for (int y = gridULC.y + RENDER_BUFFER; y >= gridLRC.y - RENDER_BUFFER; y--) {
            if (YRange.contains(y)) {
                for (int i = objectsSumUpToLine[y]; i < objectsSumUpToLine[y - 1]; i++) {
                    GameObject gameObject = gameObjects.get(i);
                    if (gameObject.getGridPosition().x >= gridULC.x - RENDER_BUFFER && gameObject.getGridPosition().x <= gridLRC.x)
                        gameObject.draw(batch);
                }
            }
        }
    }

    public static void showBuildableTiles(SpriteBatch batch) {
        BoxCollider entireWorld = new BoxCollider(Vector2i.zero(), worldSize);
        batch.setColor(UI.SEMI_TRANSPARENT_RED);

        for (Vector2i tile : entireWorld) {
            if (!isBuildable(tile)) Tiles.drawTile(batch, Textures.Tile.DEFAULT, tile);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }

    public static void showNavigableTiles(SpriteBatch batch) {
        BoxCollider entireWorld = new BoxCollider(Vector2i.zero(), worldSize);
        batch.setColor(UI.SEMI_TRANSPARENT_RED);

        for (Vector2i tile : entireWorld) {
            if (!isNavigable(tile)) Tiles.drawTile(batch, Textures.Tile.DEFAULT, tile);
        }
        batch.setColor(UI.DEFAULT_COLOR);
    }

    public static void pathfindingTest(SpriteBatch batch) {
        Vector2i mousePos = GameScreen.getMouseGridPosition();
        if (isNavigable(mousePos)) {
            Vector2i[] path = Pathfinding.findPath(Vector2i.zero(), mousePos);

            for (Vector2i tile : path) {
                Tiles.drawTile(batch, Textures.Tile.DEFAULT, tile);
            }
        }
    }

    public static boolean isBuildable(Vector2i position) {
        if (isOutOfBounds(position)) return false;

        return getTile(position) != Tile.WATER && getTile(position) != Tile.BRIDGE && !builtTiles.contains(position);
    }

    public static boolean isBuildable(int x, int y) {
        return isBuildable(new Vector2i(x, y));
    }

    public static boolean isBuildable(BoxCollider area) {
        for (Vector2i tile : area) {
            if (!isBuildable(tile)) return false;
        }
        return true;
    }

    public static boolean isNavigable(Vector2i gridPosition) {
        return dereferenceArray(navigableTiles, gridPosition);
    }

    public static boolean isNavigable(BoxCollider area) {
        for (Vector2i tile : area) {
            if (!isNavigable(tile)) return false;
        }
        return true;
    }

    public static boolean isOutOfBounds(Vector2i gridPosition) {
        return gridPosition.x < 0 || gridPosition.x >= worldSize.x || gridPosition.y < 0 || gridPosition.y >= worldSize.y;
    }

    public static boolean dereferenceArray(boolean[] array, Vector2i gridPosition) {
        if (isOutOfBounds(gridPosition)) return false;

        int index = (gridPosition.y * World.worldSize.x) + gridPosition.x;
        return array[index];
    }

    public static void writeArray(boolean[] array, Vector2i gridPosition, boolean value) {
        if (isOutOfBounds(gridPosition)) return;

        int index = gridPosition.y * World.worldSize.x + gridPosition.x;
        array[index] = value;
    }

    public static int getSeed() {
        return seed;
    }

    public static void setTime(int time) {
        World.time = time;
    }

    public static void advanceTime(int targetTime) {
        if (targetTime < time) {
            day++;
        }
        time = targetTime;
        Logic.alignShifts();
    }

    public static void setDay(int day) {
        World.day = day;
    }

    public static int getTime() {
        return time;
    }

    public static int getDay() {
        return day;
    }

    public static void incrementTime() {
        time++;
        if (time >= FULL_DAY) {
            time = 0;
            day++;
        }
    }

    public static long getDate() {
        return ((long) day << 32) | time;
    }

    public static long calculateDate(int ticksFromNow) {
        int fullDays = ticksFromNow / FULL_DAY;
        int resultTime = time + (ticksFromNow % FULL_DAY);
        if (resultTime >= FULL_DAY) {
            resultTime -= FULL_DAY;
            fullDays++;
        }
        return ((long) day + fullDays) << 32 | resultTime;
    }

    public static Tile getTile(Vector2i gridPosition) {
        return tiles[worldSize.x * gridPosition.y + gridPosition.x];
    }

    public static Tile getTile(int x, int y) {
        return tiles[worldSize.x * y + x];
    }

    public static Textures.Tile getTileTexture(Vector2i gridPosition) {
        return tileTextures[worldSize.x * gridPosition.y + gridPosition.x];
    }

    public static void setTile(Vector2i gridPosition, Tile tile) {
        tiles[worldSize.x * gridPosition.y + gridPosition.x] = tile;
        tileTextures[worldSize.x * gridPosition.y + gridPosition.x] = tile.textures[random.nextInt(tile.textures.length)];
        changedTiles.put(gridPosition.clone(), tile);
    }

    public static void setTile(int x, int y, Tile tile) {
        tiles[worldSize.x * y + x] = tile;
        tileTextures[worldSize.x * y + x] = tile.textures[random.nextInt(tile.textures.length)];
        changedTiles.put(new Vector2i(x, y), tile);
    }

    public static void setTile(Vector2i gridPosition, Tile tile, Textures.Tile texture) {
        int x = gridPosition.x;
        int y = gridPosition.y;
        tiles[worldSize.x * y + x] = tile;
        tileTextures[worldSize.x * y + x] = texture;
        changedTiles.put(gridPosition, tile);
    }

    private static void initVillagers(int num) {
        for (int i = 0; i < num; i++) {
            Villager villager = new Villager(new Vector2i(worldSize.x / 2, worldSize.y / 2));
            villager.educate(1f);
            villager.setAge(Villager.WORKING_AGE * YEAR);
            spawnVillager(villager);
        }
    }

    public static SortedList<GameObject> getGameObjects() {
        return gameObjects;
    }

    public static List<Building> getBuildings() {
        return buildings;
    }

    public static List<Villager> getVillagers() {
        return villagers;
    }

    public static List<Animal> getAnimals() {
        return animals;
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

    public static boolean[] getNavigableTiles() {
        return navigableTiles;
    }

    public static float getAverageHappiness() {
        float sum = 0;
        for (Villager villager : villagers) {
            sum += villager.getHappiness();
        }
        return sum / (float) villagers.size();
    }

    public static float getAverage(Stat stat) {
        float sum = 0;
        for (Villager villager : villagers) {
            sum += villager.getStats()[stat.ordinal()];
        }
        return sum / villagers.size();
    }

    public static Color getAmbientColor(double time) {
        final double redLowest = -0.4d;
        final double blueLowest = -0.25d;

        if (time > FULL_DAY / 2d) {
            time -= FULL_DAY;
        }
        time /= HOUR;
        double r = Math.exp(-Math.pow((time / 2d), 2d)) * redLowest + 1;
        double b = Math.min(
                Math.exp(-Math.pow((time / 1.5d - 2d), 2d)) * blueLowest + 1,
                Math.exp(-Math.pow((time / 1.5d + 2d), 2d)) * blueLowest + 1
        );
        double g = Math.exp(-Math.pow((time / 2.8d), 2d)) * redLowest + 1;
        return new Color((float) r, (float) g, (float) b, 1f);
    }

    public static void saveWorld(ObjectOutputStream out) throws IOException {
        out.writeInt(World.getSeed());
        out.writeInt(World.getGridWidth());
        out.writeInt(World.getGridHeight());
        out.writeInt(World.getTime());
        out.writeInt(World.getDay());

        BuilderGame.saveCollection(buildings, out);
        BuilderGame.saveCollection(villagers, out);
        BuilderGame.saveCollection(animals, out);
        BuilderGame.saveMap(changedFieldWorks, out);
        out.writeInt(changedTiles.size());
        for (Vector2i pos : changedTiles.keySet()) {
            out.writeObject(pos);
            out.writeUTF(changedTiles.get(pos).name());
        }
    }

    public static void loadWorld(ObjectInputStream in) throws IOException, ClassNotFoundException {
        generate(in.readInt(), new Vector2i(in.readInt(), in.readInt()));
        setTime(in.readInt());
        setDay(in.readInt());

        LoadingScreen.setMessage("Loading objects...");
        BuilderGame.loadCollection(buildings, in);
        BuilderGame.loadCollection(villagers, in);
        BuilderGame.loadCollection(animals, in);
        BuilderGame.loadMap(changedFieldWorks, in);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            changedTiles.put((Vector2i) in.readObject(), Tile.valueOf(in.readUTF()));
        }

        villagers.forEach(World::addGameObject);

        for (Building building : buildings) {
            addGameObject(building);
            makeUnnavigable(building.getCollider());
            if (building.getType() == ProductionBuilding.Type.TRANSPORT_OFFICE) {
                Logistics.getTransportOffices().add((ProductionBuilding) building);
            } else if (building.getType() == Building.Type.STORAGE_BARN) {
                Logistics.getStorages().add(building);
            }
            if (building.getType() instanceof RanchBuilding.Type) {
                Tiles.createFence(((FarmBuilding<?>) building).getFieldCollider());
            }
        }


        for (FieldWork fieldWork : changedFieldWorks.keySet()) {
            if (changedFieldWorks.get(fieldWork)) {
                addGameObject((GameObject) fieldWork);
                makeUnnavigable(fieldWork.getCollider());
            } else {
                removeGameObject((GameObject) fieldWork);
                makeNavigable(fieldWork.getCollider());
            }
        }

        for (Vector2i pos : changedTiles.keySet()) {
            tiles[worldSize.x * pos.y + pos.x] = changedTiles.get(pos);
            tileTextures[worldSize.x * pos.y + pos.x] = changedTiles.get(pos).textures[random.nextInt(changedTiles.get(pos).textures.length)];
        }
    }
}
