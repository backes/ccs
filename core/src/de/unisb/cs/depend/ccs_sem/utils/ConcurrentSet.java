package de.unisb.cs.depend.ccs_sem.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentSet<E> extends AbstractSet<E> {

    private transient final ConcurrentMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    public ConcurrentSet() {
        map = new ConcurrentHashMap<E, Object>();
    }

    public ConcurrentSet(int concurrencyLevel) {
        map = new ConcurrentHashMap<E, Object>(16, .75f, concurrencyLevel);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.putIfAbsent(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }
}