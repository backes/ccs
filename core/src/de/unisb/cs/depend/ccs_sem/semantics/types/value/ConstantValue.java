package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ConstantValue extends AbstractValue {

    private final String value;

    public ConstantValue(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    @Override
    public Value insertParameters(List<Parameter> parameters) {
        // TODO match the parameters
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

}
