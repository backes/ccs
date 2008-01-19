package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ParameterRefChannel extends AbstractValue implements Channel {

    private final Parameter param;

    public ParameterRefChannel(Parameter param) {
        this.param = param;
    }

    public String getStringValue() {
        return param.getName();
    }

    public Parameter getParam() {
        return param;
    }

    @Override
    public Channel instantiate(Map<Parameter, Value> parameters) {
        final Value myValue = parameters.get(param);

        if (myValue == null)
            return this;

        assert myValue instanceof Channel;

        return (Channel) myValue;
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return getStringValue();
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
        final ParameterRefChannel other = (ParameterRefChannel) obj;
        if (param == null) {
            if (other.param != null)
                return false;
        } else if (!param.equals(other.param))
            return false;
        return true;
    }

}
