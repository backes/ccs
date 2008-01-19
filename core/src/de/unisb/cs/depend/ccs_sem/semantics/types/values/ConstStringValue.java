package de.unisb.cs.depend.ccs_sem.semantics.types.values;



public class ConstStringValue extends AbstractValue implements ConstantValue {

    private final String value;

    public ConstStringValue(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public String getValue() {
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
        final ConstStringValue other = (ConstStringValue) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
