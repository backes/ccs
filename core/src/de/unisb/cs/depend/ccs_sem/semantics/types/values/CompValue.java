package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;


public class CompValue extends BooleanValue {

    // the types are checked by the parser
    private final Value left;
    private final Value right;
    private final Type type;

    public static enum Type {
        LESS, LEQ, GEQ, GREATER
    }


    private CompValue(Value left, Value right, Type type) {
        super();
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public static BooleanValue create(Value left, Value right, Type type) {
        if (left instanceof ConstIntegerValue && right instanceof ConstIntegerValue) {
            final int leftVal = ((ConstIntegerValue)left).getValue();
            final int rightVal = ((ConstIntegerValue)right).getValue();
            boolean value = false;
            switch (type) {
            case LESS:
                value = leftVal < rightVal;
                break;
            case LEQ:
                value = leftVal <= rightVal;
                break;
            case GEQ:
                value = leftVal >= rightVal;
                break;
            case GREATER:
                value = leftVal > rightVal;
                break;
            default:
                assert false;
                break;
            }
            return ConstBooleanValue.get(value);
        }
        return new CompValue(left, right, type);
    }

    @Override
    public BooleanValue instantiate(Map<Parameter, Value> parameters) {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(newLeft, newRight, type);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof ConditionalValue;
        final boolean needParenthesisRight = right instanceof ConditionalValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        switch (type) {
        case LESS:
            sb.append(" < ");
            break;
        case LEQ:
            sb.append(" <= ");
            break;
        case GEQ:
            sb.append(" >= ");
            break;
        case GREATER:
            sb.append(" > ");
            break;
        default:
            assert false;
            break;
        }
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
        int result = 17;
        result = prime * result + left.hashCode();
        result = prime * result + right.hashCode();
        result = prime * result + type.hashCode();
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
        final CompValue other = (CompValue) obj;
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        if (!type.equals(other.type))
            return false;
        return true;
    }

}
