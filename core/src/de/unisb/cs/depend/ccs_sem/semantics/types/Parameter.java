package de.unisb.cs.depend.ccs_sem.semantics.types;

import de.unisb.cs.depend.ccs_sem.semantics.types.value.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public class Parameter {

    private static enum Type {
        UNKNOWN, CHANNEL, VALUE
    }

    private final Type type = Type.UNKNOWN;
    private final String name;

    public Parameter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return type + "_" + name;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @param value the Value to match this Parameter against
     * @return <code>true</code> iff this parameter can be replaced by this value
     */
    public boolean matches(Value value) {
        switch (type) {
        case UNKNOWN:
            // this parameter is not used, so it always matches
            return true;

        case CHANNEL:
            return (value instanceof ConstantValue);

        case VALUE:
            return (value instanceof IntegerValue);

        default:
            assert false;
            return false;
        }
    }

    // TODO equals / hashCode (wiederverwendung gefährlich, da der typ sich ändert)

}
