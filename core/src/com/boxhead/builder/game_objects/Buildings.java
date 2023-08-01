package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.TileCircle;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;
import com.boxhead.builder.utils.Range;

import java.util.Arrays;
import java.util.Set;
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
                300,
                2,
                Jobs.MINER
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
        );


        public final Textures.Building texture;
        public final String name;
        /**
         * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
         */
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;
        public final Recipe buildCost;

        public final Job[] jobs;
        public final Service service;

        public final Harvestables.Type crop;
        public final Animals.Type farmAnimal;

        public final int residentCapacity, guestCapacity, workersPerShift;
        public int workerCapacity;
        public final int serviceInterval, productionInterval, range;
        public final Function<Set<Building>, Float> updateEfficiency;
        public transient Job mainJob;

        private boolean[] shifts = new boolean[] {false, true, false};

        static {
            Arrays.fill(HOSPITAL.shifts, true);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost, Job[] jobs,
             Service service, int serviceInterval, Harvestables.Type crop, Animals.Type farmAnimal, int residentCapacity, int guestCapacity,
             int workersPerShift, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {

            if ((crop != null && farmAnimal != null) ||
                    (guestCapacity > 0 && service == null))
                throw new IllegalArgumentException();

            this.texture = texture;
            this.name = name;
            this.entrancePosition = entrancePosition;
            this.relativeCollider = relativeCollider;
            this.buildCost = buildCost;
            this.jobs = jobs;
            this.service = service;
            this.serviceInterval = serviceInterval;
            this.crop = crop;
            this.farmAnimal = farmAnimal;
            this.residentCapacity = residentCapacity;
            this.guestCapacity = guestCapacity;
            this.workersPerShift = workersPerShift;
            this.productionInterval = productionInterval;
            this.range = range;

            if (updateEfficiency == null)
                this.updateEfficiency = (b -> 1f);
            else
                this.updateEfficiency = updateEfficiency;

            workerCapacity = 0;
            if (jobs != null) {
                mainJob = jobs[0];
                workerCapacity = jobs.length * workersPerShift * ProductionBuilding.shiftsPerJob;

                int producingJobs = 0;
                for (Job job : jobs) {
                    if (!job.getRecipe(null).isEmpty()) //job is a producing one
                        producingJobs++;
                }

                if (producingJobs > 1)
                    throw new IllegalArgumentException("More than one producing job in one building");
            }
        }

        //default collider
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job[] jobs,
             Service service, int serviceInterval, Harvestables.Type crop, Animals.Type farmAnimal, int residentCapacity, int guestCapacity,
             int workersPerShift, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, entrancePosition, new BoxCollider(
                            Vector2i.zero(),
                            Textures.get(texture).getRegionWidth() / World.TILE_SIZE,
                            Textures.get(texture).getRegionHeight() / World.TILE_SIZE),
                    buildCost, jobs, service, serviceInterval, crop, farmAnimal, residentCapacity, guestCapacity,
                    workersPerShift, productionInterval, range, updateEfficiency);
        }

        //residential
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, int residentCapacity) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, null, null, 0, null, null, residentCapacity, 0, 0, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, int residentCapacity) {
            this(texture, name, entrancePosition, buildCost, null, null, 0, null, null, residentCapacity, 0, 0, 0, 0, null);
        }

        //production
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, int productionInterval, int workersPerShift, Job... jobs) {
            this(texture, name, entrancePosition, buildCost, jobs, null, 0, null, null, 0, 0, workersPerShift, productionInterval, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int workersPerShift, int productionInterval, int range, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, new Job[]{job}, null, 0, null, null, 0, 0, workersPerShift, productionInterval, range, updateEfficiency);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int workersPerShift, int productionInterval, int range) {
            this(texture, name, entrancePosition, buildCost, relativeCollider, job, workersPerShift, productionInterval, range, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, BoxCollider relativeCollider, Job job, int workersPerShift) {
            this(texture, name, entrancePosition, buildCost, relativeCollider, job, workersPerShift, 0, 0, null);
        }

        //storage
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost) {
            this(texture, name, entrancePosition, buildCost, 0);
        }

        //farm
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int workersPerShift, Harvestables.Type crop, Animals.Type farmAnimal) {
            this(texture, name, entrancePosition, buildCost, new Job[]{job}, null, 0, crop, farmAnimal, 0, 0, workersPerShift, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int workersPerShift, Animals.Type farmAnimal) {
            this(texture, name, entrancePosition, buildCost, job, workersPerShift, null, farmAnimal);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int workersPerShift, Harvestables.Type crop) {
            this(texture, name, entrancePosition, buildCost, job, workersPerShift, crop, null);
        }

        //service
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Service service, int serviceInterval, int guestCapacity, int workersPerShift, Job... jobs) {
            this(texture, name, entrancePosition, buildCost, jobs, service, serviceInterval, null, null, 0, guestCapacity, workersPerShift, 0, 0, null);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int workersPerShift, Service service, int serviceInterval, int guestCapacity) {
            this(texture, name, entrancePosition, buildCost, new Job[]{job}, service, serviceInterval, null, null, 0, guestCapacity, workersPerShift, 0, 0, null);
        }

        //service that is also producing
        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost, Job job, int workersPerShift, Service service, int serviceInterval, int guestCapacity, int productionInterval) {
            this(texture, name, entrancePosition, buildCost, new Job[]{job}, service, serviceInterval, null, null, 0, guestCapacity, workersPerShift, productionInterval, 0, null);
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
            return jobs != null;
        }

        public boolean isResidential() {
            return residentCapacity > 0;
        }

        public boolean isStorage() {
            return entrancePosition != null;
        }
    }

    public static Building create(Type type, Vector2i gridPosition) {
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

        TextureRegion texture = currentBuilding.getTexture();

        if (!Tiles.isInFieldMode()) {
            Vector2 mousePos = GameScreen.getMouseWorldPosition();

            int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
            int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

            int posX = mouseX - (mouseX % World.TILE_SIZE);
            int posY = mouseY - (mouseY % World.TILE_SIZE);

            posX = rangeX.fit(posX);
            posY = rangeY.fit(posY);

            if (currentBuilding.range > 0) {
                showBuildingRange(batch,
                        posX + currentBuilding.entrancePosition.x * World.TILE_SIZE,
                        posY + currentBuilding.entrancePosition.y * World.TILE_SIZE,
                        currentBuilding.range);
            }
            boolean isBuildable = checkAndShowTileAvailability(batch, posX, posY);

            batch.setColor(UI.SEMI_TRANSPARENT);
            batch.draw(texture, posX, posY);
            batch.setColor(UI.DEFAULT_COLOR);

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isBuildable) {
                if (!currentBuilding.isFarm()) {
                    Vector2i buildingPosition = new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE);
                    World.placeFieldWork(new ConstructionSite(currentBuilding, buildingPosition, 100));

                    if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                        isInBuildingMode = false;
                } else {
                    Tiles.toFieldMode(
                            new BoxCollider(
                                    posX / World.TILE_SIZE,
                                    posY / World.TILE_SIZE,
                                    currentBuilding.relativeCollider.getWidth(),
                                    currentBuilding.relativeCollider.getHeight()),
                            currentBuilding.farmAnimal != null,
                            FarmBuilding.MIN_FIELD_SIZE,
                            FarmBuilding.MAX_FIELD_SIZE);
                }
            }
        } else {
            batch.setColor(UI.SEMI_TRANSPARENT);
            batch.draw(
                    texture,
                    Tiles.getBuildingCollider().getGridPosition().x * World.TILE_SIZE,
                    Tiles.getBuildingCollider().getGridPosition().y * World.TILE_SIZE
            );
            BoxCollider fieldCollider = Tiles.handleFieldMode(batch);
            if (fieldCollider != null) {
                World.placeFieldWork(new ConstructionSite(
                        currentBuilding,
                        Tiles.getBuildingCollider().getGridPosition().clone(),
                        100,
                        fieldCollider));

                Tiles.turnOffFieldMode();
                if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                    isInBuildingMode = false;
            }
        }
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

    private static boolean checkAndShowTileAvailability(SpriteBatch batch, int posX, int posY) {
        boolean isBuildable = true;
        for (int y = 0; y < currentBuilding.relativeCollider.getHeight(); y++) {
            for (int x = 0; x < currentBuilding.relativeCollider.getWidth(); x++) {
                if (World.isBuildable(new Vector2i(posX / World.TILE_SIZE + x, posY / World.TILE_SIZE + y)))
                    batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
                else {
                    batch.setColor(UI.SEMI_TRANSPARENT_RED);
                    isBuildable = false;
                }
                batch.draw(Textures.get(Textures.Tile.DEFAULT), posX + x * World.TILE_SIZE, posY + y * World.TILE_SIZE);
            }
        }
        if (currentBuilding.entrancePosition != null) {
            Vector2i entrancePos = new Vector2i(posX / World.TILE_SIZE + currentBuilding.entrancePosition.x,
                    posY / World.TILE_SIZE + currentBuilding.entrancePosition.y);
            if (rangeX.contains(entrancePos.x * World.TILE_SIZE) && rangeY.contains(entrancePos.y * World.TILE_SIZE) && World.isBuildable(entrancePos))
                batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            else {
                batch.setColor(UI.SEMI_TRANSPARENT_RED);
                isBuildable = false;
            }

            batch.draw(Textures.get(Textures.Tile.DEFAULT),
                    posX + currentBuilding.entrancePosition.x * World.TILE_SIZE,
                    posY + currentBuilding.entrancePosition.y * World.TILE_SIZE);
        }
        return isBuildable;
    }

    private static void showBuildingRange(SpriteBatch batch, int posX, int posY, int range) {
        batch.setColor(UI.VERY_TRANSPARENT);
        TileCircle.draw(
                batch,
                Textures.get(Textures.Tile.DEFAULT),
                posX,
                posY,
                range * World.TILE_SIZE);
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
