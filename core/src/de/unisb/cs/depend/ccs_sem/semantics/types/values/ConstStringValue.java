package de.unisb.cs.depend.ccs_sem.semantics.types.values;



public class ConstStringValue extends AbstractValue implements ConstantValue {

    private final String value;
    private final boolean needsQuotes;

    public ConstStringValue(String value, boolean needsQuotes) {
        this.value = value;
        this.needsQuotes = needsQuotes;
    }

    public String getStringValue() {
        if (needsQuotes)
            return '"' + value + '"';
        return value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hashCode = value.hashCode();
        if (needsQuotes)
            ++hashCode;
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
        final ConstStringValue other = (ConstStringValue) obj;
        if (needsQuotes != other.needsQuotes)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
