package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


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
            return new ConstStringChannel(str.getValue());
        }
        if (newValue instanceof ParameterReference)
            return new ParameterRefChannel(((ParameterReference)newValue).getParam());
        assert false;
        return null;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 29;
    }

}
