package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ConstIntegerValue extends AbstractValue
    implements IntegerValue, ConstantValue {

    private final int value;

    public ConstIntegerValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        return Integer.toString(value);
    }

    @Override
    public IntegerValue instantiate(Map<Parameter, Value> parameters) {
        return this;
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
        final ConstIntegerValue other = (ConstIntegerValue) obj;
        if (value != other.value)
            return false;
        return true;
    }

}
