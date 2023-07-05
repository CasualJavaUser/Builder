package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.ui.TileRect;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Predicate;

public class Tiles {
    private static boolean isInPathMode = false;
    private static boolean isInFieldMode = false;
    //private static int originX = -1, originY = -1;
    private static Vector2i origin = null;
    private static BoxCollider buildingCollider = null;
    private static boolean isRanch;
    private static int minSize = 0, maxSize = 0;
    private static Tile pathTile = null;
    //private static TilingMode mode;

    private static final Predicate<Vector2i> isFarmable = (vector) -> World.getTile(vector) == Tile.GRASS || World.getTile(vector) == Tile.DIRT;

    /*public enum TilingMode {
        SINGLE,
        PATH,
        AREA
    }*/

    /*public static TilingMode getMode() {
        return mode;
    }*/

    public static BoxCollider getBuildingCollider() {
        return buildingCollider;
    }

    public static boolean isInPathMode() {
        return isInPathMode;
    }

    public static boolean isInFieldMode() {
        return isInFieldMode;
    }

    /*public static void toTilingMode(TilingMode mode) {
        Tiles.mode = mode;
        isInTilingMode = true;
        isInFieldMode = false;
    }*/

    public static void toPathMode(Tile pathTile) {
        isInPathMode = true;
        isInFieldMode = false;
        Tiles.pathTile = pathTile;
    }

    public static void turnOffPathMode() {
        origin = null;
        isInPathMode = false;
    }

    public static void toFieldMode(BoxCollider buildingCollider, boolean isRanch, int minSize, int maxSize) {
        if (minSize >= maxSize) throw new IllegalArgumentException();

        Tiles.buildingCollider = buildingCollider;
        Tiles.isRanch = isRanch;
        Tiles.minSize = minSize;
        Tiles.maxSize = maxSize;
        //originX = buildingCollider.getGridPosition().x;
        //originY = buildingCollider.getGridPosition().y;
        origin = new Vector2i(
                buildingCollider.getGridPosition().x,
                buildingCollider.getGridPosition().y
        );
        isInFieldMode = true;
        isInPathMode = false;
    }

    public static void turnOffFieldMode() {
        isInFieldMode = false;
    }

    public static void handlePathMode(SpriteBatch batch) {
        if (!isInPathMode || isInFieldMode)
            throw new IllegalStateException("Not in path mode");

        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int) (mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int) (mousePos.y / World.TILE_SIZE);

