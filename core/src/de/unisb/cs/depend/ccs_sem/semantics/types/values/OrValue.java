package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class OrValue extends BooleanValue {

    // we use the general type Value here. it could be a BooleanValue or a
    // ParameterRefValue
    private final Value left;
    private final Value right;


    private OrValue(Value left, Value right) {
        super();
        this.left = left;
        this.right = right;
    }

    public static BooleanValue create(Value left, Value right) {
        if (left instanceof ConstBooleanValue && right instanceof ConstBooleanValue)
            return ConstBooleanValue.get(((ConstBooleanValue)left).getValue() || ((ConstBooleanValue)right).getValue());
        if ((left instanceof ParameterRefValue || left instanceof BooleanValue)
                && (right instanceof ParameterRefValue || right instanceof BooleanValue))
            return new OrValue(left, right);
        throw new IllegalArgumentException("Only BooleanValue or ParameterRefValue allowed");
    }

    @Override
    public BooleanValue insertParameters(List<Parameter> parameters) throws ParseException {
        final Value newLeft = left.insertParameters(parameters);
        final Value newRight = right.insertParameters(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return new OrValue(left, right);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(left, right);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof OrValue
            || left instanceof IntegerCondValue;
        final boolean needParenthesisRight = right instanceof OrValue
            || right instanceof IntegerCondValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        sb.append(" || ");
        if (needParenthesisRight)
            sb.append('(').append(rightStr).append(')');
        else
            sb.append(rightStr);
        return sb.toString();
    }

    public boolean isConstant() {
        return false;
    }

}
