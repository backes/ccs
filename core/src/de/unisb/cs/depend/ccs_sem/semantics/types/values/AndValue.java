package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class AndValue extends AbstractValue implements BooleanValue {

    // the types are checked by the parser
    private final Value left;
    private final Value right;


    private AndValue(Value left, Value right) {
        super();
        this.left = left;
        this.right = right;
    }

    public static BooleanValue create(Value left, Value right) {
        if (left instanceof ConstBooleanValue && right instanceof ConstBooleanValue)
            return ConstBooleanValue.get(((ConstBooleanValue)left).getValue() && ((ConstBooleanValue)right).getValue());
        return new AndValue(left, right);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(newLeft, newRight);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof OrValue
            || left instanceof ConditionalValue;
        final boolean needParenthesisRight = right instanceof OrValue
            || right instanceof ConditionalValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        sb.append(" && ");
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
        int result = 18;
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
        final AndValue other = (AndValue) obj;
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        return true;
    }

}
