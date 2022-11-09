package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class TileCircle extends UIElement {
    private int radius;

    public TileCircle(TextureRegion texture, Vector2i position, int radius) {
        this(texture, null, position, radius, true);
    }

    public TileCircle(TextureRegion texture, Vector2i position, int radius, boolean visible) {
        this(texture, null, position, radius, visible);
    }

    public TileCircle(TextureRegion texture, UIElement parent, Vector2i position, int radius, boolean visible) {
        super(texture, parent, position, visible);
        this.radius = radius;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if(radius > 0) {
            Vector2i pos = new Vector2i();
            batch.setColor(tint);
            for (int i = -radius; i < radius; i++) {
                for (int j = -radius; j < radius; j++) {
                    pos.set(position.x + j * World.TILE_SIZE, position.y + i * World.TILE_SIZE);
                    if (position.distance(pos) <= radius) batch.draw(texture, pos.x, pos.y);
                }
            }
            batch.setColor(UI.DEFAULT_COLOR);
        }
    }
}
