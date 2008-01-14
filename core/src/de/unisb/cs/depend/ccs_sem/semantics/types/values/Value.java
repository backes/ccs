package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public interface Value {

    String getStringValue();

    /**
     * Replaces all {@link ParameterRefValue}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     */
    Value instantiate(Map<Parameter, Value> parameters);

    /**
     * Is called in the constructor of a {@link Declaration} (through
     * {@link Expression#insertParameters(List)}.
     * If this value matches one of the parameters, a corresponding {@link ParameterRefValue}
     * is returned, otherwise just the Value itself.
     *
     * @throws ParseException if the types of the parameters to not fit into the expression
     */
    Value insertParameters(List<Parameter> parameters) throws ParseException;

    boolean canBeInstantiated(Value message);

}
