package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Collections;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


/**
 * An abstract {@link Value} implementation that provides default implementations of the
 * abstract methods in the interface {@link Value}.
 *
 * @author Clemens Hammacher
 */
public abstract class AbstractValue implements Value {

    public Value instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public String toString() {
        return getStringValue();
    }

    // this could be refined by subclasses
    public int compareTo(Value o) {
        final Class<? extends AbstractValue> c1 = getClass();
        final Class<? extends Value> c2 = o.getClass();
        final int classCompare = c1.equals(c2) ? 0 : c1.getName().compareTo(c2.getSimpleName());
        return classCompare == 0 ? getStringValue().compareTo(o.getStringValue())
                                 : classCompare;
    }

    @Override
    public final boolean equals(Object obj) {
        final Map<ParameterOrProcessEqualsWrapper, Integer> emptyMap = Collections.emptyMap();
        // the map is not modified, so we need no HashMap!
        return equals(obj, emptyMap);
    }

    @Override
    public final int hashCode() {
        final Map<ParameterOrProcessEqualsWrapper, Integer> emptyMap = Collections.emptyMap();
        // the map is not modified, so we need no HashMap!
        return hashCode(emptyMap);
    }

}
