package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class SetRange extends AbstractRange {

    private final Set<Value> values;

    public SetRange(Set<Value> rangeValues) {
        super();
        this.values = rangeValues;
    }

    /**
     * At the time that this method is called, there should be no more parameters
     * in the range, so we return ConstantValues.
     *
     * @return all values within this range
     */
    public Collection<ConstantValue> getPossibleValues() {
        final List<ConstantValue> possValues = new ArrayList<ConstantValue>(values.size());
        for (final Value val: values) {
            if (val instanceof ConstantValue)
                possValues.add((ConstantValue)val);
            else
                throw new InternalSystemException("range still contains non-constant values");
        }
        return possValues;
    }

    public boolean contains(Value value) {
        return values.contains(value);
    }

    public boolean isRangeRestricted() {
        return true;
    }

    @Override
    public Range instantiate(Map<Parameter, Value> parameters) {
        final Set<Value> newSet = new TreeSet<Value>();
        boolean changed = false;
        for (final Value val: values) {
            final Value newVal = val.instantiate(parameters);
            changed |= !val.equals(newVal);
            newSet.add(newVal);
        }

        if (!changed)
            return this;

        return new SetRange(newSet);
    }

    @Override
    public String toString() {
        if (values.size() == 0)
            return "[]";

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (final Value val: values) {
            if (sb.length() > 1)
                sb.append(", ");
            sb.append(val);
        }
        sb.append('}');

        return sb.toString();
    }
}
