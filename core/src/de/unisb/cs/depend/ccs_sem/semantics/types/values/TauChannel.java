package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class TauChannel extends AbstractValue implements Channel {

    private static TauChannel instance = null;

    private TauChannel() {
        // private
    }

    public static TauChannel get() {
        if (instance == null)
            instance = new TauChannel();
        return instance;
    }

    public String getStringValue() {
        return "i";
    }

    @Override
    public TauChannel instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    public boolean isConstant() {
        return true;
    }

    @Override
    public String toString() {
        return "i";
    }

    @Override
    public int hashCode() {
        return 4711;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

}
