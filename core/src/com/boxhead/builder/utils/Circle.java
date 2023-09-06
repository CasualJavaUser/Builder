package com.boxhead.builder.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Textures;
import com.boxhead.builder.Tiles;

import java.util.Iterator;

public class Circle implements Iterable<Vector2i> {
    private final Vector2i centre;
    private final int radius;

    public Circle(Vector2i centre, int radius) {
        this.centre = centre;
        this.radius = radius;
    }

    public static void draw(SpriteBatch batch, Textures.Tile tileTexture, Vector2i centre, int radius) {
        if (radius <= 0) return;
        TextureRegion texture = Textures.get(tileTexture);

        Circle circle = new Circle(centre, radius);
        for (Vector2i tile : circle) {
            Tiles.drawTile(batch, texture, tile);
        }
    }

    /**
     * Rotates around the centre 90 degrees right.
     */
    private Vector2i rotate(Vector2i tile) {
        int xDiff = tile.x - centre.x;
        int yDiff = tile.y - centre.y;
        return new Vector2i(centre.x + yDiff, centre.y - xDiff);
    }

    /**
     * Returns tiles with increasing distance from the centre.
     */
    public Iterator<Vector2i> orderedIterator() {
        return new Iterator<>() {
            private final int[] distanceFromSymmetry = new int[radius];
            private final int radiusSquared = radius * radius;
            private Vector2i lastReturnedTile = new Vector2i();
            private int rotation = 3;
            private boolean hasNext = true;
            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Vector2i next() {
                if (rotation < 3) {
                    rotation++;
                    lastReturnedTile = rotate(lastReturnedTile);
                    return lastReturnedTile;
                }

                rotation = 0;
                int minDistance = Integer.MAX_VALUE;
                Vector2i closest = null;
                Vector2i temp = new Vector2i();
                for (int y = centre.y + 1, i = 0; y < centre.y + radius; y++, i++) {
                    temp.set(centre.x + distanceFromSymmetry[i], y);
                    int currentDistance = centre.distanceSquared(temp);
                    if (currentDistance >= minDistance)
                        continue;

                    minDistance = currentDistance;
                    closest = temp.clone();
                    if (distanceFromSymmetry[i] == 0)   //no point going farther from the centre
                        break;
                }
                if (minDistance >= radiusSquared) {
                    hasNext = false;
                    temp.set(centre);
                    return temp;
                }
                distanceFromSymmetry[closest.y - centre.y - 1]++;
                lastReturnedTile = closest;
                return closest;
            }
        };
    }

    /**
     * Fast Iterator - doesn't guarantee any particular tile ordering.
     */
    @Override
    public Iterator<Vector2i> iterator() {
        return new Iterator<>() {
            private final int radiusSquared = radius * radius;
            private int previousHeight = radius - 1;
            private int x = centre.x, y = centre.y + radius - 1;
            private boolean mirror = false;

            @Override
            public boolean hasNext() {
                return x != centre.x - radius;
            }

            @Override
            public Vector2i next() {
                int lastX = x;
                int lastY = y;

                y--;
                if (y < centre.y - previousHeight) {    //next column
                    if (mirror) x--;
                    else x++;
                    if (x == centre.x + radius) {
                        mirror = true;
                        x = centre.x - 1;
                        previousHeight = radius - 1;
                    }
                    y = centre.y + previousHeight;
                    Vector2i temp = new Vector2i(x, y);
                    while (centre.distanceSquared(temp) > radiusSquared) {
                        y--;
                        temp.set(x, y);
                    }
                    previousHeight = y - centre.y;
                }
                return new Vector2i(lastX, lastY);
            }
        };
    }
}
