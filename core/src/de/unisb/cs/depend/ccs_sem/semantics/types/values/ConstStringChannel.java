package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;



// This is a constant string that can be either a channel or a value
public class ConstStringChannel extends ConstString implements Channel {

    public ConstStringChannel(String value, boolean needsQuotes) {
        super(value, needsQuotes);
    }

    @Override
    public Channel instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode()+23;
    }

}
