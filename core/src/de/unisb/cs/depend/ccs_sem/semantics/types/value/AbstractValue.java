package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


/**
 * An abstract {@link Value} implementation that provides default implementations of the
 * abstract methods in the interface {@link Value}.
 *
 * @author Clemens Hammacher
 */
public abstract class AbstractValue implements Value {

    public boolean canBeInstantiated(Value message) {
        return false;
    }

    public Value insertParameters(List<Parameter> parameters) {
        return this;
    }

    public Value instantiate(List<Value> parameters) {
        return this;
    }

    public Value instantiateInputValue(Value value) {
        return this;
    }

}
