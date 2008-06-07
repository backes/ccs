package de.unisb.cs.depend.ccs_sem.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This Map implementation only creates the inner map if an entry is inserted.
 * So it's cheap to create a LazyCreatedMap when most probably no entries will
 * be added.
 *
 * @author Clemens Hammacher
 *
 * @param <K> type for the keys
 * @param <V> type for the values
 */
public class LazyCreatedMap<K, V> implements Map<K, V> {

    public class KeySet implements Set<K> {

        private Set<K> keySet;

        private Set<K> getKeySet() {
            if (keySet == null)
                keySet = getMap().keySet();
            return keySet;
        }

        public boolean add(K o) {
            return getKeySet().add(o);
        }

        public boolean addAll(Collection<? extends K> c) {
            if (c.isEmpty())
                return false;
            return getKeySet().addAll(c);
        }

        public void clear() {
            if (!isEmpty())
                getKeySet().clear();
        }

        public boolean contains(Object o) {
            return !isEmpty() && getKeySet().contains(o);
        }

        public boolean containsAll(Collection<?> c) {
            if (c.isEmpty())
                return true;
            return !isEmpty() && getKeySet().containsAll(c);
        }

        public boolean isEmpty() {
            return map == null || getKeySet().isEmpty();
        }

        public Iterator<K> iterator() {
            if (keySet != null)
                return keySet.iterator();
            if (isEmpty())
                return new EmptyIterator<K>();
            return getKeySet().iterator();
        }

        public boolean remove(Object o) {
            if (isEmpty())
                return false;
            return getKeySet().remove(o);
        }

        public boolean removeAll(Collection<?> c) {
            if (c.isEmpty() || isEmpty())
                return false;
            return getKeySet().removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            if (isEmpty())
                return false;
            return getKeySet().retainAll(c);
        }

        public int size() {
            if (map == null)
                return 0;
            return getKeySet().size();
        }

        public Object[] toArray() {
            if (isEmpty())
                return new Object[0];
            return getKeySet().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return getKeySet().toArray(a);
        }

    }

    public class Values implements Collection<V> {

        private Collection<V> values;

        private Collection<V> getValues() {
            if (values == null)
                values = getMap().values();
            return values;
        }

        public boolean add(V o) {
            return getValues().add(o);
        }

        public boolean addAll(Collection<? extends V> c) {
            if (c.isEmpty())
                return false;
            return getValues().addAll(c);
        }

        public void clear() {
            if (!isEmpty())
                getValues().clear();
        }

        public boolean contains(Object o) {
            return !isEmpty() && getValues().contains(o);
        }

        public boolean containsAll(Collection<?> c) {
            if (c.isEmpty())
                return true;
            return !isEmpty() && getValues().containsAll(c);
        }

        public boolean isEmpty() {
            return map == null || getValues().isEmpty();
        }

        public Iterator<V> iterator() {
            if (values != null)
                return values.iterator();
            if (isEmpty())
                return new EmptyIterator<V>();
            return getValues().iterator();
        }

        public boolean remove(Object o) {
            if (isEmpty())
                return false;
            return getValues().remove(o);
        }

        public boolean removeAll(Collection<?> c) {
            if (c.isEmpty() || isEmpty())
                return false;
            return getValues().remove(c);
        }

        public boolean retainAll(Collection<?> c) {
            if (isEmpty())
                return false;
            return getValues().retainAll(c);
        }

        public int size() {
            if (map == null)
                return 0;
            return getValues().size();
        }

        public Object[] toArray() {
            if (isEmpty())
                return new Object[0];
            return getValues().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return getValues().toArray(a);
        }

    }

    public class EntrySet implements Set<Entry<K, V>> {

        private Set<Entry<K, V>> entrySet = null;

        private Set<Entry<K, V>> getEntrySet() {
            if (entrySet == null)
                entrySet = getMap().entrySet();
            return entrySet;
        }

        public boolean add(Entry<K, V> o) {
            return getEntrySet().add(o);
        }

        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            if (!c.isEmpty())
                return getEntrySet().addAll(c);
            return false;
        }

        public void clear() {
            if (!isEmpty())
                getEntrySet().clear();
        }

        public boolean contains(Object o) {
            return !isEmpty() && getEntrySet().contains(o);
        }

        public boolean containsAll(Collection<?> c) {
            if (c.isEmpty())
                return true;
            return !isEmpty() && getEntrySet().containsAll(c);
        }

        public boolean isEmpty() {
            return map == null || getEntrySet().isEmpty();
        }

        public Iterator<Entry<K, V>> iterator() {
            if (entrySet != null)
                return entrySet.iterator();
            if (isEmpty())
                return new EmptyIterator<Entry<K, V>>();
            return getEntrySet().iterator();
        }

        public boolean remove(Object o) {
            if (isEmpty())
                return false;
            return getEntrySet().remove(o);
        }

        public boolean removeAll(Collection<?> c) {
            if (c.isEmpty() || isEmpty())
                return false;
            return getEntrySet().removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            if (isEmpty())
                return false;
            return getEntrySet().retainAll(c);
        }

        public int size() {
            if (map == null)
                return 0;
            return getEntrySet().size();
        }

        public Object[] toArray() {
            if (isEmpty())
                return new Object[0];
            return getEntrySet().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return getEntrySet().toArray(a);
        }

    }

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

    protected Map<K, V> map = null;
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
        return new EntrySet();
    }

    protected Map<K, V> getMap() {
        if (map == null)
            map = fac.createMap();
        return map;
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
        return new KeySet();
    }

    public V put(K key, V value) {
        return getMap().put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        if (!t.isEmpty())
            getMap().putAll(t);
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
        return new Values();
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

    @Override
    public String toString() {
        return map == null ? "{}" : map.toString();
    }

}
