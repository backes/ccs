package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public abstract class IntegerValue extends AbstractValue {

    // some specialisations of the methods from Value
    @Override
    public abstract IntegerValue insertParameters(List<Parameter> parameters);

    @Override
    public abstract IntegerValue instantiate(Map<Parameter, Value> parameters);

}
