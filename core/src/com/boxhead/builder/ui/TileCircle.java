package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class TileCircle {
    private TileCircle() {}

    public static void draw(SpriteBatch batch, TextureRegion texture, Vector2i position, int radius) {
        /*if(radius > 0) {
            Vector2i pos = new Vector2i();
            for (int i = -radius; i < radius; i++) {
                for (int j = -radius; j < radius; j++) {
                    pos.set(position.x + j * World.TILE_SIZE, position.y + i * World.TILE_SIZE);
                    if (position.distance(pos) < radius) batch.draw(texture, pos.x, pos.y);
                }
            }
        }*/
        draw(batch, texture, position.x, position.y, radius);
    }

    public static void draw(SpriteBatch batch, TextureRegion texture, int posX, int posY, int radius) {
        if(radius > 0) {
            Vector2i pos = new Vector2i();
            for (int i = -radius; i < radius; i++) {
                for (int j = -radius; j < radius; j++) {
                    pos.set(posX + j * World.TILE_SIZE, posY + i * World.TILE_SIZE);
                    if (pos.distance(posX, posY) < radius) batch.draw(texture, pos.x, pos.y);
                }
            }
        }
    }
}
