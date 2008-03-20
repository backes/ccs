package de.unisb.cs.depend.ccs_sem.parser;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This is a ListIterator with some additional functionality (e.g. peek).
 *
 * @author Clemens Hammacher
 */
public class ExtendedListIterator<E> implements ListIterator<E> {

    private List<E> list;
    private int position;

    public ExtendedListIterator(List<E> tokens, int position) {
        super();
        this.list = tokens;
        setPosition(position);
    }

    public ExtendedListIterator(List<E> tokens) {
        this(tokens, 0);
    }

    public void add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return position < list.size();
    }

    public boolean hasPrevious() {
        return position > 0;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        return list.get(position++);
    }

    public int nextIndex() {
        return position;
    }

    public E previous() {
        if (!hasPrevious())
            throw new NoSuchElementException();

        return list.get(--position);
    }

    public int previousIndex() {
        return position - 1;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    public E set(int index, E e) {
        return list.set(index, e);
    }

    public E peek() {
        if (!hasNext())
            throw new NoSuchElementException();

        return list.get(position);
    }

    public E peekPrevious() {
        if (!hasPrevious())
            throw new NoSuchElementException();

        return list.get(position-1);
    }

    public void setPosition(int index) {
        if (position < 0 || position > list.size())
            throw new IndexOutOfBoundsException();
        position = index;
    }

}
