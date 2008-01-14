package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
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
    public Value insertParameters(List<Parameter> parameters) throws ParseException {
        for (final Parameter param: parameters) {
            if (param.getName().equals(value)) {
                // throws ParseException if they don't match
                param.match(this);

                return new ParameterRefValue(param);
            }
        }

        return this;
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
