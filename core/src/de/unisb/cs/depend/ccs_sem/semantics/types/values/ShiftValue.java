package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class ShiftValue extends AbstractValue implements IntegerValue {

    // the types are checked by the parser
    private final Value left;
    private final Value right;
    private final boolean isRightShift;


    private ShiftValue(Value left, Value right, boolean isRightShift) {
        super();
        this.left = left;
        this.right = right;
        this.isRightShift = isRightShift;
    }

    public static IntegerValue create(Value left, Value right, boolean isRightShift) {
        if (left instanceof ConstIntegerValue && right instanceof ConstIntegerValue) {
            final int leftVal = ((ConstIntegerValue)left).getValue();
            final int rightVal = ((ConstIntegerValue)right).getValue();
            final int value = isRightShift ? (leftVal >> rightVal) : (leftVal << rightVal);
            return new ConstIntegerValue(value);
        }
        return new ShiftValue(left, right, isRightShift);
    }

    @Override
    public IntegerValue instantiate(Map<Parameter, Value> parameters) {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(newLeft, newRight, isRightShift);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof ConditionalValue;
        final boolean needParenthesisRight = right instanceof ConditionalValue
            || right instanceof ShiftValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        sb.append(isRightShift ? " >> " : " << ");
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
        int result = 11;
        result = prime * result + (isRightShift ? 1231 : 1237);
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
        final ShiftValue other = (ShiftValue) obj;
        if (isRightShift != other.isRightShift)
            return false;
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        return true;
    }

}
