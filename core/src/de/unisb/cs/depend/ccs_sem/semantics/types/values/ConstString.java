package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;



// This is a constant string that can be either a channel or a value
public class ConstString extends AbstractValue implements ConstantValue {

    private final String value;
    private final boolean isQuoted;

    public ConstString(String value, boolean isQuoted) {
        this.value = value;
        this.isQuoted = isQuoted;
    }

    public String getStringValue() {
        return value;
    }

    @Override
    public String toString() {
        if (!isQuoted)
            return value;
        final StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"').append(value).append('"');
        return sb.toString();
    }

    public boolean isQuoted() {
        return isQuoted;
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isQuoted ? 1231 : 1237);
        result = prime * result + value.hashCode();
        return result;
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ConstString other = (ConstString) obj;
        if (isQuoted != other.isQuoted)
            return false;
        if (!value.equals(other.value))
            return false;
        return true;
    }

}
