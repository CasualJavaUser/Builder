package com.boxhead.builder.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A naive approach to bi-directional mapping
 */
public class BidirectionalMap<K, V> implements Map<K, V>, Serializable {
    private final Map<K, V> mapKV;
    private final Map<V, K> mapVK;

    private static final String UOE_MESSAGE = "cannot map multiple keys to one value";

    public BidirectionalMap() {
        mapKV = new HashMap<>();
        mapVK = new HashMap<>();
    }

    public BidirectionalMap(int initialCapacity) {
        mapKV = new HashMap<>(initialCapacity);
        mapVK = new HashMap<>(initialCapacity);
    }

    public BidirectionalMap(int initialCapacity, float loadFactor) {
        mapKV = new HashMap<>(initialCapacity, loadFactor);
        mapVK = new HashMap<>(initialCapacity, loadFactor);
    }

    @Override
    public int size() {
        return mapKV.size();
    }

    @Override
    public boolean isEmpty() {
        return mapKV.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapKV.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mapVK.containsKey(value);
    }

    @Override
    public V get(Object key) {
        return mapKV.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (mapVK.containsKey(value))
            throw new UnsupportedOperationException(UOE_MESSAGE);
        mapVK.put(value, key);
        return mapKV.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V value = mapKV.remove(key);
        mapVK.remove(value);
        return value;
    }

    @Override
    public V replace(K key, V value) {
        if (mapVK.containsKey(value))
            throw new UnsupportedOperationException(UOE_MESSAGE);
        V currentValue = mapKV.get(key);
        mapVK.remove(currentValue);
        mapVK.put(value, key);
        return mapKV.replace(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public void clear() {
        mapKV.clear();
        mapVK.clear();
    }

    @Override
    public Set<K> keySet() {
        return mapKV.keySet();
    }

    @Override
    public Collection<V> values() {
        return mapKV.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return mapKV.entrySet();
    }

    public K getByValue(V value) {
        return mapVK.get(value);
    }
}
