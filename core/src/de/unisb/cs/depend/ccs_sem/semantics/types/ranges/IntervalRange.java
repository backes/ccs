package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class IntervalRange extends AbstractRange {

    private final Value start;
    private final Value end;

    public IntervalRange(Value start, Value end) {
        super();
        this.start = start;
        this.end = end;
    }

    public Collection<ConstantValue> getPossibleValues() {
        // at this time, all parameters must have been instantiated,
        // so start and end must be constant
        assert start instanceof ConstIntegerValue;
        assert end instanceof ConstIntegerValue;

        final int startInt = ((ConstIntegerValue) start).getValue();
        final int endInt = ((ConstIntegerValue) end).getValue();

        final int num = endInt < startInt ? 0 : endInt - startInt + 1;
        final List<ConstantValue> newValues = new ArrayList<ConstantValue>(num);
        for (int i = startInt; i <= endInt; ++i)
            newValues.add(new ConstIntegerValue(i));

        return newValues;
    }

    public boolean contains(Value value) {
        assert start instanceof ConstIntegerValue;
        assert end instanceof ConstIntegerValue;

        if (!(value instanceof ConstIntegerValue))
            return false;

        final int hisValue = ((ConstIntegerValue)value).getValue();

        return (((ConstIntegerValue)start).getValue() <= hisValue
                && ((ConstIntegerValue)end).getValue() >= hisValue);
    }

    public boolean isRangeRestricted() {
        return true;
    }

    @Override
    public Range instantiate(Map<Parameter, Value> parameters) {
        final Value newStart = start.instantiate(parameters);
        final Value newEnd = end.instantiate(parameters);
        if (start.equals(newStart) && end.equals(newEnd))
            return this;

        return new IntervalRange(newStart, newEnd);
    }

    @Override
    public String toString() {
        return start + ".." + end;
    }

}
