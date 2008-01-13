package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public interface Value {

    String getStringValue();

    /**
     * Replaces all {@link ParameterRefValue}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     */
    Value instantiate(List<Value> parameters);

    /**
     * Replaces all {@link Value}s that occure in the parameter list by
     * corresponding {@link ParameterRefValue}s.
     */
    Value insertParameters(List<Parameter> parameters);

    boolean canBeInstantiated(Value message);

    /**
     * Replaces an InputValue by the corresponding value;
     *
     * @param value
     * @return
     */
    Value instantiateInputValue(Value value);

}
