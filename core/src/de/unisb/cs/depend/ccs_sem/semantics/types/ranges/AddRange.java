package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class AddRange extends AbstractRange {

    private final Range left;
    private final Range right;
    private final boolean isSub;

    public AddRange(Range left, Range right, boolean isSub) {
        super();
        this.left = left;
        this.right = right;
        this.isSub = isSub;
    }

    public Collection<ConstantValue> getPossibleValues() {
        final Collection<ConstantValue> valuesLeft = left.getPossibleValues();
        final Collection<ConstantValue> valuesRight = right.getPossibleValues();

        final Set<ConstantValue> newValues = new TreeSet<ConstantValue>(valuesLeft);
        if (isSub)
            newValues.removeAll(valuesRight);
        else
            newValues.addAll(valuesRight);

        return Collections.unmodifiableCollection(newValues);
    }

    public boolean contains(Value value) {
        if (isSub)
            return left.contains(value) && !right.contains(value);
        else
            return left.contains(value) || right.contains(value);
    }

    public boolean isRangeRestricted() {
        if (!left.isRangeRestricted())
            return false;

        if (!isSub && !right.isRangeRestricted())
            return false;

        return true;
    }

    @Override
    public Range instantiate(Map<Parameter, Value> parameters) {
        final Range newLeft = left.instantiate(parameters);
        final Range newRight = right.instantiate(parameters);
        if (newLeft.equals(left) && newRight.equals(right))
            return this;
        return new AddRange(newLeft, newRight, isSub);
    }

    @Override
    public String toString() {
        final boolean leftNeedsParenthesis = left instanceof IntervalRange;
        final boolean rightNeedsParenthesis = right instanceof AddRange
            || right instanceof IntervalRange;

        final StringBuilder sb = new StringBuilder();
        if (leftNeedsParenthesis)
            sb.append('(').append(left.toString()).append(')');
        else
            sb.append(left.toString());
        sb.append(isSub ? " - " : " + ");
        if (rightNeedsParenthesis)
            sb.append('(').append(right.toString()).append(')');
        else
            sb.append(right.toString());

        return sb.toString();
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isSub ? 1231 : 1237);
        result = prime * result + left.hashCode(parameterOccurences);
        result = prime * result + right.hashCode(parameterOccurences);
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
        final AddRange other = (AddRange) obj;
        if (isSub != other.isSub)
            return false;
        if (!left.equals(other.left, parameterOccurences))
            return false;
        if (!right.equals(other.right, parameterOccurences))
            return false;
        return true;
    }


}
