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
    private static boolean isInTilingMode = false;
    private static boolean isInFieldMode = false;
    private static int originX = -1, originY = -1;
    private static BoxCollider buildingCollider = null;
    private static boolean isRanch;
    private static int minSize = 0, maxSize = 0;
    private static TilingMode mode;

    private static final Predicate<Vector2i> isFarmable = (vector) -> World.getTile(vector) == Tile.GRASS || World.getTile(vector) == Tile.DIRT;

    public enum TilingMode {
        SINGLE,
        PATH,
        AREA
    }

    public static TilingMode getMode() {
        return mode;
    }

    public static BoxCollider getBuildingCollider() {
        return buildingCollider;
    }

    public static boolean isInTilingMode() {
        return isInTilingMode;
    }

    public static boolean isInFieldMode() {
        return isInFieldMode;
    }

    public static void toTilingMode(TilingMode mode) {
        Tiles.mode = mode;
        isInTilingMode = true;
        isInFieldMode = false;
    }

    public static void turnOffTilingMode() {
        originX = -1;
        originY = -1;
        isInTilingMode = false;
    }

    public static void toFieldMode(BoxCollider buildingCollider, boolean isRanch, int minSize, int maxSize) {
        if (minSize >= maxSize) throw new IllegalArgumentException();

        Tiles.buildingCollider = buildingCollider;
        Tiles.isRanch = isRanch;
        Tiles.minSize = minSize;
        Tiles.maxSize = maxSize;
        originX = buildingCollider.getGridPosition().x;
        originY = buildingCollider.getGridPosition().y;
        isInFieldMode = true;
        isInTilingMode = false;
    }

    public static void turnOffFieldMode() {
        isInFieldMode = false;
    }

    public static void handleTilingMode(SpriteBatch batch) {  //TODO tiling mode in progress
        if (!isInTilingMode || isInFieldMode)
            throw new IllegalStateException("Not in tiling mode");

        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int) (mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int) (mousePos.y / World.TILE_SIZE);
        int fieldWidth = Math.abs(mouseGridX - originX) + 1;
        int fieldHeight = Math.abs(mouseGridY - originY) + 1;

        batch.setColor(UI.SEMI_TRANSPARENT);

        switch (mode) {
            case AREA:
                if (originX >= 0 && originY >= 0) {
                    TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), originX, originY, mouseGridX, mouseGridY);
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
                } else {
                    batch.draw(Textures.get(Textures.Tile.DEFAULT), mousePos.x, mousePos.y);
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        originX = mouseGridX;
                        originY = mouseGridY;
                    }
                }
                break;
            case PATH:
                if (originX >= 0 && originY >= 0) {
                    if (fieldWidth >= fieldHeight) {
                        int temp = Math.min(originX, mouseGridX);
                        for (int i = 0; i <= fieldWidth; i++) {
                            batch.draw(Textures.get(Textures.Tile.DEFAULT), (temp + i) * World.TILE_SIZE, originY * World.TILE_SIZE);
                        }
                    } else {
                        int temp = Math.min(originY, mouseGridY);
                        for (int i = 0; i <= fieldHeight; i++) {
                            batch.draw(Textures.get(Textures.Tile.DEFAULT), originX * World.TILE_SIZE, (temp + i) * World.TILE_SIZE);
                        }
                    }
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
                } else {
                    batch.draw(Textures.get(Textures.Tile.DEFAULT), mousePos.x, mousePos.y);
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        originX = mouseGridX;
                        originY = mouseGridY;
                    }
                }
                break;
            case SINGLE:
                batch.draw(Textures.get(Textures.Tile.DEFAULT), mousePos.x, mousePos.y);
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
        }

        batch.setColor(UI.DEFAULT_COLOR);
    }

    /**
     * @return the collider of the field only after it has been placed. Otherwise, null.
     */
    public static BoxCollider handleFieldMode(SpriteBatch batch) {
        if (!isInFieldMode || isInTilingMode)
            throw new IllegalStateException("Not in field mode");

        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int) (mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int) (mousePos.y / World.TILE_SIZE);
        int fieldWidth = Math.abs(mouseGridX - originX) + 1;
        int fieldHeight = Math.abs(mouseGridY - originY) + 1;

        batch.setColor(UI.SEMI_TRANSPARENT);

        if (mouseGridX >= buildingCollider.getGridPosition().x + buildingCollider.getWidth())
            originX = buildingCollider.getGridPosition().x;
        else if (mouseGridX < buildingCollider.getGridPosition().x)
            originX = buildingCollider.getGridPosition().x + buildingCollider.getWidth() - 1;

        if (mouseGridY < buildingCollider.getGridPosition().y)
            originY = buildingCollider.getGridPosition().y - 1;
        else if (mouseGridY >= buildingCollider.getGridPosition().y + buildingCollider.getHeight())
            originY = buildingCollider.getGridPosition().y + buildingCollider.getHeight();

        boolean withinLimits = fieldWidth >= minSize && fieldHeight >= minSize && fieldWidth < maxSize && fieldHeight < maxSize;
        if (withinLimits) batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
        else batch.setColor(UI.SEMI_TRANSPARENT_RED);
        TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), isFarmable, originX, originY, mouseGridX, mouseGridY);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && withinLimits) {
            if (mouseGridX < originX) originX -= fieldWidth - 1;
            if (mouseGridY < originY) originY -= fieldHeight - 1;

            Vector2i pos = new Vector2i();

            //if plantation then change tiles
            if (!isRanch) {
                for (int y = 0; y < fieldHeight; y++) {
                    for (int x = 0; x < fieldWidth; x++) {
                        pos.set(originX + x, originY + y);
                        if (isFarmable.test(pos)) World.setTile(pos, Tile.DIRT);
                    }
                }
            }
            //if not then build fence
            else {
                //corners
                pos.set(originX, originY);
                if (isFarmable.test(pos))
                    World.addGameObject(new GameObject(Textures.Environment.FENCE_BL, pos.clone()));
                pos.set(originX, originY + fieldHeight - 1);
                if (isFarmable.test(pos))
                    World.addGameObject(new GameObject(Textures.Environment.FENCE_TL, pos.clone()));
                pos.set(originX + fieldWidth - 1, originY + fieldHeight - 1);
                if (isFarmable.test(pos))
                    World.addGameObject(new GameObject(Textures.Environment.FENCE_TR, pos.clone()));
                pos.set(originX + fieldWidth - 1, originY);
                if (isFarmable.test(pos))
                    World.addGameObject(new GameObject(Textures.Environment.FENCE_BR, pos.clone()));
                //top bottom
                for (int x = 1; x < fieldWidth - 1; x++) {
                    pos.set(originX + x, originY + fieldHeight - 1);
                    if (isFarmable.test(pos))
                        World.addGameObject(new GameObject(Textures.Environment.FENCE_T, pos.clone()));
                    pos.set(originX + x, originY);
                    if (isFarmable.test(pos))
                        World.addGameObject(new GameObject(Textures.Environment.FENCE_B, pos.clone()));
                }
                //sides
                for (int y = 1; y < fieldHeight - 1; y++) {
                    pos.set(originX, originY + y);
                    if (isFarmable.test(pos))
                        World.addGameObject(new GameObject(Textures.Environment.FENCE_L, pos.clone()));
                    pos.set(originX + fieldWidth - 1, originY + y);
                    if (isFarmable.test(pos))
                        World.addGameObject(new GameObject(Textures.Environment.FENCE_R, pos.clone()));
                }
            }
            return new BoxCollider(originX, originY, fieldWidth, fieldHeight);
        }
        return null;
    }
}
