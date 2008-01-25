package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public interface Value extends Comparable<Value> {

    String getStringValue();

    /**
     * Replaces all {@link ParameterReference}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     */
    Value instantiate(Map<Parameter, Value> parameters);

}
