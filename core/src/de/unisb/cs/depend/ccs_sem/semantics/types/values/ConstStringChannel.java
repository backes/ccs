package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;



// This is a constant string that can be either a channel or a value
public class ConstStringChannel extends ConstString implements Channel {

    public ConstStringChannel(String value, boolean isQuoted) {
        super(value, isQuoted);
    }

    @Override
    public Channel instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return super.hashCode(parameterOccurences);
    }

    public boolean sameChannel(Channel other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;
        return getStringValue().equals(other.getStringValue());
    }

}
