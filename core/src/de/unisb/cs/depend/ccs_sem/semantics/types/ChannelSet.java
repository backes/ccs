package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;


public class ChannelSet extends TreeSet<Channel> {

    private static final long serialVersionUID = 1210778863338572990L;

    public ChannelSet() {
        super();
    }

    public ChannelSet(Collection<? extends Channel> c) {
        super(c);
    }

    public ChannelSet(Comparator<? super Channel> comparator) {
        super(comparator);
    }

    public ChannelSet(SortedSet<Channel> s) {
        super(s);
    }

    @Override
    public int hashCode() {
        return hashCode(new HashMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences) {
        int result = 1;
        // remember that the set is ordered, so there should be no ambiguousness
        for (final Channel ch: this)
            if (ch != null)
                result += ch.hashCode(parameterOccurences);
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
        final ChannelSet other = (ChannelSet) o;
        if (size() != other.size())
            return false;

        // the set is ordered, to
        final Iterator<Channel> i1 = iterator();
        final Iterator<Channel> i2 = ((ChannelSet) o).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            final Channel c1 = i1.next();
            final Channel c2 = i2.next();
            if (!(c1==null ? c2==null : c1.equals(c2, parameterOccurences)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }

}
