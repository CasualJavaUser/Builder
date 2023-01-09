package com.boxhead.builder.utils;

import java.io.Serializable;
import java.util.Objects;

public class Pair<A, B> implements Serializable {
    public A first;
    public B second;

    private Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) return false;

        return ((Pair<?, ?>) other).first.equals(first) && ((Pair<?, ?>) other).second.equals(second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return first.toString() + " " + second.toString();
    }
}
