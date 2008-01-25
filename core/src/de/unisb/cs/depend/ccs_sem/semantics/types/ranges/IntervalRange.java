package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class IntervalRange extends AbstractRange {

    private final int start;
    private final int end;

    public IntervalRange(int start, int endValue) {
        super();
        this.start = start;
        this.end = endValue;
    }

    public Collection<ConstantValue> getPossibleValues() {
        final List<ConstantValue> newValues = new ArrayList<ConstantValue>(end-start+1);
        for (int i = start; i <= end; ++i)
            newValues.add(new ConstIntegerValue(i));

        return newValues;
    }

    public boolean contains(Value value) {
        if (!(value instanceof ConstIntegerValue))
            return false;

        final int hisValue = ((ConstIntegerValue)value).getValue();

        return (start <= hisValue && end >= hisValue);
    }

}
