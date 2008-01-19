package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class EqValue extends BooleanValue {

    // the types are checked by the parser
    private final Value left;
    private final Value right;
    private final boolean isNegated;


    private EqValue(Value left, Value right, boolean isNegated) {
        super();
        this.left = left;
        this.right = right;
        this.isNegated = isNegated;
    }

    public static BooleanValue create(Value left, Value right, boolean isNegated) {
        if (left instanceof ConstantValue && right instanceof ConstantValue) {
            boolean value = ((ConstantValue)left).equals(right);
            if (isNegated)
                value = !value;
            return ConstBooleanValue.get(value);
        }
        return new EqValue(left, right, isNegated);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(newLeft, newRight, isNegated);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof OrValue
            || left instanceof ConditionalValue || left instanceof AndValue;
        final boolean needParenthesisRight = right instanceof OrValue
            || right instanceof ConditionalValue || right instanceof AndValue
            || right instanceof EqValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        sb.append(isNegated ? " != " : " == ");
        if (needParenthesisRight)
            sb.append('(').append(rightStr).append(')');
        else
            sb.append(rightStr);
        return sb.toString();
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 15;
        result = prime * result + (isNegated ? 1231 : 1237);
        result = prime * result + left.hashCode();
        result = prime * result + right.hashCode();
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
        final EqValue other = (EqValue) obj;
        if (isNegated != other.isNegated)
            return false;
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        return true;
    }

}
