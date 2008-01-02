package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


public class ConstantValue implements Value {
    
    private String value;

    public ConstantValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Value replaceParameters(List<Value> parameters) {
        return this;
    }

    public Value insertParameters(List<Value> parameters) {
        int index = parameters.indexOf(this);
        if (index != -1)
            return new ParameterRefValue(index);

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

    @Override
    public Value clone() {
        Value cloned;
        try {
            cloned = (Value) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalSystemException(getClass().getName() + " could not be cloned");
        }
        
        return cloned;
    }

}
