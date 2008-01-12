package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


public class ConstantValue implements Value {

    private final String value;

    public ConstantValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Value instantiate(List<Value> parameters) {
        return this;
    }

    public Value insertParameters(List<Value> parameters) {
        final int index = parameters.indexOf(this);
        if (index == -1)
            return this;

        return new ParameterRefValue(index);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ConstantValue other = (ConstantValue) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
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
