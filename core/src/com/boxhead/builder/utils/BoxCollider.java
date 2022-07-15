package com.boxhead.builder.utils;

public class BoxCollider {
    private Vector2i v1;
    private int width, height;

    /**
     * Creates a 2D rectangular collider with the lower left corner at v1.
     * @param v1 lower left corner of the collider
     * @param width the width of the collider
     * @param height the height of the collider
     */
    public BoxCollider(Vector2i v1, int width, int height) {
        this.v1 = v1;
        this.width = width;
        this.height = height;
    }

    public boolean Overlap(Vector2i position) {
        return position.x >= v1.x && position.x < v1.x + width &&
                position.y >= v1.y && position.y < v1.y + height;
    }

    public Vector2i getPosition() {
        return v1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
