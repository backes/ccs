package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class SetRange extends AbstractRange {

    private final Set<Value> values;

    public SetRange(Set<Value> values) {
        super();
        this.values = values;
    }

    public Collection<ConstantValue> getPossibleValues() {
        final Set<ConstantValue> newValues = new TreeSet<ConstantValue>();
        for (final Value value: values) {
            assert value instanceof ConstantValue;
            newValues.add((ConstantValue)value);
        }

        return newValues;
    }

    public boolean contains(Value value) {
        return values.contains(value);
    }

}
