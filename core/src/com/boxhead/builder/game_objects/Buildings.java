package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.Function;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static boolean isInDemolishingMode = false;
    private static Type currentBuilding;
    private static Range<Integer> rangeX, rangeY;

    public enum Type {
        LOG_CABIN(
                Textures.Building.LOG_CABIN,
                "log cabin",
                new Vector2i(2, -1),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                new BoxCollider(0, 0, 4, 2),
                5
        ),
        LUMBERJACKS_HUT(
                Textures.Building.LUMBERJACKS_HUT,
                "lumberjack's hut",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                new BoxCollider(0, 0, 4, 3),
                Jobs.LUMBERJACK,
                1,
                0,
                15
        ),
        MINE(
                Textures.Building.MINE,
                "mine",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 40),
                        Pair.of(Resource.STONE, 20)),
                Jobs.MINER,
                2,
                300,
                10,
                (buildingsInRange) -> {
                    float efficiency = 1f - buildingsInRange.size() / 3f;
                    if (efficiency < 0) efficiency = 0;
                    return efficiency;
                }
        ),
        STORAGE_BARN(
                Textures.Building.STORAGE_BARN,
                "storage barn",
                new Vector2i(2, -1),
                new Recipe(Pair.of(Resource.WOOD, 50))
        ),
        BUILDERS_HUT(
                Textures.Building.BUILDERS_HUT,
                "builder's hut",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                new BoxCollider(0, 0, 4, 2),
                Jobs.BUILDER,
                3
        ),
        TRANSPORT_OFFICE(
                Textures.Building.CARRIAGE_HOUSE,
                "carriage house",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                new BoxCollider(0, 0, 5, 2),
                Jobs.CARRIER,
                5
        ),
        STONE_GATHERERS(
                Textures.Building.STONE_GATHERERS_SHACK,
                "stone gatherer's shack",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 30)),
                new BoxCollider(0, 0, 4, 2),
                Jobs.STONEMASON,
                2,
                0,
                15
        ),
        PLANTATION(
                Textures.Building.TOOL_SHACK,
                "plantation",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.FARMER,
                3,
                Harvestables.Type.WHEAT
        ),
        RANCH(
                Textures.Building.TOOL_SHACK,
                "ranch",
                new Vector2i(1, -1),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.FARMER,
                3,
                Animals.Type.PIG
        ),
        PUB(
                Textures.Building.FUNGUS,
                "pub",
                new Vector2i(0, -1),
                new Recipe(Pair.of(Resource.WOOD, 10)),
                Jobs.BARTENDER,
                2,
                Service.BARTENDING,
                20,
                5,
                200
        ),
        HOSPITAL(
                Textures.Building.SERVICE_FUNGUS,
                "hospital",
                new Vector2i(0, -1),
                new Recipe(Pair.of(Resource.WOOD, 30),
                        Pair.of(Resource.STONE, 30)),
                Service.HEALTHCARE,
                100,
                10,
                3,
                Jobs.DOCTOR
        ),
        SCHOOL(
                Textures.Building.SERVICE_FUNGUS,
                "school",
                new Vector2i(0, -1),
                new Recipe(Pair.of(Resource.WOOD, 50)),
                new BoxCollider(0, 0, 2, 2),
                Jobs.TEACHER,
                10,
                3
        );


        public final Textures.Building texture;
        public final String name;
        /**
         * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
         */
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;
        public final Recipe buildCost;

        public final Job job;
        public final Service service;

        public final Harvestables.Type crop;
        public final Animals.Type farmAnimal;

        public final int residentCapacity, guestCapacity, studentCapacity, maxEmployeeCapacity;
        public final int serviceInterval, productionInterval, range;
        public final Function<Set<Building>, Float> updateEfficiency;

        private final boolean[] shifts = new boolean[]{false, true, false};

        static {
            Arrays.fill(HOSPITAL.shifts, true);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost, Job job,
             Service service, int serviceInterval, Harvestables.Type crop, Animals.Type farmAnimal, int residentCapacity, int guestCapacity,
             int studentCapacity, int maxEmployeeCapacity, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {

            if ((crop != null && farmAnimal != null) ||
                    (guestCapacity > 0 && service == null))
                throw new IllegalArgumentException();

            this.texture = texture;
            this.name = name;
            this.entrancePosition = entrancePosition;
            this.relativeCollider = relativeCollider;
            this.buildCost = buildCost;
            this.job = job;
            this.service = service;
            this.serviceInterval = serviceInterval;
            this.crop = crop;
            this.farmAnimal = farmAnimal;
            this.residentCapacity = residentCapacity;
            this.guestCapacity = guestCapacity;
            this.studentCapacity = studentCapacity;
            this.maxEmployeeCapacity = maxEmployeeCapacity;
            this.productionInterval = productionInterval;
            this.range = range;

            if (updateEfficiency == null)
                this.updateEfficiency = (b -> 1f);
            else
                this.updateEfficiency = updateEfficiency;
        }

        //default collider
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job,
             Service service, int serviceInterval, Harvestables.Type crop, Animals.Type farmAnimal, int residentCapacity, int guestCapacity,
             int studentCapacity, int maxEmployeeCapacity, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, entrancePosition, new BoxCollider(
                            Vector2i.zero(),
                            Textures.get(texture).getRegionWidth() / World.TILE_SIZE,
                            Textures.get(texture).getRegionHeight() / World.TILE_SIZE),
                    buildCost, job, service, serviceInterval, crop, farmAnimal, residentCapacity, guestCapacity,
                    studentCapacity, maxEmployeeCapacity, productionInterval, range, updateEfficiency);
        }

        //residential
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, int residentCapacity) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, null, null, 0, null, null, residentCapacity, 0, 0, 0, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, int residentCapacity) {
            this(texture, name, entrancePosition, buildCost, null, null, 0, null, null, residentCapacity, 0, 0, 0, 0, 0, null);
        }

        //production
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, int productionInterval, int maxEmployeeCapacity, Job job) {
            this(texture, name, entrancePosition, buildCost, job, null, 0, null, null, 0, 0, 0, maxEmployeeCapacity, productionInterval, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, entrancePosition, buildCost, job, null, 0, null, null, 0, 0, 0, maxEmployeeCapacity, productionInterval, range, updateEfficiency);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int maxEmployeeCapacity, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job, null, 0, null, null, 0, 0, 0, maxEmployeeCapacity, productionInterval, range, updateEfficiency);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int maxEmployeeCapacity, int productionInterval, int range) {
            this(texture, name, entrancePosition, buildCost, relativeCollider, job, maxEmployeeCapacity, productionInterval, range, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int maxEmployeeCapacity) {
            this(texture, name, entrancePosition, buildCost, relativeCollider, job, maxEmployeeCapacity, 0, 0, null);
        }

        //school
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int studentCapacity, int maxEmployeeCapacity) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job,
                    null, 0, null, null, 0, 0,
                    studentCapacity, maxEmployeeCapacity, 0, 0, null);
        }

        //storage
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost) {
            this(texture, name, entrancePosition, buildCost, 0);
        }

        //farm
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, Harvestables.Type crop, Animals.Type farmAnimal) {
            this(texture, name, entrancePosition, buildCost, job, null, 0, crop, farmAnimal, 0, 0, 0, maxEmployeeCapacity, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, Animals.Type farmAnimal) {
            this(texture, name, entrancePosition, buildCost, job, maxEmployeeCapacity, null, farmAnimal);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, Harvestables.Type crop) {
            this(texture, name, entrancePosition, buildCost, job, maxEmployeeCapacity, crop, null);
        }

        //service
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Service service, int serviceInterval, int guestCapacity, int maxEmployeeCapacity, Job job) {
            this(texture, name, entrancePosition, buildCost, job, service, serviceInterval, null, null, 0, guestCapacity, 0, maxEmployeeCapacity, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, Service service, int serviceInterval, int guestCapacity) {
            this(texture, name, entrancePosition, buildCost, job, service, serviceInterval, null, null, 0, guestCapacity, 0, maxEmployeeCapacity, 0, 0, null);
        }

        //service that is also producing
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int maxEmployeeCapacity, Service service, int serviceInterval, int guestCapacity, int productionInterval) {
            this(texture, name, entrancePosition, buildCost, job, service, serviceInterval, null, null, 0, guestCapacity, 0, maxEmployeeCapacity, productionInterval, 0, null);
        }

        public TextureRegion getTexture() {
            return Textures.get(texture);
        }

        public Textures.TextureId getConstructionSite() {
            try {
                return Textures.Building.valueOf(texture.name() + "_CS");
            } catch (IllegalArgumentException e) {
                return texture;
            }
        }

        public void setShiftActivity(int index, boolean active) {
            if (!this.isProduction())
                throw new IllegalStateException();

            shifts[index] = active;

            for (Building building : World.getBuildings()) {
                if (building.type == this) {
                    ((ProductionBuilding) building).setShiftActivity(index, active);
                }
            }
        }

        public boolean getShiftActivity(int index) {
            return shifts[index];
        }

        private String defaultName() {
            return name().toLowerCase().replace('_', ' ');
        }

        public boolean isSchool() {
            return studentCapacity > 0;
        }

        public boolean isFarm() {
            return crop != null || farmAnimal != null;
        }

        public boolean isPlantation() {
            return crop != null;
        }

        public boolean isRanch() {
            return farmAnimal != null;
        }

        public boolean isService() {
            return service != null;
        }

        public boolean isProduction() {
            return job != null;
        }

        public boolean isResidential() {
            return residentCapacity > 0;
        }

        public boolean isStorage() {
            return entrancePosition != null;
        }
    }

    public static Building create(Type type, Vector2i gridPosition) {
        if (type.isSchool()) return new SchoolBuilding(type, gridPosition);
        if (type.isPlantation()) return new PlantationBuilding(type, gridPosition);
        if (type.isRanch()) return new RanchBuilding(type, gridPosition);
        if (type.isService()) return new ServiceBuilding(type, gridPosition);
        if (type.isProduction()) return new ProductionBuilding(type, gridPosition);
        if (type.isResidential()) return new ResidentialBuilding(type, gridPosition);
        if (type.isStorage()) return new StorageBuilding(type, gridPosition);
        return new Building(type, gridPosition);
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode || isInDemolishingMode)
            throw new IllegalStateException("Not in building mode");

        if (Tiles.isInFieldMode()) {
            handleFieldMode(batch);
            return;
        }

        TextureRegion texture = currentBuilding.getTexture();
        Vector2 mousePos = GameScreen.getMouseWorldPosition();

        //center texture around mouse
        int screenX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int screenY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        //snap texture to grid within map boundaries
        screenX = rangeX.fit(screenX - (screenX % World.TILE_SIZE));
        screenY = rangeY.fit(screenY - (screenY % World.TILE_SIZE));

        Vector2i gridPosition = new Vector2i(screenX, screenY).divide(World.TILE_SIZE);

        if (currentBuilding.range > 0) {
            showBuildingRange(batch,
                    gridPosition.add(currentBuilding.entrancePosition),
                    currentBuilding.range);
        }
        boolean isBuildable = checkAndShowTileAvailability(batch, gridPosition);

        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(texture, screenX, screenY);
        batch.setColor(UI.DEFAULT_COLOR);

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE) && isBuildable) {
            if (!currentBuilding.isFarm()) {
                ConstructionSite constructionSite = new ConstructionSite(currentBuilding, gridPosition, 100);
                Harvestable onEntrance = World.findHarvestables(constructionSite.getEntrancePosition());
                if (onEntrance != null) World.removeFieldWorks(onEntrance);
                World.removeFieldWorks(constructionSite.getCollider());
                World.placeFieldWork(constructionSite);
                World.makeBuilt(constructionSite.getCollider());

                if (!InputManager.isKeyDown(InputManager.CONTROL))
                    isInBuildingMode = false;
            } else {
                Tiles.toFieldMode(
                        currentBuilding.relativeCollider.cloneAndTranslate(gridPosition),
                        currentBuilding.farmAnimal != null,
                        FarmBuilding.MIN_FIELD_SIZE,
                        FarmBuilding.MAX_FIELD_SIZE);
            }
        }
    }

    private static void handleFieldMode(SpriteBatch batch) {
        TextureRegion texture = currentBuilding.getTexture();

        batch.setColor(UI.SEMI_TRANSPARENT);
        batch.draw(
                texture,
                Tiles.getBuildingCollider().getGridPosition().x * World.TILE_SIZE,
                Tiles.getBuildingCollider().getGridPosition().y * World.TILE_SIZE
        );
        BoxCollider fieldCollider = Tiles.handleFieldMode(batch);
        if (fieldCollider == null) return;  //field hasn't been placed yet

        ConstructionSite constructionSite = new ConstructionSite(
                currentBuilding,
                Tiles.getBuildingCollider().getGridPosition().clone(),
                100,
                fieldCollider);
        Harvestable onEntrance = World.findHarvestables(constructionSite.getEntrancePosition());
        if (onEntrance != null) World.removeFieldWorks(onEntrance);
        World.removeFieldWorks(constructionSite.getCollider());
        World.removeFieldWorks(fieldCollider);
        World.placeFieldWork(constructionSite);
        World.makeBuilt(constructionSite.getCollider());
        World.makeBuilt(fieldCollider);

        Tiles.turnOffFieldMode();
        if (!InputManager.isKeyDown(InputManager.CONTROL))
            isInBuildingMode = false;
    }

    public static void handleDemolishingMode() {
        if (!isInDemolishingMode || isInBuildingMode)
            throw new IllegalStateException("Not in demolishing mode");

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            for (Building building : World.getBuildings()) {
                if (building.isMouseOver()) {
                    World.removeBuilding(building);
                    if (!InputManager.isKeyDown(Input.Keys.CONTROL_LEFT)) isInDemolishingMode = false;
                    break;
                }
            }
        }
    }

    public static void toBuildingMode(Type building) {
        currentBuilding = building;
        rangeX = Range.between(0, World.getWidth() - currentBuilding.getTexture().getRegionWidth());
        rangeY = Range.between(0, World.getHeight() - currentBuilding.getTexture().getRegionHeight());
        isInDemolishingMode = false;
        isInBuildingMode = true;
    }

    public static void toDemolishingMode() {
        isInDemolishingMode = true;
        isInBuildingMode = false;
    }

    public static void turnOffBuildingMode() {
        isInBuildingMode = false;
        Tiles.turnOffFieldMode();
    }

    public static void turnOffDemolishingMode() {
        isInDemolishingMode = false;
    }

    public static boolean isInBuildingMode() {
        return isInBuildingMode;
    }

    public static boolean isInDemolishingMode() {
        return isInDemolishingMode;
    }

    private static boolean checkAndShowTileAvailability(SpriteBatch batch, Vector2i gridPosition) {
        boolean isBuildable = true;
        BoxCollider area = currentBuilding.relativeCollider.cloneAndTranslate(gridPosition);
        List<Harvestable> harvestables = World.findHarvestables(area);
        List<Vector2i> harvestablePositions = new ArrayList<>(harvestables.size());
        for (Harvestable harvestable : harvestables) {
            harvestablePositions.add(harvestable.getCollider().getGridPosition());
        }

        for (Vector2i tile : area) {
            if (harvestablePositions.contains(tile)) {
                batch.setColor(UI.SEMI_TRANSPARENT_YELLOW);
            } else if (World.isBuildable(tile)) {
                batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            } else {
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                isBuildable = false;
            }

            batch.draw(Textures.get(Textures.Tile.DEFAULT), tile.x * World.TILE_SIZE, tile.y * World.TILE_SIZE);
        }
        if (currentBuilding.entrancePosition != null) {
            Vector2i entrancePos = currentBuilding.entrancePosition.add(gridPosition);
            if (rangeX.contains(entrancePos.x) && rangeY.contains(entrancePos.y) && World.isBuildable(entrancePos))
                batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            else {
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                isBuildable = false;
            }

            batch.draw(Textures.get(Textures.Tile.DEFAULT),
                    entrancePos.x * World.TILE_SIZE,
                    entrancePos.y * World.TILE_SIZE);
        }
        return isBuildable;
    }

    private static void showBuildingRange(SpriteBatch batch, Vector2i gridPosition, int range) {
        batch.setColor(UI.VERY_TRANSPARENT);
        Circle.draw(
                batch,
                Textures.get(Textures.Tile.DEFAULT),
                gridPosition,
                range);
        batch.setColor(UI.DEFAULT_COLOR);
    }

    public static void saveShiftActivity(ObjectOutputStream oos) throws IOException {
        for (Type type : Type.values()) {
            for (boolean shift : type.shifts) {
                oos.writeBoolean(shift);
            }
        }
    }

    public static void loadShiftActivity(ObjectInputStream ois) throws IOException {
        for (Type type : Type.values()) {
            for (int i = 0; i < type.shifts.length; i++) {
                type.shifts[i] = ois.readBoolean();
            }
            UI.loadShiftMenuValues();
        }
    }
}
