package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public interface Value extends Cloneable {
    String getValue();

    /**
     * Replaces all {@link ParameterRefValue}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     */
    Value instantiate(List<Value> parameters);

    /**
     * Replaces all {@link Value}s that occure in the parameter list by
     * corresponding {@link ParameterRefValue}s.
     */
    Value insertParameters(List<Value> parameters);

    Value clone();

}
