package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.ui.TileRect;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Predicate;

public class Tiles {
    private static boolean isInTilingMode = false;
    private static int originX = -1, originY = -1;
    private static ConstructionSite constructionSite = null;
    private static int minSize = 0, maxSize = 0;
    private static TilingMode mode;

    private static final Predicate<Vector2i> isFarmable = (vector) -> World.getTile(vector) == Tile.GRASS || World.getTile(vector) == Tile.DIRT;

    public enum TilingMode {
        SINGLE,
        PATH,
        AREA,
        FARM
    }

    public static TilingMode getMode() {
        return mode;
    }

    public static boolean isInTilingMode() {
        return isInTilingMode;
    }

    public static void toTilingMode(TilingMode mode) {
        if (mode == TilingMode.FARM) throw new IllegalArgumentException();
        Tiles.mode = mode;
        isInTilingMode = true;
    }

    public static void toTilingMode(ConstructionSite constructionSite, int minSize, int maxSize) {
        Tiles.constructionSite = constructionSite;
        Tiles.minSize = minSize;
        Tiles.maxSize = maxSize;
        originX = constructionSite.getGridPosition().x;
        originY = constructionSite.getGridPosition().y;
        mode = TilingMode.FARM;
        isInTilingMode = true;
    }

    public static void turnOffTilingMode() {
        originX = -1;
        originY = -1;
        isInTilingMode = false;
    }

    public static void handleTilingMode(SpriteBatch batch) {
        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int) (mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int) (mousePos.y / World.TILE_SIZE);
        int fieldWidth = Math.abs(mouseGridX - originX) + 1;
        int fieldHeight = Math.abs(mouseGridY - originY) + 1;

        batch.setColor(UI.SEMI_TRANSPARENT);

        switch (mode) {
            case FARM:
                if (mouseGridX >= constructionSite.getGridPosition().x + constructionSite.getCollider().getWidth())
                    originX = constructionSite.getGridPosition().x;
                else if (mouseGridX < constructionSite.getGridPosition().x)
                    originX = constructionSite.getGridPosition().x + constructionSite.getCollider().getWidth() - 1;

                if (mouseGridY < constructionSite.getGridPosition().y)
                    originY = constructionSite.getGridPosition().y - 1;
                else if (mouseGridY >= constructionSite.getGridPosition().y + constructionSite.getCollider().getHeight())
                    originY = constructionSite.getGridPosition().y + constructionSite.getCollider().getHeight();

                boolean withinLimits = fieldWidth >= minSize && fieldHeight >= minSize && fieldWidth < maxSize && fieldHeight < maxSize;
                if (withinLimits) batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
                else batch.setColor(UI.SEMI_TRANSPARENT_RED);
                TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), isFarmable, originX, originY, mouseGridX, mouseGridY);

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && withinLimits) {
                    if (mouseGridX < originX) originX -= fieldWidth - 1;
                    if (mouseGridY < originY) originY -= fieldHeight - 1;

                    constructionSite.setFieldCollider(new BoxCollider(originX, originY, fieldWidth, fieldHeight));

                    Vector2i pos = new Vector2i();

                    //if plantation then change tiles
                    if (constructionSite.getType().crop != null) {
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
                            World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_BL, pos.clone()));
                        pos.set(originX, originY + fieldHeight - 1);
                        if (isFarmable.test(pos))
                            World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_TL, pos.clone()));
                        pos.set(originX + fieldWidth - 1, originY + fieldHeight - 1);
                        if (isFarmable.test(pos))
                            World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_TR, pos.clone()));
                        pos.set(originX + fieldWidth - 1, originY);
                        if (isFarmable.test(pos))
                            World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_BR, pos.clone()));
                        //top bottom
                        for (int x = 1; x < fieldWidth - 1; x++) {
                            pos.set(originX + x, originY + fieldHeight - 1);
                            if (isFarmable.test(pos))
                                World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_T, pos.clone()));
                            pos.set(originX + x, originY);
                            if (isFarmable.test(pos))
                                World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_B, pos.clone()));
                        }
                        //sides
                        for (int y = 1; y < fieldHeight - 1; y++) {
                            pos.set(originX, originY + y);
                            if (isFarmable.test(pos))
                                World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_L, pos.clone()));
                            pos.set(originX + fieldWidth - 1, originY + y);
                            if (isFarmable.test(pos))
                                World.getGameObjects().add(new GameObject(Textures.Environment.FENCE_R, pos.clone()));
                        }
                    }

                    turnOffTilingMode();
                }
                break;
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
}
