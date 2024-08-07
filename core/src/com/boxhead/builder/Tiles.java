package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Range;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Predicate;

import static com.boxhead.builder.Textures.Tile.BRIDGE;
import static com.boxhead.builder.Textures.Tile.DEFAULT;
import static com.boxhead.builder.ui.UI.*;
import static com.boxhead.builder.utils.Vector2i.*;

public class Tiles {
    private static Vector2i origin = null;
    private static BoxCollider buildingCollider = null;
    private static boolean isRanch;
    private static Range<Integer> fieldSizeRange;
    private static Tile pathTile = null;
    private static BoxCollider fieldCollider = null;

    private static Mode currentMode = null;

    public enum Mode {
        PATH,
        FIELD,
        BRIDGE,
        REMOVE_PATH
    }

    private static final Predicate<Vector2i> isFarmable = (vector) -> World.getTile(vector) == Tile.GRASS || World.getTile(vector) == Tile.FARMLAND;

    private static final Recipe pathCost = new Recipe(Pair.of(Resource.STONE, 1));
    private static final Recipe bridgeCost = new Recipe(Pair.of(Resource.STONE, 2));

    public static void toPathMode(Tile pathTile) {
        currentMode = Mode.PATH;
        Tiles.pathTile = pathTile;
        UI.pushOnEscapeAction(Tiles::turnOff, () -> getCurrentMode() == Mode.PATH);
    }

    public static void toFieldMode(BoxCollider buildingCollider, boolean isRanch, Range<Integer> fieldSizeRange) {
        Tiles.buildingCollider = buildingCollider;
        Tiles.isRanch = isRanch;
        Tiles.fieldSizeRange = fieldSizeRange;
        origin = buildingCollider.getGridPosition().clone();
        currentMode = Mode.FIELD;
        UI.pushOnEscapeAction(Tiles::turnOff, () -> getCurrentMode() == Mode.FIELD);
    }

    public static void toBridgeMode() {
        currentMode = Mode.BRIDGE;
        UI.pushOnEscapeAction(Tiles::turnOff, () -> getCurrentMode() == Mode.BRIDGE);
    }

    public static void toRemovingMode() {
        currentMode = Mode.REMOVE_PATH;
        UI.pushOnEscapeAction(Tiles::turnOff, () -> getCurrentMode() == Mode.REMOVE_PATH);
    }

