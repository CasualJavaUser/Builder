package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.*;
import org.apache.commons.lang3.Range;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class World {

    public static final int TILE_SIZE = 16;
    public static final int FULL_DAY = 86400;
    public static final int HOUR = 3600;

    /**
     * The size (in tiles) of the biggest GameObject texture.
     */
    private static final int RENDER_BUFFER = 4;

    private static int day;
    private static int time;
    private static int temperature;
    private static Vector2i worldSize;

    private static int seed;
    private static Random random;
    private static final int noiseSize = 100;

    private static final int[] storedResources = new int[Resource.values().length];

    private static Tile[] tiles;
    private static Textures.Tile[] tileTextures;
    private static List<Building> buildings;
    private static List<Villager> villagers;
    private static List<Animal> animals;
    private static Set<FieldWork> fieldWorks;
    private static Set<FieldWork> removedFieldWorks;
    private static SortedList<GameObject> gameObjects;
    /**
     * Sum of GameObjects in all preceding horizontal lines.
     */
    private static int[] objectsSumUpToLine;

    private static final Set<Vector2i> navigableTiles = new HashSet<>();

    /**
     * A map of field works that have been either placed or removed. Value of each entry specifies if a given FieldWork has been placed (true) or removed (false).
     */
    private static final Map<FieldWork, Boolean> changedFieldWorks = new HashMap<>();
    private static final Map<Vector2i, Tile> changedTiles = new HashMap<>();

    public static void init(int seed, Vector2i worldSize) {
        World.seed = seed;
        World.worldSize = worldSize;
        tiles = new Tile[worldSize.x * worldSize.y];
        tileTextures = new Textures.Tile[tiles.length];
        buildings = new ArrayList<>();
        villagers = new ArrayList<>();
        animals = new ArrayList<>();
        fieldWorks = new HashSet<>();
        removedFieldWorks = new HashSet<>();
        gameObjects = new SortedList<>(GameObject.gridPositionComparator);
        objectsSumUpToLine = new int[worldSize.y + 1];

        random = new Random(World.seed);

        resetNavigability(worldSize);

        generateTiles();
        generateObjects();
    }

    public static void temp() {
        initVillagers(10);
        spawnVillager(new Villager((int) (Math.random() + 1d), new Vector2i((int) (worldSize.x * 0.10), (int) (worldSize.y * 0.50) - 7)));
        spawnAnimal(new Animal(Animals.Type.COW, new Vector2i(10, 10)));
        Vector2i buildingPosition = new Vector2i((int) (worldSize.x * 0.45f), (int) (worldSize.y * 0.45));
        BoxCollider collider = Buildings.Type.BUILDERS_HUT.relativeCollider.cloneAndTranslate(buildingPosition);
        placeBuilding(Buildings.Type.BUILDERS_HUT, buildingPosition);
        makeUnnavigable(collider);

        collider = Buildings.Type.STORAGE_BARN.relativeCollider;
        buildingPosition = buildingPosition.add(-collider.getWidth() * 2, 0);
        collider = collider.cloneAndTranslate(buildingPosition);
        placeBuilding(Buildings.Type.STORAGE_BARN, buildingPosition);
        makeUnnavigable(collider);
        StorageBuilding.getByCoordinates(buildingPosition).getInventory().put(Resource.WOOD, 100);
        StorageBuilding.getByCoordinates(buildingPosition).getInventory().put(Resource.STONE, 100);
        storedResources[Resource.WOOD.ordinal()] = 100;
        storedResources[Resource.STONE.ordinal()] = 100;

        collider = Buildings.Type.TRANSPORT_OFFICE.relativeCollider;
        buildingPosition = buildingPosition.add(-collider.getWidth() * 2, 0);
        placeBuilding(Buildings.Type.TRANSPORT_OFFICE, buildingPosition);
        collider = collider.cloneAndTranslate(buildingPosition);
        makeUnnavigable(collider);
    }

    public static void handleNpcsAndBuildingsOnClick() {
        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            for (Building building : buildings) {
                if (building.isMouseOver()) {
                    building.onClick();
                    return;
                }
            }
            for (Villager villager : villagers) {
                if (villager.isMouseOver()) {
                    villager.onClick();
                    return;
                }
            }
        }
    }

    public static void generateTiles() {
        Arrays.fill(tiles, Tile.GRASS);
        generateRiver();

        for (int i = 0; i < tiles.length; i++) {
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

        Harvestable tree = Harvestables.create(Harvestables.Type.BIG_TREE, pos);  //TODO randomise tree types
        int width = tree.getTexture().getRegionWidth() / TILE_SIZE;
        Vector2i trunk = new Vector2i(pos.x + width / 2, pos.y);
        if (smallNoise > 0.1f && bigNoise > 0.21f && isBuildable(trunk)) {
            makeUnnavigable(tree.getCollider());
            tree.nextPhase();
            fieldWorks.add(tree);
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
        if (smallNoise > -0.05f && bigNoise > 0.35f && isBuildable(pos)) {
            makeUnnavigable(rock.getCollider());
            rock.nextPhase();
            fieldWorks.add(rock);
            addGameObject(rock);
        }
    }

    private static void generateRiver() {
        double noiseFrequency = random.nextDouble() * 2 + 3;    //number of curves (3 <= x < 5)
        double curveMultiplier = random.nextDouble() * 4 + 16;  //curve size (16 <= x < 20)
        int width = 2;                                          //river width
        double bias = random.nextDouble() - 0.5f;               //the general direction in which the river is going (positive - right, negative - left) (-0.5 <= x < 0.5)
        float minDistanceFromEdge = 0.3f;

        int[] steps = new int[worldSize.y];
        double startX = random.nextInt((int) (worldSize.x * (1 - 2 * minDistanceFromEdge))) + (int) (worldSize.x * minDistanceFromEdge);
        for (int i = 0; i < steps.length; i++) {
            double di = (double) i / noiseSize;
            double noise = PerlinNoise.noise2D(di * noiseFrequency, seed) * curveMultiplier;
            steps[i] = (int) startX + (int) noise;
            startX += bias;
        }
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                if (x >= steps[y] - width && x <= steps[y] + width) {
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
        Pathfinding.updateCache(gridPosition);
    }

    public static void makeUnnavigable(BoxCollider area) {
        Vector2i tile = new Vector2i();
        for (int y = 0; y < area.getHeight(); y++) {
            for (int x = 0; x < area.getWidth(); x++) {
                tile.set(x + area.getGridPosition().x, y + area.getGridPosition().y);
                navigableTiles.remove(tile);
            }
        }
        Pathfinding.updateCache(area);
    }

    public static void makeNavigable(Vector2i gridPosition) {
        navigableTiles.add(gridPosition.clone());
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
        addGameObject(building);
        makeUnnavigable(building.getCollider());
        if (type == Buildings.Type.TRANSPORT_OFFICE) {
            Logistics.getTransportOffices().add((ProductionBuilding) building);
        } else if (type == Buildings.Type.STORAGE_BARN) {
            Logistics.getStorages().add((StorageBuilding) building);
        }
        for (Building b : buildings) {
            if (b instanceof ProductionBuilding && ((ProductionBuilding) b).isBuildingInRange(building)) {
                ((ProductionBuilding) b).getBuildingsInRange().add(building);
                ((ProductionBuilding) b).updateEfficiency();
            }
        }
    }

    public static void placeFarm(Buildings.Type type, Vector2i gridPosition, BoxCollider fieldCollider) {
        if (!type.isFarm()) throw new IllegalArgumentException("Wrong building type");

        Building building = Buildings.create(type, gridPosition);
        buildings.add(building);
        addGameObject(building);
        makeUnnavigable(building.getCollider());
        ((FarmBuilding<?>) building).setFieldCollider(fieldCollider);
        for (Building b : buildings) {
            if (b instanceof ProductionBuilding && ((ProductionBuilding) b).isBuildingInRange(building)) {
                ((ProductionBuilding) b).getBuildingsInRange().add(building);
                ((ProductionBuilding) b).updateEfficiency();
            }
        }

        if (type.farmAnimal != null) {
            for (int i = 0; i < 2; i++) {  //TODO hardcoded
                FarmAnimal animal = new FarmAnimal(
                        Animals.Type.COW,
                        ((FarmBuilding<?>) building).getFieldCollider().getGridPosition().clone(),
                        fieldCollider
                );
                World.spawnAnimal(animal);
                ((RanchBuilding) building).addFieldWork(animal);
            }
        }
    }

    public static void removeBuilding(Building building) {
        makeNavigable(building.getCollider());
        if (building instanceof ConstructionSite) fieldWorks.remove(building);
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

        if (fieldWork instanceof ConstructionSite constructionSite) {
            buildings.add(constructionSite);
        }
        else if (fieldWork instanceof Harvestable harvestable) {
            harvestable.nextPhase();
        }
        fieldWorks.add(fieldWork);
        changedFieldWorks.put(fieldWork, true);
        addGameObject((GameObject) fieldWork);
    }

    public static void removeFieldWorks() {
        for (FieldWork fieldWork : removedFieldWorks) {
            fieldWorks.remove(fieldWork);
            removeGameObject((GameObject) fieldWork);

            if (fieldWork instanceof ConstructionSite)
                buildings.remove(fieldWork);
        }
        removedFieldWorks.clear();
    }

    public static void removeFieldWorks(FieldWork fieldWork) {
        makeNavigable(fieldWork.getCollider());
        removedFieldWorks.add(fieldWork);
        if(changedFieldWorks.containsKey(fieldWork))
            changedFieldWorks.remove(fieldWork);
        else
            changedFieldWorks.put(fieldWork, false);
    }

    public static void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        for (int y = gameObject.getGridPosition().y + 1; y < objectsSumUpToLine.length; y++) {
            objectsSumUpToLine[y]++;
        }
    }

    private static void removeGameObject(GameObject gameObject) {
        gameObjects.remove(gameObject);
        for (int y = gameObject.getGridPosition().y + 1; y < objectsSumUpToLine.length; y++) {
            objectsSumUpToLine[y]--;
        }
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

        for (int y = gridULC.y + RENDER_BUFFER; y >= gridLRC.y - RENDER_BUFFER; y--) {
            if (Range.between(0, worldSize.y - 1).contains(y)) {
                for (int i = objectsSumUpToLine[y]; i < objectsSumUpToLine[y + 1]; i++) {
                    GameObject gameObject = gameObjects.get(gameObjects.size() - 1 - i);
                    if (gameObject.getGridPosition().x >= gridULC.x - RENDER_BUFFER && gameObject.getGridPosition().x <= gridLRC.x)
                        gameObject.draw(batch);
                }
            }
        }
    }

    public static void showBuildableTiles(SpriteBatch batch) {
        Vector2i pos = new Vector2i();
        for (int y = 0; y < worldSize.y; y++) {
            for (int x = 0; x < worldSize.x; x++) {
                pos.set(x, y);
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                if (!isBuildable(pos)) batch.draw(Textures.get(Textures.Tile.DEFAULT), x * TILE_SIZE, y * TILE_SIZE);
                batch.setColor(UI.DEFAULT_COLOR);
            }
        }
    }

    public static boolean isBuildable(Vector2i position) {
        if (worldSize.x * position.y + position.x < tiles.length) {
            Tile tile = getTile(position);
            return tile != Tile.WATER &&
                    navigableTiles.contains(position);
        } else return false;
    }

    public static int getStored(Resource resource) {
        return storedResources[resource.ordinal()];
    }

    public static void updateStoredResources(Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            storedResources[resource.ordinal()] += recipe.getChange(resource);
        }
        UI.getResourceList().updateData(recipe);
    }

    public static void updateStoredResources(Resource resource, int amount) {
        storedResources[resource.ordinal()] += amount;
        UI.getResourceList().updateData(resource, amount);
    }

    public static void setSeed(int seed) {
        World.seed = seed;
        random = new Random(seed);
    }

    public static int getSeed() {
        return seed;
    }

    public static void setWorldSize(int width, int height) {
        worldSize.set(width, height);
    }

    public static void setTime(int time) {
        World.time = time;
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

    public static void setTile(Vector2i gridPosition, Tile tile) {
        tiles[worldSize.x * gridPosition.y + gridPosition.x] = tile;
        tileTextures[worldSize.x * gridPosition.y + gridPosition.x] = tile.textures[random.nextInt(tile.textures.length)];
        changedTiles.put(gridPosition.clone(), tile);
    }

    private static void initVillagers(int num) {
        for (int i = 0; i < num; i++) {
            spawnVillager(new Villager((int) (Math.random() + 1d), new Vector2i(worldSize.x / 2, worldSize.y / 2)));
        }
    }

    public static Random getRandom() {
        return random;
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

    public static Set<Vector2i> getNavigableTiles() {
        return navigableTiles;
    }

    public static void saveWorld(ObjectOutputStream out) throws IOException {
        out.writeInt(World.getSeed());
        out.writeInt(World.getGridWidth());
        out.writeInt(World.getGridHeight());
        out.writeInt(World.getTime());

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
        init(in.readInt(), new Vector2i(in.readInt(), in.readInt()));
        setTime(in.readInt());

        BuilderGame.loadCollection(buildings, in);
        BuilderGame.loadCollection(villagers, in);
        BuilderGame.loadCollection(animals, in);
        BuilderGame.loadMap(changedFieldWorks, in);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            changedTiles.put((Vector2i)in.readObject(), Tile.valueOf(in.readUTF()));
        }

        villagers.forEach(World::addGameObject);

        for (Building building : buildings) {
            addGameObject(building);
            makeUnnavigable(building.getCollider());
            if (building.getType() == Buildings.Type.TRANSPORT_OFFICE) {
                Logistics.getTransportOffices().add((ProductionBuilding) building);
            } else if (building.getType() == Buildings.Type.STORAGE_BARN) {
                Logistics.getStorages().add((StorageBuilding) building);
            }
            if (building.getType().farmAnimal != null) {
                Tiles.createFence(((FarmBuilding<?>) building).getFieldCollider());
            }
        }


        for (FieldWork fieldWork : changedFieldWorks.keySet()) {
            if (changedFieldWorks.get(fieldWork)) {
                fieldWorks.add(fieldWork);
                addGameObject((GameObject) fieldWork);
                makeUnnavigable(fieldWork.getCollider());
            }
            else {
                fieldWorks.remove(fieldWork);
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
