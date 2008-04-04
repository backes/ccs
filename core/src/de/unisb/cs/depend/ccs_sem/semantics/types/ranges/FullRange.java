package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class FullRange extends AbstractRange {

    private static FullRange instance = new FullRange();

    private FullRange() {
        // private!
    }

    public static FullRange get() {
        return instance;
    }

    // we contain everything...
    public boolean contains(Value value) {
        return true;
    }

    public Collection<ConstantValue> getPossibleValues() {
        // this method is never called, because it is checked that no
        // "not range restricted parameter" get's evaluated at top level
        assert false;

        return null;
    }

    public boolean isRangeRestricted() {
        return false;
    }

    @Override
    public String toString() {
        return "ALL_VALUES";
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 19;
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return obj == this;
    }

}
