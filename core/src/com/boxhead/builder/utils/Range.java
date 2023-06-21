package com.boxhead.builder.utils;

import java.io.Serializable;

public class Range<T extends Comparable<T>> implements Serializable {
    private final T lowerLimit;
    private final T upperLimit;

    private Range(T fromInclusive, T toInclusive) {
        if (toInclusive.compareTo(fromInclusive) > 0) {
            this.lowerLimit = fromInclusive;
            this.upperLimit = toInclusive;
        } else {
            this.lowerLimit = toInclusive;
            this.upperLimit = fromInclusive;
        }
    }

    public static <T extends Comparable<T>> Range<T> between(T fromInclusive, T toInclusive) {
        return new Range<>(fromInclusive, toInclusive);
    }

    public T fit(T element) {
        if (upperLimit.compareTo(element) <= 0)
            return upperLimit;
        if (lowerLimit.compareTo(element) >= 0)
            return lowerLimit;
        return element;
    }

    public boolean contains(T element) {
        if (element == null) {
            return false;
        }
        return lowerLimit.compareTo(element) < 0 && upperLimit.compareTo(element) > 0;
    }
}
