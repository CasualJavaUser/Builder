package com.boxhead.builder.utils;

import java.util.*;

public class SortedList<T> implements Collection<T> {
    private final List<T> list;

    private final Comparator<T> comparator;

    private static final String NPE_MESSAGE = "SortedList does not allow null elements";

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
        return binarySearchEquals((T) o) != -1;
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
        if (t == null)
            throw new NullPointerException(NPE_MESSAGE);
        if (list.size() == 0) {
            list.add(t);
            return true;
        }
        int seek = binarySearchComparable(t);

        list.add(null);
        for (int i = list.size() - 1; i > seek; i--) {
            list.set(i, list.get(i - 1));
        }
        list.set(seek, t);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i == -1)
            return false;

        for (; i < list.size() - 1; i++) {
            list.set(i, list.get(i + 1));
        }
        list.remove(list.size() - 1);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            add(t);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o))
                changed = true;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean changed = false;
        for (int i = 0; i < list.size(); i++) {
            boolean contains = false;
            for (Object o : collection) {
                if (contains(o)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                remove(i);
                changed = true;
            }
        }
        return changed;
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
        for (; index < list.size() - 1; index++) {
            list.set(index, list.get(index + 1));
        }
        list.remove(list.size() - 1);
        return t;
    }

    public int indexOf(Object o) {
        if (o != null) {
            return binarySearchEquals((T) o);
        } else {
            throw new NullPointerException(NPE_MESSAGE);
        }
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

    /**
     * Returns an index (<b>i</b>) such that list[<b>i</b>].equals(t) is true. If there are multiple equal elements in the list,
     * no guarantees are gives as to which index will be returned.
     *
     * @return index ranging [0, size] inclusive, or <b>0</b> if the list's empty.
     */
    private int binarySearchEquals(T t) {
        int size = list.size();
        if (size == 0)
            return -1;
        if (size < 10)
            return linearSearchEqual(t);

        int step = size / 2;    //not (size / 4) because it is further divided in 'while'
        int seek = size / 2;

        int comp = comparator.compare(t, list.get(seek));
        int prevComp;

        while (seek >= 0 && seek < size) {
            if (list.get(seek).equals(t))
                return seek;

            prevComp = comp;
            comp = comparator.compare(t, list.get(seek));

            if (step == 1 && comp >> 31 != prevComp >> 31)
                return -1;

            step /= 2;
            if (step == 0) step++;

            if (comp < 0)
                seek += step;
            else
                seek -= step;
        }
        return -1;
    }

    /**
     * A linear alternative to {@code binarySearchEqual()} (for details refer to it).
     * Unlike binary search this method guarantees returning index of the first occurrence of the given object (t).
     */
    private int linearSearchEqual(T t) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(t))
                return i;
        }
        return -1;
    }

    /**
     * Returns an index (<b>i</b>) such that list[<b>i</b> - 1] >= t >= list[<b>i</b>]
     * based on the provided comparator {@code compare()} method.
     *
     * @return index ranging [0, size] inclusive, or <b>0</b> if the list's empty.
     */
    private int binarySearchComparable(T t) {
        int size = list.size();
        if (size == 0)
            return 0;
        if (size < 10)  //not worth performing a binary search due to program control overhead
            return linearSearchComparable(t);   //also, should this check ever be removed, new check for sizes < 2 would need to be put in its place
        int step = size / 2;    //not (size / 4) because it is further divided in 'while'
        int seek = size / 2;

        while (seek > 0 && seek < size) {
            step /= 2;
            if (step == 0) step++;

            int comp = comparator.compare(t, list.get(seek));
            int compPrev = comparator.compare(t, list.get(seek - 1));
            if (comp == 0 || (comp >= 0 && compPrev <= 0)) {
                break;
            }
            if (comp < 0) {
                seek += step;
            } else {
                seek -= step;
            }
        }
        return seek;
    }

    /**
     * A linear alternative to {@code binarySearchComparable()} (for details refer to it).
     */
    private int linearSearchComparable(T t) {
        int i = 0;
        while (i < list.size() && comparator.compare(t, list.get(i)) < 0) i++;
        return i;
    }
}
