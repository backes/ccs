package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public interface Value extends Comparable<Value> {

    String getStringValue();

    /**
     * Replaces all {@link ParameterReference}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     *
     * Typically delegates to its subterms.
     * @param parameters the parameters to replace by concrete values
     * @return either <code>this</code> or a new created Value
     * @throws ArithmeticError if a division by zero occures

     */
    Value instantiate(Map<Parameter, Value> parameters) throws ArithmeticError;

    int hashCode(Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences);

    boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper,Integer> parameterOccurences);

}
