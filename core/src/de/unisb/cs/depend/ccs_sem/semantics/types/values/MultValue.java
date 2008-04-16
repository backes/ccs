package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class MultValue extends AbstractValue implements IntegerValue {

    // the types are checked by the parser
    private final Value left;
    private final Value right;
    private final Type type;

    public static enum Type {
        MULT, DIV, MOD
    }


    private MultValue(Value left, Value right, Type type) {
        super();
        this.left = left;
        this.right = right;
        this.type = type;
    }

    /**
     * Creates a new MultValue. If the parameters are constant, the value is
     * computed and a new {@link ConstIntegerValue} is returned.
     *
     * @param left the left operand
     * @param right the right operand
     * @param type the type of the operation (MULT, DIV or MOD)
     * @return a new {@link MultValue} or a new {@link ConstIntegerValue}
     * @throws ArithmeticError if type is DIV or MOD and the right operand is 0
     */
    public static IntegerValue create(Value left, Value right, Type type) throws ArithmeticError {
        if (left instanceof ConstIntegerValue && right instanceof ConstIntegerValue) {
            final int leftVal = ((ConstIntegerValue)left).getValue();
            final int rightVal = ((ConstIntegerValue)right).getValue();
            int value = 0;
            switch (type) {
            case MULT:
                value = leftVal * rightVal;
                break;
            case DIV:
                if (rightVal == 0)
                    throw new ArithmeticError("Division by zero");
                value = leftVal / rightVal;
                break;
            case MOD:
                if (rightVal == 0)
                    throw new ArithmeticError("Division by zero (in modulo operator)");
                value = leftVal % rightVal;
                break;
            default:
                assert false;
                break;
            }
            return new ConstIntegerValue(value);
        }
        return new MultValue(left, right, type);
    }

    @Override
    public IntegerValue instantiate(Map<Parameter, Value> parameters) throws ArithmeticError {
        final Value newLeft = left.instantiate(parameters);
        final Value newRight = right.instantiate(parameters);
        if (left.equals(newLeft) && right.equals(newRight))
            return this;
        return create(newLeft, newRight, type);
    }

    public String getStringValue() {
        final boolean needParenthesisLeft = left instanceof ConditionalValue
            || left instanceof ShiftValue || left instanceof AddValue;
        final boolean needParenthesisRight = right instanceof ConditionalValue
            || right instanceof ShiftValue || right instanceof AddValue
            || right instanceof MultValue;
        final String leftStr = left.toString();
        final String rightStr = right.toString();
        final StringBuilder sb = new StringBuilder(leftStr.length() + rightStr.length() + 6);
        if (needParenthesisLeft)
            sb.append('(').append(leftStr).append(')');
        else
            sb.append(leftStr);
        switch (type) {
        case MULT:
            sb.append(" * ");
            break;
        case DIV:
            sb.append(" / ");
            break;
        case MOD:
            sb.append(" % ");
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

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int prime = 31;
        int result = 1;
        result = prime * result + left.hashCode(parameterOccurences);
        result = prime * result + right.hashCode(parameterOccurences);
        result = prime * result + type.hashCode();
        return result;
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MultValue other = (MultValue) obj;
        if (!type.equals(other.type))
            return false;
        if (!left.equals(other.left, parameterOccurences))
            return false;
        if (!right.equals(other.right, parameterOccurences))
            return false;
        return true;
    }

}
