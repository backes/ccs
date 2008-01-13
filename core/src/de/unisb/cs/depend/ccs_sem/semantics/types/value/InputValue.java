package de.unisb.cs.depend.ccs_sem.semantics.types.value;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


/**
 * A value that is replaced by a read value.
 *
 * @author Clemens Hammacher
 */
public class InputValue extends AbstractValue {

    private final int depth;

    public InputValue() {
        this(0);
    }

    public InputValue(int depth) {
        super();
        this.depth = depth;
    }

    public String getStringValue() {
        // this value is always instantiated before it's value is read
        assert false;
        throw new InternalSystemException();
    }

    @Override
    public Value instantiateInputValue(Value value) {
        if (depth == 0)
            return value;

        return new InputValue(depth - 1);
    }

    // TODO InputValue
    // TODO hashCode & equals

}
