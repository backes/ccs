package att.grappa;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public final class EmptyEnumeration<E> implements Enumeration<E> {

    public E nextElement() {
        throw new NoSuchElementException("Empty Enumerator");
    }

    public boolean hasMoreElements() {
        return false;
    }

}