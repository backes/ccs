package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;


public class ParameterList extends ArrayList<Parameter> {

    private static final long serialVersionUID = -1213534458772337187L;

    public ParameterList() {
        super();
    }

    public ParameterList(int initialCapacity) {
        super(initialCapacity);
    }

    public ParameterList(Collection<? extends Parameter> c) {
        super(c);
    }

    @Override
    public int hashCode() {
        return hashCode(new HashMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        int result = 1;
        for (final Parameter param: this)
            result = 31 * result + (param == null ? 0 : param.hashCode(parameterOccurences));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return equals(new HashMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public boolean equals(Object o,
            Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

        final ListIterator<Parameter> i1 = listIterator();
        final ListIterator<Parameter> i2 = ((ParameterList) o).listIterator();
        while (i1.hasNext() && i2.hasNext()) {
            final Parameter r1 = i1.next();
            final Parameter r2 = i2.next();
            if (!(r1==null ? r2==null : r1.equals(r2, parameterOccurences)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }

}