    public static void handlePathMode(SpriteBatch batch) {
        if (currentMode != Mode.PATH)
            throw new IllegalStateException("Not in path mode");

        Vector2i mouseGrid = GameScreen.getMouseGridPosition();

        if (origin == null) {
            setBatchColorForTile(batch, mouseGrid);
            drawTile(batch, DEFAULT, mouseGrid);
            if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
                origin = mouseGrid;
            }
        } else {
            Vector2i[] line = straightLine(origin, mouseGrid);
            boolean buildPath = InputManager.isButtonPressed(InputManager.LEFT_MOUSE);
            Recipe totalCost = new Recipe();

            for (int i = 0; i < line.length - 1; i++) {
                Tile tile = World.getTile(line[i]);

                if (World.isBuildable(line[i]) && tile != Tile.PATH) {
                    totalCost.add(pathCost);
                }
            }

            if (Resource.canAfford(totalCost)) {
                if (buildPath) {
                    for (int i = 0; i < line.length - 1; i++) {
                        Vector2i tile = line[i];

                        boolean[] neighbours = new boolean[4]; //top right bottom left
                        if (tile.plus(UP).equals(line[i + 1]) || World.getTile(tile.plus(UP)) == pathTile || World.getTile(tile.plus(UP)) == Tile.BRIDGE)
                            neighbours[0] = true;
                        if (tile.plus(RIGHT).equals(line[i + 1]) || World.getTile(tile.plus(RIGHT)) == pathTile || World.getTile(tile.plus(RIGHT)) == Tile.BRIDGE)
                            neighbours[1] = true;
                        if (tile.plus(DOWN).equals(line[i + 1]) || World.getTile(tile.plus(DOWN)) == pathTile || World.getTile(tile.plus(DOWN)) == Tile.BRIDGE)
                            neighbours[2] = true;
                        if (tile.plus(LEFT).equals(line[i + 1]) || World.getTile(tile.plus(LEFT)) == pathTile || World.getTile(tile.plus(LEFT)) == Tile.BRIDGE)
                            neighbours[3] = true;

                        if (World.isBuildable(tile)) {
                            World.setTile(tile, pathTile, getTextureForTile(neighbours, pathTile));
                        }
                    }
                    origin = null;
                    Resource.takeFromStorage(totalCost);

                    BoxCollider lineArea;
                    if (line[0].x == line[line.length - 2].x) {
                        lineArea = new BoxCollider(line[0], 1, line.length - 1);
                    } else {
                        lineArea = new BoxCollider(line[0], line.length - 1, 1);
                    }
                    World.removeFieldWorks(lineArea);
                } else {
                    for (int i = 0; i < line.length - 1; i++) {
                        setBatchColorForTile(batch, line[i]);
                        drawTile(batch, DEFAULT, line[i]);
                    }
                }
            } else {
                batch.setColor(SEMI_TRANSPARENT_RED);
                for (int i = 0; i < line.length - 1; i++) {
                    drawTile(batch, DEFAULT, line[i]);
                }
            }

            batch.setColor(DEFAULT_COLOR);
        }
    }

    /**
     * @return the collider of the field only after it has been placed. Otherwise, null.
     */
    public static BoxCollider handleFieldMode(SpriteBatch batch) {
        if (currentMode != Mode.FIELD)
            throw new IllegalStateException("Not in field mode");

        Vector2i mouseGrid = GameScreen.getMouseGridPosition();
        Vector2i fieldDimensions = mouseGrid.minus(origin).absolutise().add(1, 1);

        if (mouseGrid.x >= buildingCollider.getGridPosition().x + buildingCollider.getWidth())
            origin.x = buildingCollider.getGridPosition().x;
        else if (mouseGrid.x < buildingCollider.getGridPosition().x)
            origin.x = buildingCollider.getGridPosition().x + buildingCollider.getWidth() - 1;

        if (mouseGrid.y < buildingCollider.getGridPosition().y)
            origin.y = buildingCollider.getGridPosition().y - 1;
        else if (mouseGrid.y >= buildingCollider.getGridPosition().y + buildingCollider.getHeight())
            origin.y = buildingCollider.getGridPosition().y + buildingCollider.getHeight();

        Vector2i lowerLeftCorner = origin.clone();
        if (mouseGrid.x < origin.x) lowerLeftCorner.x -= fieldDimensions.x - 1;
        if (mouseGrid.y < origin.y) lowerLeftCorner.y -= fieldDimensions.y - 1;
        BoxCollider field = new BoxCollider(lowerLeftCorner, fieldDimensions);

        boolean withinLimits = fieldSizeRange.contains(fieldDimensions.x) && fieldSizeRange.contains(fieldDimensions.y);
        if (withinLimits) batch.setColor(SEMI_TRANSPARENT_GREEN);
        else batch.setColor(SEMI_TRANSPARENT_RED);
        field.draw(batch, DEFAULT, isFarmable);

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE) && withinLimits) {
            //if plantation then change tiles
            if (!isRanch) {
                for (Vector2i tile : field) {
                    if (isFarmable.test(tile)) World.setTile(tile, Tile.FARMLAND);
                }
            } else {
                createFence(field);
            }
            origin = null;
            return field;
        }
        return null;
    }

    public static void handleBridgeMode(SpriteBatch batch) {
        if (currentMode != Mode.BRIDGE)
            throw new IllegalStateException("Not in path mode");

        Vector2i mouseGrid = GameScreen.getMouseGridPosition();

        if (origin == null) {
            batch.setColor(World.getTile(mouseGrid) == Tile.WATER ? SEMI_TRANSPARENT_GREEN : SEMI_TRANSPARENT_RED);
            drawTile(batch, DEFAULT, mouseGrid);
            if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
                origin = mouseGrid;
            }
        } else {
            Vector2i[] line = horizontalLine(origin, mouseGrid);
            boolean buildBridge = InputManager.isButtonPressed(InputManager.LEFT_MOUSE);
            Recipe totalCost = new Recipe();

            for (int i = 0; i < line.length - 1; i++) {
                if (World.getTile(line[i]) == Tile.WATER) {
                    totalCost.add(bridgeCost);
                }
            }

            if (Resource.canAfford(totalCost)) {
                if (buildBridge) {
                    for (int i = 0; i < line.length - 1; i++) {
                        Vector2i tile = line[i];

                        if (World.getTile(line[i]) == Tile.WATER) {
                            try {
                                World.setTile(tile, Tile.BRIDGE, Textures.Tile.valueOf(World.getTileTexture(line[i]).name() + "_BRIDGE"));
                            } catch (IllegalArgumentException e) {
                                World.setTile(tile, Tile.BRIDGE, Tile.BRIDGE.textures[0]);
                            }
                        }
                    }
                    origin = null;
                    Resource.takeFromStorage(totalCost);

                    BoxCollider lineArea;
                    if (line[0].x == line[line.length - 2].x) {
                        lineArea = new BoxCollider(line[0], 1, line.length - 1);
                    } else {
                        lineArea = new BoxCollider(line[0], line.length - 1, 1);
                    }
                    World.removeFieldWorks(lineArea);
                } else {
                    for (int i = 0; i < line.length - 1; i++) {
                        batch.setColor(World.getTile(line[i]) == Tile.WATER ? SEMI_TRANSPARENT_GREEN : SEMI_TRANSPARENT_RED);
                        drawTile(batch, DEFAULT, line[i]);
                    }
                }
            } else {
                batch.setColor(SEMI_TRANSPARENT_RED);
                for (int i = 0; i < line.length - 1; i++) {
                    drawTile(batch, DEFAULT, line[i]);
                }
            }

            batch.setColor(DEFAULT_COLOR);
        }
    }

    public static void handleRemovingMode(SpriteBatch batch) {
        if (currentMode != Mode.REMOVE_PATH)
            throw new IllegalStateException("Not in tile removing mode");

        Vector2i mouseGrid = GameScreen.getMouseGridPosition();

        if (origin == null) {
            batch.setColor(SEMI_TRANSPARENT_GREEN);
            drawTile(batch, DEFAULT, mouseGrid);
            if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
                origin = mouseGrid;
            }
        } else {
            Vector2i[] line = straightLine(origin, mouseGrid);
            boolean removeTiles = InputManager.isButtonPressed(InputManager.LEFT_MOUSE);


            if (removeTiles) {
                for (int i = 0; i < line.length - 1; i++) {
                    if (World.getTile(line[i]) == Tile.PATH) {
                        World.setTile(line[i], Tile.GRASS);
                    }
                    else if (World.getTile(line[i]) == Tile.BRIDGE) {
                        Textures.Tile texture = World.getTileTexture(line[i]);
                        if (texture == BRIDGE)
                            World.setTile(line[i], Tile.WATER);
                        else
                            World.setTile(line[i], Tile.WATER, Textures.Tile.valueOf(texture.name().substring(0, texture.name().lastIndexOf('_'))));
                    }
                }
                origin = null;
            } else {
                for (int i = 0; i < line.length - 1; i++) {
                    batch.setColor(SEMI_TRANSPARENT_GREEN);
                    drawTile(batch, DEFAULT, line[i]);
                }
            }

            batch.setColor(DEFAULT_COLOR);
        }
    }

    /**
     * @return an either horizontal or vertical straight line anchored at the origin such that its other end is as close as possible to the target.
     */
    private static Vector2i[] straightLine(Vector2i origin, Vector2i target) {
        Vector2i offset = target.minus(origin);
        Vector2i absoluteOffset = offset.absolute().add(1, 1);
        int length = Math.max(absoluteOffset.x, absoluteOffset.y);
        Vector2i[] line = new Vector2i[length + 1]; //the array is null terminated so that line[i + 1] in loops doesn't throw

        Vector2i unitVector; //direction from origin to target
        if (absoluteOffset.x == length) {   //horizontal
            unitVector = RIGHT.clone();
            if (offset.x < 0) unitVector.x = -1;
        } else {                            //vertical
            unitVector = UP.clone();
            if (offset.y < 0) unitVector.y = -1;
        }

        Vector2i temp = origin.clone();
        for (int i = 0; i < length; i++) {
            line[i] = temp.clone();
            temp.add(unitVector);
        }
        line[length] = null;
        return line;
    }

    private static Vector2i[] horizontalLine(Vector2i origin, Vector2i target) {
        int offset = target.x - origin.x;
        int absoluteOffset = Math.abs(offset) + 1;
        Vector2i[] line = new Vector2i[absoluteOffset + 1]; //the array is null terminated so that line[i + 1] in loops doesn't throw
        int dir = offset < 0 ? -1 : 1;

        Vector2i temp = origin.clone();
        for (int i = 0; i < absoluteOffset; i++) {
            line[i] = temp.clone();
            temp.add(dir, 0);
        }
        line[absoluteOffset] = null;
        return line;
    }

    /**
     * @return texture of the tile based on neighbouring tiles
     */
    private static Textures.Tile getTextureForTile(boolean[] neighbours, Tile tile) {
        int count = 0;
        for (boolean n : neighbours) {
            if (n) count++;
        }

        if (count == 4) {
            return tile.textures[0];
        } else if (count == 3) {
            if (!neighbours[0])
                return tile.textures[3];
            else if (!neighbours[1])
                return tile.textures[4];
            else if (!neighbours[2])
                return tile.textures[1];
            else if (!neighbours[3])
                return tile.textures[2];
        } else if (count == 2) {
            if (neighbours[0]) {
                if (neighbours[1])
                    return tile.textures[5];
                else if (neighbours[2])
                    return tile.textures[10];
                else if (neighbours[3])
                    return tile.textures[8];
            } else if (neighbours[2]) {
                if (neighbours[1])
                    return tile.textures[6];
                else if (neighbours[3])
                    return tile.textures[7];
            } else
                return tile.textures[9];
        } else if (count == 1) {
            if (neighbours[0] || neighbours[2])
                return tile.textures[10];
            else
                return tile.textures[9];
        }
        return tile.textures[0];
    }

    private static void setBatchColorForTile(SpriteBatch batch, Vector2i gridPosition) {
        if (World.getTile(gridPosition) == Tile.PATH)
            batch.setColor(SEMI_TRANSPARENT_YELLOW);
        else if (World.isBuildable(gridPosition))
            batch.setColor(SEMI_TRANSPARENT_GREEN);
        else
            batch.setColor(SEMI_TRANSPARENT_RED);
    }

    public static void createFence(BoxCollider fieldCollider) {
        Vector2i origin = fieldCollider.getGridPosition();
        Vector2i pos = origin.clone();
        int fieldWidth = fieldCollider.getWidth();
        int fieldHeight = fieldCollider.getHeight();

        //corners
        if (isFarmable.test(pos))
            World.addGameObject(new GameObject(Textures.Environment.FENCE_BL, pos.clone()));
        pos.set(origin.x, origin.y + fieldHeight - 1);
        if (isFarmable.test(pos))
            World.addGameObject(new GameObject(Textures.Environment.FENCE_TL, pos.clone()));
        pos.set(origin.x + fieldWidth - 1, origin.y + fieldHeight - 1);
        if (isFarmable.test(pos))
            World.addGameObject(new GameObject(Textures.Environment.FENCE_TR, pos.clone()));
        pos.set(origin.x + fieldWidth - 1, origin.y);
        if (isFarmable.test(pos))
            World.addGameObject(new GameObject(Textures.Environment.FENCE_BR, pos.clone()));
        //top bottom
        for (int x = 1; x < fieldWidth - 1; x++) {
            pos.set(origin.x + x, origin.y + fieldHeight - 1);
            if (isFarmable.test(pos))
                World.addGameObject(new GameObject(Textures.Environment.FENCE_T, pos.clone()));
            pos.set(origin.x + x, origin.y);
            if (isFarmable.test(pos))
                World.addGameObject(new GameObject(Textures.Environment.FENCE_B, pos.clone()));
        }
        //sides
        for (int y = 1; y < fieldHeight - 1; y++) {
            pos.set(origin.x, origin.y + y);
            if (isFarmable.test(pos))
                World.addGameObject(new GameObject(Textures.Environment.FENCE_L, pos.clone()));
            pos.set(origin.x + fieldWidth - 1, origin.y + y);
            if (isFarmable.test(pos))
                World.addGameObject(new GameObject(Textures.Environment.FENCE_R, pos.clone()));
        }
    }

    public static void turnOff() {
        currentMode = null;
        origin = null;
    }

    public static Mode getCurrentMode() {
        return currentMode;
    }

    public static BoxCollider getBuildingCollider() {
        return buildingCollider;
    }

    public static void drawTile(SpriteBatch batch, Textures.Tile texture, Vector2i gridPosition) {
        drawTile(batch, Textures.get(texture), gridPosition);
    }

    public static void drawTile(SpriteBatch batch, TextureRegion texture, Vector2i gridPosition) {
        batch.draw(texture, gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }

    public static void handleCurrentMode(SpriteBatch batch) {
        switch (currentMode) {
            case PATH -> handlePathMode(batch);
            case FIELD -> handleFieldMode(batch);
            case BRIDGE -> handleBridgeMode(batch);
            case REMOVE_PATH -> handleRemovingMode(batch);
        }
    }
}
