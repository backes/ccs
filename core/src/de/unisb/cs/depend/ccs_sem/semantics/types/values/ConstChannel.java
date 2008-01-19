package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ConstChannel extends AbstractValue implements Channel, ConstantValue {

    private final String value;

    public ConstChannel(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public ConstChannel instantiate(Map<Parameter, Value> parameters) {
        return this;
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
        final ConstChannel other = (ConstChannel) obj;
        if (!value.equals(other.value))
            return false;
        return true;
    }

}
