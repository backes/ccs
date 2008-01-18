package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public abstract class BooleanValue extends AbstractValue {

    // some specialisations of the methods from Value
    @Override
    public abstract BooleanValue insertParameters(List<Parameter> parameters) throws ParseException;

    @Override
    public abstract BooleanValue instantiate(Map<Parameter, Value> parameters);

}
