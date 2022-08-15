package com.boxhead.builder.utils;

import com.boxhead.builder.World;

public class BoxCollider {
    private Vector2i lowerLeftCorner;
    private int width, height;

    /**
     * Creates a 2D rectangular collider with the lower left corner at v1.
     *
     * @param lowerLeftCorner lower left corner of the collider (grid)
     * @param width           the width of the collider (pixel)
     * @param height          the height of the collider (pixel)
     */
    public BoxCollider(Vector2i lowerLeftCorner, int width, int height) {
        this.lowerLeftCorner = lowerLeftCorner;
        this.width = width;
        this.height = height;
    }

    public boolean overlap(Vector2i gridPosition) {
        return gridPosition.x >= lowerLeftCorner.x && gridPosition.x < lowerLeftCorner.x + (width / World.TILE_SIZE) &&
                gridPosition.y >= lowerLeftCorner.y && gridPosition.y < lowerLeftCorner.y + (height / World.TILE_SIZE);
    }

    public double distance(Vector2i gridPosition) {
        if (overlap(gridPosition)) return 0d;

        Vector2i closestTile;
        if (gridPosition.x < lowerLeftCorner.x) {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = lowerLeftCorner;
            else if (gridPosition.y > lowerLeftCorner.y + (height / World.TILE_SIZE))
                closestTile = lowerLeftCorner.addScalar(0, height / World.TILE_SIZE);
            else
                closestTile = new Vector2i(lowerLeftCorner.x, gridPosition.y);
        } else if (gridPosition.x < lowerLeftCorner.x + (width / World.TILE_SIZE)) {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y);
            else
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y + (height / World.TILE_SIZE));
        } else {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = new Vector2i(lowerLeftCorner.x + (width / World.TILE_SIZE), lowerLeftCorner.y);
            else if (gridPosition.y > lowerLeftCorner.y + (height / World.TILE_SIZE))
                closestTile = new Vector2i(lowerLeftCorner.x + (width / World.TILE_SIZE), lowerLeftCorner.y + (height / World.TILE_SIZE));
            else
                closestTile = new Vector2i(lowerLeftCorner.x + (width / World.TILE_SIZE), gridPosition.y);
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
}
