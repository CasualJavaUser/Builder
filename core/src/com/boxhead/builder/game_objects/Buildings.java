package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.TileCircle;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class Buildings {
    private static boolean isInBuildingMode = false;
    private static boolean isDemolishing = false;
    private static Type currentBuilding;
    private static Range<Integer> rangeX, rangeY;

    public enum Type {
        LOG_CABIN
                (Textures.Building.LOG_CABIN, "log cabin", new Vector2i(2, -1), new BoxCollider(0, 0, 4, 2), 5,
                        new Recipe(Pair.of(Resource.WOOD, 20))),
        LUMBERJACKS_HUT
                (Textures.Building.LUMBERJACKS_HUT, "lumberjack's hut", Jobs.LUMBERJACK, new Vector2i(1, -1), new BoxCollider(0, 0, 4, 3), 1, 100,
                        new Recipe(Pair.of(Resource.WOOD, 20))),
        MINE
                (Textures.Building.MINE, "mine", Jobs.MINER, new Vector2i(1, -1), 3, 300,
                        new Recipe(Pair.of(Resource.WOOD, 40),
                                Pair.of(Resource.STONE, 20))),
        DEFAULT_SERVICE_BUILDING
                (Textures.Building.SERVICE_FUNGUS, "hospital", Jobs.DOCTOR, Service.HEAL, new Vector2i(0, -1), 5, 100, 10, 100,
                        new Recipe(Pair.of(Resource.WOOD, 30),
                                Pair.of(Resource.STONE, 30))),
        DEFAULT_STORAGE_BUILDING
                (Textures.Building.STORAGE_FUNGUS, "storage", new Vector2i(1, -1),
                        new Recipe(Pair.of(Resource.WOOD, 50))),
        BUILDERS_HUT
                (Textures.Building.BUILDERS_HUT, Jobs.BUILDER, new Vector2i(0, -1), 5, 0,
                        new Recipe(Pair.of(Resource.WOOD, 20))),
        TRANSPORT_OFFICE
                (Textures.Building.FUNGUS, Jobs.CARRIER, new Vector2i(2, 0), 5, 0,
                        new Recipe(Pair.of(Resource.WOOD, 20))),
        STONE_GATHERERS
                (Textures.Building.SERVICE_FUNGUS, Jobs.STONEMASON, new Vector2i(0, -1), 2, 0,
                        new Recipe(Pair.of(Resource.WOOD, 30)));

        public final Textures.Building texture;
        public String name;
        public final Job job;
        public final Service service;
        /**
         * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
         */
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;
        public final Recipe buildCost;
        public final int npcCapacity, productionInterval, guestCapacity, serviceInterval;

        Type(Textures.Building texture, String name, Job job, Service service, Vector2i entrancePosition, BoxCollider relativeCollider,
             int npcCapacity, int productionInterval, int guestCapacity, int serviceInterval, Recipe buildCost) {
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
            this.buildCost = buildCost;
        }

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, BoxCollider relativeCollider,
             int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, name, job, null, entrancePosition, relativeCollider, npcCapacity, productionInterval, 0, 0, buildCost);
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

        Type(Textures.Building texture, String name, Job job, Vector2i entrancePosition, int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, name, job, null, entrancePosition, npcCapacity, productionInterval, 0, 0, buildCost);
        }

        Type(Textures.Building texture, Job job, Vector2i entrancePosition, int npcCapacity, int productionInterval, Recipe buildCost) {
            this(texture, null, job, entrancePosition, npcCapacity, productionInterval, buildCost);
            name = defaultName();
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, int npcCapacity, Recipe buildCost) {
            this(texture, name, null, null, entrancePosition, relativeCollider, npcCapacity, 0, 0, 0, buildCost);
        }

        Type(Textures.Building texture, String name, Vector2i entrancePosition, Recipe buildCost) {
            this(texture, name, null, null, entrancePosition, 0, 0, 0, 0, buildCost);
        }

        public TextureRegion getTexture() {
            return Textures.get(texture);
        }

        public TextureRegion getConstructionSite() {
            try {
                return Textures.get(Textures.Building.valueOf(texture.name() + "_CS"));
            } catch (IllegalArgumentException e) {
                return getTexture();
            }
        }

        private String defaultName() {
            return name().toLowerCase().replace('_', ' ');
        }
    }

    public static Building create(Type type, Vector2i gridPosition) {
        if (type.service != null) return new ServiceBuilding(type, gridPosition);
        if (type.job != null) return new ProductionBuilding(type, gridPosition);
        if (type.npcCapacity > 0) return new ResidentialBuilding(type, gridPosition);
        if (type.entrancePosition != null) return new StorageBuilding(type, gridPosition);
        return new Building(type, gridPosition);
    }

    public static void handleBuildingMode(SpriteBatch batch) {
        if (!isInBuildingMode || isDemolishing)
            throw new IllegalStateException("Not in building mode");

        TextureRegion texture = currentBuilding.getTexture();
        Vector3 mousePos = GameScreen.getMouseWorldPosition();

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2;
        int mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE);
        int posY = mouseY - (mouseY % World.TILE_SIZE);

        posX = rangeX.fit(posX);
        posY = rangeY.fit(posY);

        if (currentBuilding.job != null && currentBuilding.job.getRange() > 0) {
            showBuildingRange(batch,
                    posX + currentBuilding.entrancePosition.x * World.TILE_SIZE,
                    posY + currentBuilding.entrancePosition.y * World.TILE_SIZE,
                    currentBuilding.job.getRange());
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

    public static void demolish() {
        if (!isDemolishing || isInBuildingMode)
            throw new IllegalStateException("Not in demolishing mode");

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            for (Building building : World.getBuildings()) {
                if (building.isMouseOver()) {
                    World.removeBuilding(building);
                    if (!InputManager.isKeyDown(Input.Keys.CONTROL_LEFT)) isDemolishing = false;
                    break;
                }
            }
        }
    }

    public static void setDemolishingMode(boolean isDemolishing) {
        Buildings.isDemolishing = isDemolishing;
        isInBuildingMode = false;
    }

    public static void toBuildingMode(Type building) {
        currentBuilding = building;
        rangeX = Range.between(0, World.getWidth() - currentBuilding.getTexture().getRegionWidth());
        rangeY = Range.between(0, World.getHeight() - currentBuilding.getTexture().getRegionHeight());
        isDemolishing = false;
        isInBuildingMode = true;
    }

    public static void turnOffBuildingMode() {
        isInBuildingMode = false;
    }

    public static boolean isInBuildingMode() {
        return isInBuildingMode;
    }

    public static boolean isDemolishing() {
        return isDemolishing;
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
                Textures.get(Textures.Tile.DEFAULT),  //todo temp texture
                posX,
                posY,
                range * World.TILE_SIZE);
        batch.setColor(UI.DEFAULT_COLOR);
    }
}
