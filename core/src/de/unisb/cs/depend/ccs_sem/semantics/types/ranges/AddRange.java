package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class AddRange extends AbstractRange {

    private final Range left;
    private final Range right;
    private final boolean isSub;

    public AddRange(AbstractRange left, Range right, boolean isSub) {
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
    public String toString() {
        final boolean rightNeedsParenthesis = right instanceof AddRange;

        final StringBuilder sb = new StringBuilder();
        sb.append(left.toString());
        sb.append(isSub ? " - " : " + ");
        if (rightNeedsParenthesis)
            sb.append('(').append(right.toString()).append(')');
        else
            sb.append(right.toString());

        return sb.toString();
    }

}
