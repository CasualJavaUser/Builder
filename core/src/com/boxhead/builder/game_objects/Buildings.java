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
import org.apache.commons.lang3.Range;

import java.util.Set;
import java.util.function.Function;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static boolean isInDemolishingMode = false;
    private static Type currentBuilding;
    private static Range<Integer> rangeX, rangeY;

    public enum Type {
        LOG_CABIN (
                Textures.Building.LOG_CABIN,
                "log cabin",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 4, 2),
                5,
                new Recipe(Pair.of(Resource.WOOD, 20))
        ),
        LUMBERJACKS_HUT (
                Textures.Building.LUMBERJACKS_HUT,
                "lumberjack's hut",
                Jobs.LUMBERJACK,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 3),
                1,
                100,
                15,
                new Recipe(Pair.of(Resource.WOOD, 20))
        ),
        MINE (
                Textures.Building.MINE,
                "mine", Jobs.MINER,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 3, 3),
                3,
                300,
                10,
                new Recipe(Pair.of(Resource.WOOD, 40),
                           Pair.of(Resource.STONE, 20)),
                (buildingsInRange) -> {
                    float efficiency = 1f - buildingsInRange.size() / 3f;
                    if (efficiency < 0) efficiency = 0;
                    return efficiency;
                }
        ),
        DEFAULT_SERVICE_BUILDING (
                Textures.Building.SERVICE_FUNGUS,
                "hospital",
                Jobs.DOCTOR,
                Service.HEAL,
                new Vector2i(0, -1),
                5,
                100,
                10,
                100,
                new Recipe(Pair.of(Resource.WOOD, 30),
                           Pair.of(Resource.STONE, 30))),
        STORAGE_BARN (
                Textures.Building.STORAGE_BARN,
                "storage barn",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 5, 3),
                new Recipe(Pair.of(Resource.WOOD, 50))
        ),
        BUILDERS_HUT (
                Textures.Building.BUILDERS_HUT,
                "builder's hut",
                Jobs.BUILDER,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                5,
                0,
                new Recipe(Pair.of(Resource.WOOD, 20))
        ),
        TRANSPORT_OFFICE (
                Textures.Building.CARRIAGE_HOUSE,
                "carriage house",
                Jobs.CARRIER,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 5, 2),
                5,
                0,
                new Recipe(Pair.of(Resource.WOOD, 20))
        ),
        STONE_GATHERERS (
                Textures.Building.STONE_GATHERERS_SHACK,
                "stone gatherer's shack",
                Jobs.STONEMASON,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                2,
                0,
                15,
                new Recipe(Pair.of(Resource.WOOD, 30))
        ),
        PLANTATION(
                Textures.Building.TOOL_SHACK,
                "tool shack", Jobs.FARMER,
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                1,
                0,
                true,
                new Recipe(Pair.of(Resource.WOOD, 10))
        );

        public final Textures.Building texture;
        public String name;
        public final Job job;
        public final Service service;
        /**
         * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
         */
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;
        public final int npcCapacity, productionInterval, guestCapacity, serviceInterval;
        public final int range;
        /**
         * True if this type of building is a plantation. Only applies to farm buildings. (Null if not a farm building)
         */
        public final Boolean isPlantation;
        public final Recipe buildCost;
        /**
         * Returns building's efficiency based on the buildings in range.
         */
        public final Function<Set<Building>, Float> updateEfficiency;

        Type(Textures.Building texture, String name, Job job, Service service, Vector2i entrancePosition, BoxCollider relativeCollider,
             int npcCapacity, int productionInterval, int guestCapacity, int serviceInterval, int range, Boolean isPlantation, Recipe buildCost, Function<Set<Building>, Float> updateEfficiency) {
            this.texture = texture;
            this.job = job;
            this.service = service;
            this.entrancePosition = entrancePosition;
            this.relativeCollider = relativeCollider;
            this.name = name;
            this.npcCapacity = npcCapacity;
            this.productionInterval = productionInterval;
            this.guestCapacity = guestCapacity;
            this.serviceInterval = serviceInterval;
            this.range = range;
            this.isPlantation = isPlantation;
            this.buildCost = buildCost;
            this.updateEfficiency = updateEfficiency;
        }

        //service
        Type(Textures.Building texture, String name, Job job, Service service, Vector2i entrancePosition, BoxCollider relativeCollider,
             int npcCapacity, int productionInterval, int guestCapacity, int serviceInterval, int range, Recipe buildCost, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, job, service, entrancePosition, relativeCollider, npcCapacity, productionInterval, guestCapacity, serviceInterval, range, null, buildCost, updateEfficiency);
        }

        Type(Textures.Building texture, String name, Job job, Service service, Vector2i entrancePosition, BoxCollider relativeCollider,
             int npcCapacity, int productionInterval, int guestCapacity, int serviceInterval, Recipe buildCost) {
            this(texture, name, job, service, entrancePosition, relativeCollider, npcCapacity, productionInterval, guestCapacity, serviceInterval, 0, buildCost, (b) -> 1f);
        }

        Type(Textures.Building texture, String name, Job job, Service service, Vector2i entrancePosition,
             int npcCapacity, int productionInterval, int guestCapacity, int serviceCapacity, Recipe buildCost) {
            this(
                    texture, name, job, service, entrancePosition,
                    new BoxCollider(
                            Vector2i.zero(),
                            Textures.get(texture).getRegionWidth() / World.TILE_SIZE,
                            Textures.get(texture).getRegionHeight() / World.TILE_SIZE),
                    npcCapacity, productionInterval,
                    guestCapacity, serviceCapacity,
                    buildCost);
        }

        //production
        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, int productionInterval, int range, Recipe buildCost, Function<Set<Building>, Float> updateEfficiency) {
            this(texture, name, job, null, entrancePosition, relativeCollider, npcCapacity, productionInterval, 0, 0, range, buildCost, updateEfficiency);
        }

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, int productionInterval, int range, Recipe buildCost) {
            this(texture, name, job, entrancePosition, relativeCollider, npcCapacity, productionInterval, range, buildCost, (b) -> 1f);
        }

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, int productionInterval, boolean isPlantation, Recipe buildCost) {
            this(texture, name, job, null, entrancePosition, relativeCollider, npcCapacity, productionInterval, 0, 0, 0, isPlantation, buildCost, (b) -> 1f);
        }

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, name, job, null, entrancePosition, relativeCollider, npcCapacity, productionInterval, 0, 0, buildCost);
        }

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, name, job, null, entrancePosition, npcCapacity, productionInterval, 0, 0, buildCost);
        }

        Type(Textures.Building texture, Job job, Vector2i entrancePosition, int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, null, job, entrancePosition, npcCapacity, productionInterval, buildCost);
            name = defaultName();
        }

        //residential
        Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, Recipe buildCost) {
            this(texture, name, null, null, entrancePosition, relativeCollider, npcCapacity, 0, 0, 0, buildCost);
        }

        Type(Textures.Building texture, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, Recipe buildCost) {
            this(texture, null, null, null, entrancePosition, relativeCollider, npcCapacity, 0, 0, 0, buildCost);
            name = defaultName();
        }

        //storage
        Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost) {
            this(texture, name, null, null, entrancePosition, relativeCollider, 0, 0, 0, 0, buildCost);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost) {
            this(texture, name, null, null, entrancePosition, 0, 0, 0, 0, buildCost);
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

        private String defaultName() {
            return name().toLowerCase().replace('_', ' ');
        }

        public boolean isFarm() {
            return isPlantation != null;
        }

        public boolean isService() {
            return service != null;
        }

        public boolean isProduction() {
            return job != null;
        }

        public boolean isResidential() {
            return npcCapacity > 0 && job == null;
        }

        public boolean isStorage() {
            return entrancePosition != null;
        }
    }

    public static Building create(Type type, Vector2i gridPosition) {
        if (type.isFarm()) return new FarmBuilding(type, gridPosition);
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

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)  && isBuildable) {
            Vector2i buildingPosition = new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE);
            World.placeFieldWork(new ConstructionSite(currentBuilding, buildingPosition, 100));

            if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                isInBuildingMode = false;
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
                if(World.isBuildable(new Vector2i(posX/World.TILE_SIZE + x, posY/World.TILE_SIZE + y)))
                    batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
                else {
                    batch.setColor(UI.SEMI_TRANSPARENT_RED);
                    isBuildable = false;
                }
                batch.draw(Textures.get(Textures.Tile.DEFAULT), posX + x * World.TILE_SIZE, posY + y * World.TILE_SIZE);
            }
        }
        if(currentBuilding.entrancePosition != null) {
            Vector2i entrancePos = new Vector2i(posX/World.TILE_SIZE + currentBuilding.entrancePosition.x,
                                                posY/World.TILE_SIZE + currentBuilding.entrancePosition.y);
            if(rangeX.contains(entrancePos.x * World.TILE_SIZE) && rangeY.contains(entrancePos.y * World.TILE_SIZE) && World.isBuildable(entrancePos))
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
