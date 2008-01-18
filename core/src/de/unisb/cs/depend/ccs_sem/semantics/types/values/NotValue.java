package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class NotValue extends BooleanValue {

    // we use the general type Value here. it could be a BooleanValue or a
    // ParameterRefValue
    private final Value negatedValue;

    private NotValue(Value negatedValue) {
        super();
        this.negatedValue = negatedValue;
    }

    public Value getNegatedValue() {
        return negatedValue;
    }

    public static BooleanValue create(Value negatedValue) {
        if (negatedValue instanceof ConstBooleanValue)
            return ConstBooleanValue.get(!((ConstBooleanValue)negatedValue).getValue());
        if (negatedValue instanceof ParameterRefValue || negatedValue instanceof BooleanValue)
            return new NotValue(negatedValue);
        throw new IllegalArgumentException("Only BooleanValue or ParameterRefValue allowed");
    }

    @Override
    public BooleanValue insertParameters(List<Parameter> parameters) throws ParseException {
        final Value newNegatedValue = negatedValue.insertParameters(parameters);
        if (negatedValue.equals(newNegatedValue))
            return this;
        return new NotValue(newNegatedValue);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newNegatedValue = negatedValue.instantiate(parameters);
        if (negatedValue.equals(newNegatedValue))
            return this;
        return create(newNegatedValue);
    }

    public String getStringValue() {
        final boolean needParenthesis = !(negatedValue instanceof ParameterRefValue
                || negatedValue instanceof ConstBooleanValue);
        return needParenthesis ? "!(" + negatedValue + ")" : "!" + negatedValue;
    }

    public boolean isConstant() {
        return false;
    }

}
