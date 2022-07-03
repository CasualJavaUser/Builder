package com.boxhead.builder;

import com.badlogic.gdx.math.Vector2;

public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
        x = 0;
        y = 0;
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2 vector) {
        this.x = (int) vector.x;
        this.y = (int) vector.y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        Vector2i vector = (Vector2i) other;
        return x == vector.x && y == vector.y;
    }

    @Override
    public int hashCode() {
        int result;
        result = 13 + Float.floatToIntBits((float) x);
        result = 13 * result + Float.floatToIntBits((float) y);
        return result;
    }
}
