package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class NotValue extends BooleanValue {

    // the type is checked by the parser
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
        if (negatedValue instanceof NotValue)
            return (BooleanValue) ((NotValue)negatedValue).getNegatedValue();
        return new NotValue(negatedValue);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newNegatedValue = negatedValue.instantiate(parameters);
        if (negatedValue.equals(newNegatedValue))
            return this;
        return create(newNegatedValue);
    }

    public String getStringValue() {
        final boolean needParenthesis = !(negatedValue instanceof ParameterReference
                || negatedValue instanceof ConstBooleanValue);
        return needParenthesis ? "!(" + negatedValue + ")" : "!" + negatedValue;
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public int hashCode() {
        return 13*31 + negatedValue.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NotValue other = (NotValue) obj;
        if (!negatedValue.equals(other.negatedValue))
            return false;
        return true;
    }

}
