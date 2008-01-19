package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ConditionalValue extends AbstractValue {

    // the types are checked by the parser
    private final Value condition;
    private final Value thenValue;
    private final Value elseValue;

    private ConditionalValue(Value condition, Value thenValue, Value elseValue) {
        super();
        this.condition = condition;
        this.thenValue = thenValue;
        this.elseValue = elseValue;
    }

    public static Value create(Value condition, Value thenValue, Value elseValue) {
        if (condition instanceof ConstBooleanValue)
            return ((ConstBooleanValue)condition).getValue() ? thenValue : elseValue;
        return new ConditionalValue(condition, thenValue, elseValue);
    }

    public Value getCondition() {
        return condition;
    }

    public Value getThenValue() {
        return thenValue;
    }

    public Value getElseValue() {
        return elseValue;
    }

    @Override
    public Value instantiate(Map<Parameter, Value> parameters) {
        final Value newCondition = condition.instantiate(parameters);
        final Value newThenValue = thenValue.instantiate(parameters);
        final Value newElseValue = elseValue.instantiate(parameters);

        if (condition.equals(newCondition) && thenValue.equals(newThenValue)
                && elseValue.equals(newElseValue))
            return this;

        return create(newCondition, newThenValue, newElseValue);
    }

    public String getStringValue() {
        final String conditionString = condition.toString();
        final String thenString = thenValue.toString();
        final String elseString = elseValue.toString();

        final StringBuilder sb = new StringBuilder(conditionString.length() + thenString.length()
            + elseString.length() + 6);
        sb.append(conditionString).append(" ? ").append(thenString).append(" : ").append(elseString);
        return sb.toString();
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 16;
        result = prime * result + condition.hashCode();
        result = prime * result + elseValue.hashCode();
        result = prime * result + thenValue.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ConditionalValue other = (ConditionalValue) obj;
        if (!condition.equals(other.condition))
            return false;
        if (!elseValue.equals(other.elseValue))
            return false;
        if (!thenValue.equals(other.thenValue))
            return false;
        return true;
    }

}
