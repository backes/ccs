package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class ValueList extends ArrayList<Value> {

    private static final long serialVersionUID = -2026089497522373338L;

    public ValueList() {
        super();
    }

    public ValueList(Collection<? extends Value> c) {
        super(c);
    }

    public ValueList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public int hashCode() {
        return hashCode(new HashMap<ParameterOrProcessEqualsWrapper, Integer>());
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        int result = 1;
        for (final Value val: this)
            result = 31 * result + (val == null ? 0 : val.hashCode(parameterOccurences));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return equals(new HashMap<ParameterOrProcessEqualsWrapper, Integer>());
    }

    public boolean equals(Object o,
            Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

        final ListIterator<Value> i1 = listIterator();
        final ListIterator<Value> i2 = ((ValueList) o).listIterator();
        while (i1.hasNext() && i2.hasNext()) {
            final Value v1 = i1.next();
            final Value v2 = i2.next();
            if (!(v1==null ? v2==null : v1.equals(v2, parameterOccurences)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }

}
