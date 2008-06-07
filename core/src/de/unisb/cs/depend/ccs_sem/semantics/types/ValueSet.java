package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.LazyCreatedMap;


public class ValueSet extends TreeSet<Value> {

    private static final long serialVersionUID = 1210778863338572990L;

    public ValueSet() {
        super();
    }

    public ValueSet(Collection<? extends Value> c) {
        super(c);
    }

    public ValueSet(Comparator<? super Value> comparator) {
        super(comparator);
    }

    public ValueSet(SortedSet<Value> s) {
        super(s);
    }

    @Override
    public int hashCode() {
        return hashCode(new LazyCreatedMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        int result = 1;
        for (final Value val: this)
            if (val != null)
                result += val.hashCode(parameterOccurences);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return equals(new LazyCreatedMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public boolean equals(Object o,
            Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final ValueSet other = (ValueSet) o;
        if (size() != other.size())
            return false;

        // the set is ordered, to
        final Iterator<Value> i1 = iterator();
        final Iterator<Value> i2 = ((ValueSet) o).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            final Value v1 = i1.next();
            final Value v2 = i2.next();
            if (!(v1==null ? v2==null : v1.equals(v2, parameterOccurences)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }

}
