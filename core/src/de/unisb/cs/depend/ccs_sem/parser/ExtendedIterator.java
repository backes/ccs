package de.unisb.cs.depend.ccs_sem.parser;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This is a read-only ListIterator, that has some extended methods specially
 * for Parsing (look-ahead).
 *
 * @author Clemens Hammacher
 */
public class ExtendedIterator<E> implements ListIterator<E> {

    private List<E> list;
    private int position;

    public ExtendedIterator(List<E> tokens, int position) {
        super();
        this.list = tokens;
        setPosition(position);
    }


    public ExtendedIterator(List<E> tokens) {
        this(tokens, 0);
    }

    public void add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return position < list.size();
    }

    public boolean hasPrevious() {
        return position >= 0;
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

    public E peek() {
        if (!hasNext())
            throw new NoSuchElementException();

        return list.get(position);
    }

    public boolean lookahead(Class<? extends E> class1) {
        return (position + 1 < list.size()
                && class1.isInstance(list.get(position+1)));
    }

    public boolean lookahead(Class<? extends E> class1, Class<? extends E> class2) {
        return (position + 2 < list.size()
                && class1.isInstance(list.get(position+1))
                && class2.isInstance(list.get(position+2)));
    }

    public boolean lookahead(Class<? extends E> class1, Class<? extends E> class2,
            Class<? extends E> class3) {
        return (position + 3 < list.size()
                && class1.isInstance(list.get(position+1))
                && class2.isInstance(list.get(position+1))
                && class3.isInstance(list.get(position+2)));
    }

    public boolean lookahead(Class<? extends E> class1, Class<? extends E> class2,
            Class<? extends E> class3, Class<? extends E> class4) {
        return (position + 4 < list.size()
                && class1.isInstance(list.get(position+1))
                && class2.isInstance(list.get(position+1))
                && class3.isInstance(list.get(position+1))
                && class4.isInstance(list.get(position+2)));
    }

    public boolean lookahead(Class<? extends E> class1, Class<? extends E> class2,
            Class<? extends E> class3, Class<? extends E> class4, Class<? extends E> class5) {
        return (position + 5 < list.size()
                && class1.isInstance(list.get(position+1))
                && class2.isInstance(list.get(position+1))
                && class3.isInstance(list.get(position+1))
                && class4.isInstance(list.get(position+1))
                && class5.isInstance(list.get(position+2)));
    }

    public void setPosition(int index) {
        if (position < 0 || position > list.size())
            throw new IndexOutOfBoundsException();
        position = index;
    }

}
