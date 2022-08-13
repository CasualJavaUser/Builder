package com.boxhead.builder.utils;

public class BoxCollider {
    private Vector2i lowerLeftCorner;
    private int width, height;

    /**
     * Creates a 2D rectangular collider with the lower left corner at v1.
     *
     * @param lowerLeftCorner lower left corner of the collider
     * @param width           the width of the collider
     * @param height          the height of the collider
     */
    public BoxCollider(Vector2i lowerLeftCorner, int width, int height) {
        this.lowerLeftCorner = lowerLeftCorner;
        this.width = width;
        this.height = height;
    }

    public boolean overlap(Vector2i position) {
        return position.x >= lowerLeftCorner.x && position.x < lowerLeftCorner.x + width &&
                position.y >= lowerLeftCorner.y && position.y < lowerLeftCorner.y + height;
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
