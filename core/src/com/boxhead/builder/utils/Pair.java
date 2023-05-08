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
        Object otherFirst = ((Pair<?, ?>) other).first;
        Object otherSecond = ((Pair<?, ?>) other).second;

        boolean equalFirst;
        if (first == null || otherFirst == null) {
            equalFirst = first == otherFirst;
        } else
            equalFirst = first.equals(otherFirst);

        boolean equalSecond;
        if (second == null || otherSecond == null) {
            equalSecond = second == otherSecond;
        } else
            equalSecond = second.equals(otherSecond);

        return equalFirst && equalSecond;
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
