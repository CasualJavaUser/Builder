package com.boxhead.builder;

import com.badlogic.gdx.math.Vector2;

public class Vector2i implements Cloneable {
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

    public double distance(Vector2i vector) {
        return Math.sqrt(Math.pow(x - vector.x, 2d) + Math.pow(y - vector.y, 2d));
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

    @Override
    public Vector2i clone() {
        try {
            return (Vector2i) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
