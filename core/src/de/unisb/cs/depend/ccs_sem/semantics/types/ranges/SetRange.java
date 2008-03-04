package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
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
    	List<ConstantValue> possValues = new ArrayList<ConstantValue>(values.size());
    	for (Value val: values) {
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

}
