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

}