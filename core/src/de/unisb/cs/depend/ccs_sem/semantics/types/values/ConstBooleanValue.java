package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class ConstBooleanValue extends AbstractValue
    implements BooleanValue, ConstantValue {

    private static ConstBooleanValue trueInstance = new ConstBooleanValue(true);
    private static ConstBooleanValue falseInstance = new ConstBooleanValue(false);

    private final boolean value;

    private ConstBooleanValue(boolean value) {
        super();
        this.value = value;
    }

    public static ConstBooleanValue get(boolean b) {
        return b ? trueInstance : falseInstance;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    public String getStringValue() {
        return Boolean.toString(value);
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return value ? 41*31 : 42*31;
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return this == obj;
    }

}
