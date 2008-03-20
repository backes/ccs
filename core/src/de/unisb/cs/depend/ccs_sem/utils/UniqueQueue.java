package de.unisb.cs.depend.ccs_sem.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A Queue in which every element is only inserted once. Further insertions have
 * no effekt.
 *
 * @author Clemens Hammacher
 */
public class UniqueQueue<E> extends ArrayQueue<E> {
    private final Set<E> seen;

    public UniqueQueue() {
        super();
        seen = new HashSet<E>();
    }

    public UniqueQueue(Collection<? extends E> c) {
        super(c);
        seen = new HashSet<E>(c);
    }

    public UniqueQueue(int numElements) {
        super(numElements);
        seen = new HashSet<E>(numElements);
    }

    public Set<E> getSeen() {
        return Collections.unmodifiableSet(seen);
    }

    @Override
    public void addFirst(E e) {
        if (seen.add(e))
            super.addFirst(e);
    }

    @Override
    public void addLast(E e) {
        if (seen.add(e))
            super.addLast(e);
    }

    @Override
    public boolean add(E e) {
        if (!seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        if (!seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    public boolean offerFirst(E e) {
        if (!seen.add(e))
            return false;
        super.addFirst(e);
        return true;
    }

    public boolean offerLast(E e) {
        if (!seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    /**
     * Resets the set of seen elements.
     * After this operation, every elements is again accepted exactly once.
     */
    public void clearSeen() {
        seen.clear();
    }

}
