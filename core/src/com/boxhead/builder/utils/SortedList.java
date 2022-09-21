package com.boxhead.builder.utils;

import java.util.*;

public class SortedList<T> implements Collection<T> {
    private List<T> list;

    private Comparator<T> comparator;

    public SortedList(Comparator<T> comparator) {
        this.comparator = comparator;
        list = new ArrayList<>();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        int i = 0;
        while (i < list.size() && comparator.compare(t, list.get(i)) < 0) i ++;
        list.add(null);
        for (int j = list.size()-1; j > i; j--) {
            list.set(j, list.get(j-1));
        }
        list.set(i, t);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int i = list.indexOf(o);
        for (; i < list.size()-1; i++) {
            list.set(i, list.get(i+1));
        }
        list.remove(list.size()-1);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for(T t : c) {
            add(t);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            remove(o);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        for (T t : list) {
            for (Object o : c) {
                if (t != c) remove(t);
            }
        }
        return true;
    }

    @Override
    public void clear() {
        list.clear();
    }

    public T get(int index) {
        return list.get(index);
    }

    public T remove(int index) {
        T t = list.get(index);
        for (; index < list.size()-1; index++) {
            list.set(index, list.get(index+1));
        }
        list.remove(list.size()-1);
        return t;
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
