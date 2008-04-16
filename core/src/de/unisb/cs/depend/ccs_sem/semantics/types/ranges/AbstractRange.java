package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;



public abstract class AbstractRange implements Range {

    protected AbstractRange() {
        // nothing to do
    }

    public Range add(Range otherRange) {
        return new AddRange(this, otherRange, false);
    }

    public Range subtract(Range otherRange) {
        return new AddRange(this, otherRange, true);
    }

    public Range instantiate(Map<Parameter, Value> parameters) throws ArithmeticError {
        return this;
    }

    @Override
    public String toString() {
        final Collection<ConstantValue> values = getPossibleValues();

        if (values.size() == 0)
            return "[]";

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (final ConstantValue val: values) {
            if (sb.length() > 1)
                sb.append(", ");
            sb.append(val);
        }
        sb.append('}');

        return sb.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        final Map<ParameterOrProcessEqualsWrapper, Integer> emptyMap = Collections.emptyMap();
        // the map is not modified, so we need no HashMap!
        return equals(obj, emptyMap);
    }

    @Override
    public final int hashCode() {
        final Map<ParameterOrProcessEqualsWrapper, Integer> emptyMap = Collections.emptyMap();
        // the map is not modified, so we need no HashMap!
        return hashCode(emptyMap);
    }

}
