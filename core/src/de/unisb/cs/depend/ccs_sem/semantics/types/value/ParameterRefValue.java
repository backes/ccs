package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import java.util.List;


public class ParameterRefValue extends AbstractValue {

    private final int paramNr;

    public ParameterRefValue(int paramNr) {
        this.paramNr = paramNr;
    }

    public String getStringValue() {
        return "param#" + paramNr;
    }

    @Override
    public Value instantiate(List<Value> parameters) {
        assert parameters.size() > paramNr;

        return parameters.get(paramNr);
    }

    @Override
    public String toString() {
        return getStringValue();
    }

    @Override
    public int hashCode() {
        return paramNr;
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
        if (paramNr != other.paramNr)
            return false;
        return true;
    }

}
