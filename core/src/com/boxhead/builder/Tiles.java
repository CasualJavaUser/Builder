package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.game_objects.ConstructionSite;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.ui.TileRect;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class Tiles {
    private static boolean isInTilingMode = false;
    private static int originX = -1, originY = -1;
    private static ConstructionSite constructionSite = null;
    private static int minX = 0, minY = 0;
    private static TilingMode mode;

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

    public static void toTilingMode(ConstructionSite constructionSite, int minX, int minY) {
        Tiles.constructionSite = constructionSite;
        Tiles.minX = minX;
        Tiles.minY = minY;
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
        Vector3 mousePos = GameScreen.getMouseWorldPosition();
        int mouseGridX = (int)(mousePos.x / World.TILE_SIZE);
        int mouseGridY = (int)(mousePos.y / World.TILE_SIZE);
        int width =  Math.abs(mouseGridX - originX);
        int height = Math.abs(mouseGridY - originY);

        batch.setColor(UI.SEMI_TRANSPARENT);

        if(mode == TilingMode.FARM) {
            if(mousePos.x / World.TILE_SIZE > constructionSite.getGridPosition().x + constructionSite.getCollider().getWidth())
                originX = constructionSite.getGridPosition().x;
            else if(mousePos.x / World.TILE_SIZE < constructionSite.getGridPosition().x)
                originX = constructionSite.getGridPosition().x + constructionSite.getCollider().getWidth()-1;

            if(mousePos.y < constructionSite.getGridPosition().y * World.TILE_SIZE)
                originY = constructionSite.getGridPosition().y-1;
            else if(mousePos.y > (constructionSite.getGridPosition().y + constructionSite.getCollider().getHeight()) * World.TILE_SIZE)
                originY = constructionSite.getGridPosition().y + constructionSite.getCollider().getHeight();

            boolean canBuild = width >= minX && height >= minY;
            if(!canBuild) batch.setColor(UI.SEMI_TRANSPARENT_RED);
            else batch.setColor(UI.SEMI_TRANSPARENT_GREEN);
            TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), World::isBuildable, originX, originY, mouseGridX, mouseGridY);

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && canBuild) {
                if (mouseGridX < originX) originX -= width;
                if (mouseGridY < originY) originY -= height;
                constructionSite.setFieldCollider(new BoxCollider(originX, originY, width, height));
                Vector2i pos = new Vector2i();
                Vector2i fieldPos = constructionSite.getFieldCollider().getGridPosition();
                int fieldWidth = constructionSite.getFieldCollider().getWidth();
                int fieldHeight = constructionSite.getFieldCollider().getHeight();

                //if plantation then change tiles
                if (constructionSite.getType().isPlantation) {
                    for (int y = 0; y <= fieldHeight; y++) {
                        for (int x = 0; x <= fieldWidth; x++) {
                            pos.set(fieldPos.x + x, fieldPos.y + y);
                            if (World.isBuildable(pos)) World.setTile(pos, Tile.DIRT);
                        }
                    }
                }
                //if not then build fence
                else {
                    //corners
                    pos.set(fieldPos.x, fieldPos.y);
                    if (World.isBuildable(pos))
                        World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_BL), pos.clone()));
                    pos.set(fieldPos.x, fieldPos.y + fieldHeight);
                    if (World.isBuildable(pos))
                        World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_TL), pos.clone()));
                    pos.set(fieldPos.x + fieldWidth, fieldPos.y + fieldHeight);
                    if (World.isBuildable(pos))
                        World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_TR), pos.clone()));
                    pos.set(fieldPos.x + fieldWidth, fieldPos.y);
                    if (World.isBuildable(pos))
                        World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_BR), pos.clone()));
                    //top bottom
                    for (int x = 1; x < fieldWidth; x++) {
                        pos.set(fieldPos.x + x, fieldPos.y + fieldHeight);
                        if (World.isBuildable(pos))
                            World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_T), pos.clone()));
                        pos.set(fieldPos.x + x, fieldPos.y);
                        if (World.isBuildable(pos))
                            World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_B), pos.clone()));
                    }
                    //sides
                    for (int y = 1; y < fieldHeight; y++) {
                        pos.set(fieldPos.x, fieldPos.y + y);
                        if (World.isBuildable(pos))
                            World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_L), pos.clone()));
                        pos.set(fieldPos.x + fieldWidth, fieldPos.y + y);
                        if (World.isBuildable(pos))
                            World.getGameObjects().add(new GameObject(Textures.get(Textures.Environment.FENCE_R), pos.clone()));
                    }
                }

                turnOffTilingMode();
            }
        }
        else if (mode == TilingMode.AREA) {
            if (originX >= 0 && originY >= 0) {
                TileRect.draw(batch, Textures.get(Textures.Tile.DEFAULT), originX, originY, mouseGridX, mouseGridY);
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
            }
            else {
                batch.draw(Textures.get(Textures.Tile.DEFAULT), mouseGridX * World.TILE_SIZE, mouseGridY * World.TILE_SIZE);
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    originX = mouseGridX;
                    originY = mouseGridY;
                }
            }
        }
        else if (mode == TilingMode.PATH) {
            if (originX >= 0 && originY >= 0) {
                if(width >= height) {
                    int temp = Math.min(originX, mouseGridX);
                    for (int i = 0; i <= width; i++) {
                        batch.draw(Textures.get(Textures.Tile.DEFAULT), (temp + i) * World.TILE_SIZE, originY * World.TILE_SIZE);
                    }
                } else {
                    int temp = Math.min(originY, mouseGridY);
                    for (int i = 0; i <= height; i++) {
                        batch.draw(Textures.get(Textures.Tile.DEFAULT), originX * World.TILE_SIZE, (temp + i) * World.TILE_SIZE);
                    }
                }
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
            }
            else {
                batch.draw(Textures.get(Textures.Tile.DEFAULT), mouseGridX * World.TILE_SIZE, mouseGridY * World.TILE_SIZE);
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    originX = mouseGridX;
                    originY = mouseGridY;
                }
            }
        }
        else if (mode == TilingMode.SINGLE) {
            batch.draw(Textures.get(Textures.Tile.DEFAULT), mouseGridX * World.TILE_SIZE, mouseGridY * World.TILE_SIZE);
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) turnOffTilingMode();
        }

        batch.setColor(UI.DEFAULT_COLOR);
    }
}
