package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

import java.util.function.Predicate;

public class TileRect {
    private TileRect() {}

    public static void draw(SpriteBatch batch, TextureRegion texture, int x1, int y1, int x2, int y2) {
        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);

        if(x1 > x2) x1 = x2;
        if(y1 > y2) y1 = y2;

        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                batch.draw(texture, (x1 + x) * World.TILE_SIZE, (y1 + y) * World.TILE_SIZE);
            }
        }
    }

    public static void draw(SpriteBatch batch, TextureRegion texture, Predicate<Vector2i> predicate, int x1, int y1, int x2, int y2) {
        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);

        if(x1 > x2) x1 = x2;
        if(y1 > y2) y1 = y2;
        Vector2i pos = new Vector2i();

        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                pos.set(x1 + x, y1 + y);
                if (predicate.test(pos)) batch.draw(texture, pos.x * World.TILE_SIZE, pos.y * World.TILE_SIZE);
            }
        }
    }
}
