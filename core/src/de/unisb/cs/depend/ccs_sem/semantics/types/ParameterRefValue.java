package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


public class ParameterRefValue implements Value {

    private final int paramNr;

    public ParameterRefValue(int paramNr) {
        this.paramNr = paramNr;
    }

    public String getValue() {
        return "param#" + paramNr;
    }

    public Value instantiate(List<Value> parameters) {
        assert parameters.size() > paramNr;

        return parameters.get(paramNr);
    }

    public Value insertParameters(List<Value> parameters) {
        return this;
    }

    @Override
    public String toString() {
        return getValue();
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

    @Override
    public Value clone() {
        Value cloned;
        try {
            cloned = (Value) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalSystemException(getClass().getName() + " could not be cloned");
        }

        return cloned;
    }

}
