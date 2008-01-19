package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public abstract class IntegerValue extends AbstractValue {

    // specialisations of the methods from Value
    @Override
    public abstract IntegerValue instantiate(Map<Parameter, Value> parameters);

}
