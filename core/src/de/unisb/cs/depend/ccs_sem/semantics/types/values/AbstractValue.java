package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
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

    // ParseException is thrown in some subclasses
    @SuppressWarnings("unused")
    public Value insertParameters(List<Parameter> parameters) throws ParseException {
        return this;
    }

    public Value instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    public Value instantiateInputValue(Value value) {
        return this;
    }

}
