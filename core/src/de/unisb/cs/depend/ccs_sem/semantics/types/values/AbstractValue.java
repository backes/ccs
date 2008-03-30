package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


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
        return getStringValue().compareTo(o.getStringValue());
    }

}
