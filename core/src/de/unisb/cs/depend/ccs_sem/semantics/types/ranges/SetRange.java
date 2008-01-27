package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class SetRange extends AbstractRange {

    private final Set<ConstantValue> values;

    public SetRange(Set<ConstantValue> values) {
        super();
        this.values = values;
    }

    public Collection<ConstantValue> getPossibleValues() {
        return values;
    }

    public boolean contains(Value value) {
        return values.contains(value);
    }

    public boolean isRangeRestricted() {
        return true;
    }

}
