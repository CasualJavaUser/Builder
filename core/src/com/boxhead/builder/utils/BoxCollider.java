package com.boxhead.builder.utils;

public class BoxCollider {
    private Vector2i lowerLeftCorner;
    private int width, height;

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

    public boolean overlap(Vector2i gridPosition) {
        return gridPosition.x >= lowerLeftCorner.x && gridPosition.x < lowerLeftCorner.x + width &&
                gridPosition.y >= lowerLeftCorner.y && gridPosition.y < lowerLeftCorner.y + height;
    }

    public double distance(Vector2i gridPosition) {
        if (overlap(gridPosition)) return 0d;

        Vector2i closestTile;
        if (gridPosition.x < lowerLeftCorner.x) {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = lowerLeftCorner;
            else if (gridPosition.y > lowerLeftCorner.y + height)
                closestTile = lowerLeftCorner.add(0, height);
            else
                closestTile = new Vector2i(lowerLeftCorner.x, gridPosition.y);
        } else if (gridPosition.x < lowerLeftCorner.x + width) {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y);
            else
                closestTile = new Vector2i(gridPosition.x, lowerLeftCorner.y + height);
        } else {
            if (gridPosition.y < lowerLeftCorner.y)
                closestTile = new Vector2i(lowerLeftCorner.x + width, lowerLeftCorner.y);
            else if (gridPosition.y > lowerLeftCorner.y + height)
                closestTile = new Vector2i(lowerLeftCorner.x + width, lowerLeftCorner.y + height);
            else
                closestTile = new Vector2i(lowerLeftCorner.x + width, gridPosition.y);
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

    public void setGridPosition(Vector2i gridPosition) {
        lowerLeftCorner.set(gridPosition);
    }
}
