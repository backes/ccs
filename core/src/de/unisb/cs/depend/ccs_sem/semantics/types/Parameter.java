package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.BooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstStringValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


/**
 * Represents a Parameter of a recursion variable.
 *
 * @author Clemens Hammacher
 */
public class Parameter {

    // TODO when instantiating a parameter, it has to be copied (i think)

    public static enum Type {
        UNKNOWN, CHANNEL, VALUE, STRINGVALUE, BOOLEANVALUE, INTEGERVALUE
    }

    // the type is determined while Value.insertParameters() and
    // UnknownString.replaceRecursion
    private Type type = Type.UNKNOWN;
    private final String name;
    private List<Parameter> connectedParameters = null;

    public Parameter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    /**
     * Tries to match this parameter with the given Value.
     * If this Parameter has UNKNOWN type, then the type is set according to the
     * value.
     * If the type is different from UNKNOWN and the type of the value does
     * not suit, the type is left unchanged, and a ParseException is thrown.
     *
     * @param value the Value to match this Parameter with
     * @throws ParseException if this parameter cannot be replaced by the given value
     */
    public void match(Value value) throws ParseException {
        if (value instanceof ParameterRefValue) {
            final ParameterRefValue paramValue = (ParameterRefValue) value;
            final Type otherType = paramValue.getParam().type;
            switch (otherType) {
            case UNKNOWN:
                addConnectedParameter(paramValue.getParam());
                paramValue.getParam().addConnectedParameter(this);
                break;
            case CHANNEL:
            case VALUE:
            case BOOLEANVALUE:
            case INTEGERVALUE:
            case STRINGVALUE:
                setType(otherType);
                break;
            default:
                assert false;
                break;
            }
        } else if (value instanceof Channel) {
            setType(Type.CHANNEL);
        } else if (value instanceof BooleanValue) {
            setType(Type.BOOLEANVALUE);
        } else if (value instanceof IntegerValue) {
            setType(Type.INTEGERVALUE);
        } else if (value instanceof ConstStringValue) {
            setType(Type.STRINGVALUE);
        } else {
            // TODO does this occure?
            setType(Type.VALUE);
        }
    }

    /**
     * Tries to set a new type, which has to be more specific than the old one.
     * Otherwise (if they clash), a ParseException is thrown.
     * @param newType the new type to set this parameter to.
     * @throws ParseException if the old type and the new type don't fit together
     */
    public void setType(Type newType) throws ParseException {
        assert newType != Type.UNKNOWN;
        if (type == newType)
            return;

        switch (type) {
        case UNKNOWN:
            // we accept every type
            break;
        case CHANNEL:
            // a channel is a channel and stays a channel...
            throw new ParseException("Parameter already has type " + type + ", cannot be used as " + newType);
        case VALUE:
            switch (newType) {
            case BOOLEANVALUE:
            case INTEGERVALUE:
            case STRINGVALUE:
                // accept
                break;

            default:
                throw new ParseException("Parameter already has type " + type + ", cannot be used as " + newType);
            }
        case BOOLEANVALUE:
        case INTEGERVALUE:
        case STRINGVALUE:
            if (newType != Type.VALUE)
                throw new ParseException("Parameter already has type " + type + ", cannot be used as " + newType);
        default:
            assert false;
        }
        type = newType;
        if (connectedParameters != null) {
            // work on a copy of connectedParameters, otherwise we would get a loop
            final List<Parameter> parametersToSetType = connectedParameters;
            connectedParameters = null;
            for (final Parameter otherParam: parametersToSetType)
                otherParam.setType(newType);
        }
    }

    private void addConnectedParameter(Parameter param) {
        if (connectedParameters == null)
            connectedParameters = new ArrayList<Parameter>(2);
        connectedParameters.add(param);
    }

    // hashCode and equals are not overridden (only identical Parameters are equal)

}
