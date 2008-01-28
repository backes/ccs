package de.unisb.cs.depend.ccs_sem.semantics.types.values;



// This is a constant string that can be either a channel or a value
public class ConstString extends AbstractValue implements ConstantValue {

    private final String value;

    public ConstString(String value) {
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
        int hashCode = value.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ConstString other = (ConstString) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
