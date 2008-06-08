package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class ParameterReference extends AbstractValue {

    private final Parameter param;

    public ParameterReference(Parameter param) {
        this.param = param;
    }

    public String getStringValue() {
        return param.getName();
    }

    public Parameter getParam() {
        return param;
    }

    @Override
    public Value instantiate(Map<Parameter, Value> parameters) {
        final Value myValue = parameters.get(param);
        return myValue == null ? this : myValue;
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return param.hashCode(parameterOccurences);
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParameterReference other = (ParameterReference) obj;
        if (!param.equals(other.param, parameterOccurences))
            return false;
        return true;
    }

}
