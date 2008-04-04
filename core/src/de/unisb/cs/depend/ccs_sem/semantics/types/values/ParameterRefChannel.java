package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class ParameterRefChannel extends ParameterReference implements Channel {

    public ParameterRefChannel(Parameter param) {
        super(param);
    }

    @Override
    public Channel instantiate(Map<Parameter, Value> parameters) {
        final Value newValue = super.instantiate(parameters);
        if (newValue instanceof Channel)
            return (Channel)newValue;
        if (newValue instanceof ConstString) {
            final ConstString str = (ConstString)newValue;
            return new ConstStringChannel(str.getStringValue(), str.isQuoted());
        }
        if (newValue instanceof ParameterReference)
            return new ParameterRefChannel(((ParameterReference)newValue).getParam());

        // one of the things above must have been true
        assert false;
        return null;
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return super.hashCode(parameterOccurences) + 29;
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
