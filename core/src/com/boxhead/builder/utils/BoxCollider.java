package com.boxhead.builder.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.Tiles;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class BoxCollider implements Iterable<Vector2i>, Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private final Vector2i lowerLeftCorner;
    private final int width, height;

    /**
     * Creates a 2D rectangular collider with the lower left corner at v1.
     *
     * @param lowerLeftCorner lower left corner of the collider (grid)
     * @param width           the width of the collider (tiles)
     * @param height          the height of the collider (tiles)
     */
    public BoxCollider(Vector2i lowerLeftCorner, int width, int height) {
        this.lowerLeftCorner = lowerLeftCorner;
        this.width = width;
        this.height = height;
    }

    public BoxCollider(int x, int y, int width, int height) {
        this(new Vector2i(x, y), width, height);
    }

    public BoxCollider() {
        this(new Vector2i(), 0, 0);
    }

    public boolean overlaps(Vector2i gridPosition) {
        return gridPosition.x >= lowerLeftCorner.x && gridPosition.x < lowerLeftCorner.x + width &&
                gridPosition.y >= lowerLeftCorner.y && gridPosition.y < lowerLeftCorner.y + height;
    }

    public boolean overlaps(BoxCollider other) {
        int thisRight = lowerLeftCorner.x + width;
        int thisTop = lowerLeftCorner.y + height;
        int otherRight = other.lowerLeftCorner.x + other.width;
        int otherTop = other.lowerLeftCorner.y + other.height;

        return lowerLeftCorner.x < otherRight && thisRight > other.lowerLeftCorner.x
                && thisTop > other.lowerLeftCorner.y && lowerLeftCorner.y < otherTop;
    }

    public double distance(Vector2i gridPosition) {
        if (overlaps(gridPosition)) return 0d;

        Vector2i closestTile;
        if (gridPosition.x < lowerLeftCorner.x) {
            if (gridPosition.y <= lowerLeftCorner.y)
                closestTile = lowerLeftCorner;
            else if (gridPosition.y > lowerLeftCorner.y + height - 1)
                closestTile = lowerLeftCorner.plus(0, height - 1);
            else
                closestTile = new Vector2i(lowerLeftCorner.x, gridPosition.y);
        } else if (gridPosition.x < lowerLeftCorner.x + width - 1) {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y);
            else
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y + height - 1);
        } else {
            if (gridPosition.y <= lowerLeftCorner.y)
                closestTile = new Vector2i(lowerLeftCorner.x + width - 1, lowerLeftCorner.y);
            else if (gridPosition.y > lowerLeftCorner.y + height - 1)
                closestTile = new Vector2i(lowerLeftCorner.x + width - 1, lowerLeftCorner.y + height - 1);
            else
                closestTile = new Vector2i(lowerLeftCorner.x + width - 1, gridPosition.y);
        }
        return gridPosition.distance(closestTile);
    }

    public Vector2i getGridPosition() {
        return lowerLeftCorner;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getArea() {
        return width * height;
    }

    public void setGridPosition(Vector2i gridPosition) {
        lowerLeftCorner.set(gridPosition);
    }

    public BoxCollider cloneAndTranslate(Vector2i gridPosition) {
        return new BoxCollider(lowerLeftCorner.plus(gridPosition), width, height);
    }

    public BoxCollider extended() {
        return new BoxCollider(lowerLeftCorner.plus(-1, -1), width + 2, height + 2);
    }

    public List<Vector2i> toVector2iList() {
        int area = width * height;
        List<Vector2i> list = new ArrayList<>(area);
        for (int i = 0; i < area; i++) {
            list.add(new Vector2i(lowerLeftCorner.x + i % width, lowerLeftCorner.y + i / width));
        }
        return list;
    }

    public void draw(SpriteBatch batch, Textures.Tile tileTexture, Predicate<Vector2i> predicate) {
        TextureRegion texture = Textures.get(tileTexture);

        for (Vector2i tile : this) {
            if (predicate.test(tile)) Tiles.drawTile(batch, texture, tile);
        }
    }

    @Override
    public Iterator<Vector2i> iterator() {
        return new Iterator<>() {
            private int x = 0, y = 0;

            @Override
            public boolean hasNext() {
                return y < height;
            }

            @Override
            public Vector2i next() {
                int lastX = x;
                int lastY = y;
                y += ++x / width;
                x = x % width;
                return lowerLeftCorner.plus(lastX, lastY);
            }
        };
    }
}
