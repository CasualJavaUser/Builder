package com.boxhead.builder.utils;

import com.badlogic.gdx.math.Vector2;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

public class Vector2i implements Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final Vector2i UP = new Vector2i(0, 1);
    public static final Vector2i RIGHT = new Vector2i(1, 0);
    public static final Vector2i DOWN = new Vector2i(0, -1);
    public static final Vector2i LEFT = new Vector2i(-1, 0);

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

    public void set(Vector2i v) {
        x = v.x;
        y = v.y;
    }

    //immutable variants
    public Vector2i plus(Vector2i vector) {
        return new Vector2i(x + vector.x, y + vector.y);
    }

    public Vector2i plus(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    public Vector2i minus(Vector2i vector) {
        return new Vector2i(x - vector.x, y - vector.y);
    }

    public Vector2i times(int factor) {
        return new Vector2i(x * factor, y * factor);
    }

    public Vector2i dividedBy(int divisor) {
        return new Vector2i(x / divisor, y / divisor);
    }

    public Vector2i absolute() {
        return new Vector2i(Math.abs(x), Math.abs(y));
    }

    //mutable variants
    public Vector2i add(Vector2i vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }

    public Vector2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2i multiply(int factor) {
        x *= factor;
        y *= factor;
        return this;
    }

    public Vector2i divide(int divisor) {
        x /= divisor;
        y /= divisor;
        return this;
    }

    public Vector2i absolutise() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }

    public double distance(Vector2i vector) {
        return Math.sqrt(Math.pow(x - vector.x, 2d) + Math.pow(y - vector.y, 2d));
    }

    public double distance(int x, int y) {
        return Math.sqrt(Math.pow(this.x - x, 2d) + Math.pow(this.y - y, 2d));
    }

    public double distanceApprox(Vector2i vector) {
        double xDiff = Math.abs(x - vector.x);
        double yDiff = Math.abs(y - vector.y);
        double a;
        double b;
        if (xDiff <= yDiff) {
            a = xDiff;
            b = yDiff;
        } else {
            a = yDiff;
            b = xDiff;
        }
        return b + 0.337 * a;
    }

    /**
     * Consistent with <code>.distance()</code> in comparisons.
     */
    public int distanceSquared(Vector2i vector) {
        int xDiff = x - vector.x;
        int yDiff = y - vector.y;
        return (xDiff * xDiff) + (yDiff * yDiff);
    }

    public Comparator<Vector2i> distanceComparator() {
        return Comparator.comparingInt(vector -> vector.distanceSquared(this));
    }

    public long gridHash() {
        return (long) y << 32 | x;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ')';
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
        int result = 0;
        int bitMaskX = 1;
        int bitMaskY = 1;
        int bitMaskRes = 1;
        for (int i = 0; i < 16; i++)
        {
            if ((x & bitMaskX) != 0)
                result |= bitMaskRes;
            bitMaskRes <<= 1;
            bitMaskX <<= 1;

            if ((y & bitMaskY) != 0)
                result |= bitMaskRes;
            bitMaskRes <<= 1;
            bitMaskY <<= 1;
        }
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

    public Vector2 toVector2() {
        return new Vector2(x, y);
    }

    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }

    public static Vector2i[] line(Vector2i end1, Vector2i end2) {
        Vector2 floatVector = end1.toVector2();

        int diffX = end2.x - end1.x;
        int diffY = end2.y - end1.y;
        int length = Math.max(Math.abs(diffX), Math.abs(diffY));
        float fDiffX = (float) diffX / length;
        float fDiffY = (float) diffY / length;
        Vector2i[] line = new Vector2i[length + 1];
        line[0] = end1.clone();
        line[length] = end2.clone();
        for (int i = 1; i < length; i++) {
            floatVector.add(fDiffX, fDiffY);
            line[i] = new Vector2i((int) (floatVector.x + 0.5f), (int) (floatVector.y + 0.5f));
        }
        return line;
    }
}
