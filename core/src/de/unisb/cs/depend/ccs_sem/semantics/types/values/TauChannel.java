package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class TauChannel extends AbstractValue implements Channel {

    private static TauChannel instance = new TauChannel();

    private TauChannel() {
        // private
    }

    public static TauChannel get() {
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

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 4711;
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return this == obj;
    }

    public boolean sameChannel(Channel other) {
        return this == other;
    }

}
