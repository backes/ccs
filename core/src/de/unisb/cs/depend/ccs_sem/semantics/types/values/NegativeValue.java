package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class NegativeValue extends AbstractValue implements IntegerValue {

    // the type is checked by the parser
    private final Value negativeValue;

    private NegativeValue(Value negativeValue) {
        super();
        this.negativeValue = negativeValue;
    }

    public Value getNegativeValue() {
        return negativeValue;
    }

    public static Value create(Value negativeValue) {
        if (negativeValue instanceof ConstIntegerValue)
            return new ConstIntegerValue(-((ConstIntegerValue)negativeValue).getValue());
        if (negativeValue instanceof NegativeValue)
            return ((NegativeValue)negativeValue).getNegativeValue();
        return new NegativeValue(negativeValue);
    }

    @Override
    public Value instantiate(Map<Parameter, Value> parameters) {
        final Value newNegativeValue = negativeValue.instantiate(parameters);
        if (negativeValue.equals(newNegativeValue))
            return this;
        return create(newNegativeValue);
    }

    public String getStringValue() {
        final boolean needParenthesis = !(negativeValue instanceof ParameterReference
                || negativeValue instanceof ConstBooleanValue);
        return needParenthesis ? "-(" + negativeValue + ")" : "-" + negativeValue;
    }

    public boolean isConstant() {
        return false;
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 17*31 + negativeValue.hashCode(parameterOccurences);
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NegativeValue other = (NegativeValue) obj;
        if (!negativeValue.equals(other.negativeValue, parameterOccurences))
            return false;
        return true;
    }

}
