package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class IntegerValue extends AbstractValue {

    private final int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        return Integer.toString(value);
    }

    @Override
    public Value insertParameters(List<Parameter> parameters) {
        final int index = parameters.indexOf(this);
        if (index == -1)
            return this;

        return new ParameterRefValue(index);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IntegerValue other = (IntegerValue) obj;
        if (value != other.value)
            return false;
        return true;
    }

}
