package com.boxhead.builder.utils;

public class Pair<A, B> {
    public A first;
    public B second;

    private Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }
}