        if (origin == null) {
            setBatchColorForTile(batch, mouseGridX, mouseGridY);
            batch.draw(Textures.get(Textures.Tile.DEFAULT), mouseGridX * World.TILE_SIZE, mouseGridY * World.TILE_SIZE);
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                origin = new Vector2i(mouseGridX, mouseGridY);
            }
        }
        else {
            int width = Math.abs(mouseGridX - origin.x);
            int height = Math.abs(mouseGridY - origin.y);
            int temp;
            boolean buildPath = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

            if (width >= height) {
                temp = Math.min(origin.x, mouseGridX);
                for (int i = 0; i <= width; i++) {
                    if (!buildPath) {
                        setBatchColorForTile(batch, temp + i, origin.y);
                        batch.draw(Textures.get(Textures.Tile.DEFAULT), (temp + i) * World.TILE_SIZE, origin.y * World.TILE_SIZE);
                    }
                    else if (World.isBuildable(temp + i, origin.y)) {
                        boolean[] neighbours = new boolean[4];
                        if (i > 0) neighbours[3] = true;
                        if (i < width) neighbours[1] = true;
                        if (World.getTile(temp + i, origin.y + 1).equals(pathTile)) neighbours[0] = true;
                        if (World.getTile(temp + i, origin.y - 1).equals(pathTile)) neighbours[2] = true;
                        World.setTile(temp + i, origin.y, pathTile, getTextureForTile(neighbours, pathTile));
                    }
                }
            } else {
                temp = Math.min(origin.y, mouseGridY);
                for (int i = 0; i <= height; i++) {
                    if (!buildPath) {
                        setBatchColorForTile(batch, origin.x, temp + i);
                        batch.draw(Textures.get(Textures.Tile.DEFAULT), origin.x * World.TILE_SIZE, (temp + i) * World.TILE_SIZE);
                    }
                    else if (World.isBuildable(origin.x, temp + i)) {
                        boolean[] neighbours = new boolean[4];
                        if (i > 0) neighbours[2] = true;
                        if (i < height) neighbours[0] = true;
                        if (World.getTile(origin.x + 1, temp + i).equals(pathTile)) neighbours[1] = true;
                        if (World.getTile(origin.x - 1, temp + i).equals(pathTile)) neighbours[3] = true;
                        World.setTile(origin.x, temp + i, pathTile, getTextureForTile(neighbours, pathTile));
                    }
                }
            }
            if (buildPath) {
                turnOffPathMode();
            }
        }

        batch.setColor(UI.DEFAULT_COLOR);
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
        }
        else if (count == 3) {
            if (!neighbours[0])
                return tile.textures[3];
            else if (!neighbours[1])
                return tile.textures[4];
            else if (!neighbours[2])
                return tile.textures[1];
            else if (!neighbours[3])
                return tile.textures[2];
        }
        else if (count == 2) {
            if (neighbours[0]) {
                if (neighbours[1])
                    return tile.textures[5];
                else if (neighbours[2])
                    return tile.textures[10];
                else if (neighbours[3])
                    return tile.textures[8];
            }
            else if (neighbours[2]) {
                if (neighbours[1])
                    return tile.textures[6];
                else if (neighbours[3])
                    return tile.textures[7];
            }
            else
                return tile.textures[9];
        }
        else if (count == 1) {
            if (neighbours[0] || neighbours[2])
                return tile.textures[10];
            else
                return tile.textures[9];
        }
        return tile.textures[0];
    }

    private static void setBatchColorForTile(SpriteBatch batch, int x, int y) {
        if (World.isBuildable(x, y))
            batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
        else
            batch.setColor(UI.SEMI_TRANSPARENT_RED);
    }

    /**
     * @return the collider of the field only after it has been placed. Otherwise, null.
     */
    public static BoxCollider handleFieldMode(SpriteBatch batch) {
        if (!isInFieldMode || isInPathMode)
            throw new IllegalStateException("Not in field mode");

        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int) (mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int) (mousePos.y / World.TILE_SIZE);
        int fieldWidth = Math.abs(mouseGridX - origin.x) + 1;
        int fieldHeight = Math.abs(mouseGridY - origin.y) + 1;

        batch.setColor(UI.SEMI_TRANSPARENT);

        if (mouseGridX >= buildingCollider.getGridPosition().x + buildingCollider.getWidth())
            origin.x = buildingCollider.getGridPosition().x;
        else if (mouseGridX < buildingCollider.getGridPosition().x)
            origin.x = buildingCollider.getGridPosition().x + buildingCollider.getWidth() - 1;

        if (mouseGridY < buildingCollider.getGridPosition().y)
            origin.y = buildingCollider.getGridPosition().y - 1;
        else if (mouseGridY >= buildingCollider.getGridPosition().y + buildingCollider.getHeight())
            origin.y = buildingCollider.getGridPosition().y + buildingCollider.getHeight();

        boolean withinLimits = fieldWidth >= minSize && fieldHeight >= minSize && fieldWidth < maxSize && fieldHeight < maxSize;
        if (withinLimits) batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
        else batch.setColor(UI.SEMI_TRANSPARENT_RED);
        TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), isFarmable, origin.x, origin.y, mouseGridX, mouseGridY);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && withinLimits) {
            if (mouseGridX < origin.x) origin.x -= fieldWidth - 1;
            if (mouseGridY < origin.y) origin.y -= fieldHeight - 1;

            Vector2i pos = new Vector2i();

            //if plantation then change tiles
            if (!isRanch) {
                for (int y = 0; y < fieldHeight; y++) {
                    for (int x = 0; x < fieldWidth; x++) {
                        pos.set(origin.x + x, origin.y + y);
                        if (isFarmable.test(pos)) World.setTile(pos, Tile.DIRT);
                    }
                }
            }
            //if not then build fence
            else {
                //corners
                pos.set(origin.x, origin.y);
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
            return new BoxCollider(origin.x, origin.y, fieldWidth, fieldHeight);
        }
        return null;
    }

    public static void createFence(BoxCollider fieldCollider) {
        Vector2i origin = fieldCollider.getGridPosition();
        Vector2i pos = new Vector2i();
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
}
