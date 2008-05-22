package de.unisb.cs.depend.ccs_sem.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This Map implementation only creates the inner map if an entry is inserted.
 * So it's cheap to create a LazyCreatedMap when most probably no entries will
 * be added
 *
 * @author Clemens Hammacher
 *
 * @param <K> type for the keys
 * @param <V> type for the values
 */
public class LazyCreatedMap<K, V> implements Map<K, V> {


    public static interface MapFactory<K, V> {
        Map<K, V> createMap();
    }

    public static class HashMapFactory<K, V> implements MapFactory<K, V> {

        private final int initialSize;

        public HashMapFactory(int initialSize) {
            this.initialSize = initialSize;
        }

        public Map<K, V> createMap() {
            return new HashMap<K, V>(initialSize);
        }

    }

    private Map<K, V> map = null;
    private final MapFactory<K, V> fac;

    /**
     * Initialises a LazyCreatedMap with a user defined {@link MapFactory}.
     *
     * @param fac the map factory to (later) create the inner map
     */
    public LazyCreatedMap(MapFactory<K, V> fac) {
        this.fac = fac;
    }

    /**
     * Initialises a LazyCreatedMap with a HashMapFactory that will create
     * a map with the given initialSize.
     *
     * @param initialSize the initial size of the lazily created HashMap
     */
    public LazyCreatedMap(int initialSize) {
        this(new HashMapFactory<K, V>(initialSize));
    }

    public void clear() {
        map = null;
    }

    public boolean containsKey(Object key) {
        return map != null && map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map != null && map.containsValue(value);
    }

    public Set<Entry<K, V>> entrySet() {
        createMap();
        return map.entrySet();
    }

    private void createMap() {
        if (map == null)
            map = fac.createMap();
    }

    public V get(Object key) {
        if (map == null)
            return null;
        return map.get(key);
    }

    public boolean isEmpty() {
        return map == null || map.isEmpty();
    }

    public Set<K> keySet() {
        createMap();
        return map.keySet();
    }

    public V put(K key, V value) {
        createMap();
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        createMap();
        map.putAll(t);
    }

    public V remove(Object key) {
        if (map == null)
            return null;
        return map.remove(key);
    }

    public int size() {
        if (map == null)
            return 0;
        return map.size();
    }

    public Collection<V> values() {
        createMap();
        return map.values();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fac.hashCode();
        result = prime * result + ((map == null || map.isEmpty()) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LazyCreatedMap<?, ?> other = (LazyCreatedMap<?, ?>) obj;
        if (map == null) {
            if (other.map != null)
                return false;
        } else if (!map.equals(other.map))
            return false;
        return true;
    }

}
