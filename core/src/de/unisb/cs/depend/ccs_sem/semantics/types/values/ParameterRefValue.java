package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ParameterRefValue extends AbstractValue {

    private final Parameter param;

    public ParameterRefValue(Parameter param) {
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

    public boolean isConstant() {
        return false;
    }

    @Override
    public int hashCode() {
        return param.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParameterRefValue other = (ParameterRefValue) obj;
        if (param == null) {
            if (other.param != null)
                return false;
        } else if (!param.equals(other.param))
            return false;
        return true;
    }

}
