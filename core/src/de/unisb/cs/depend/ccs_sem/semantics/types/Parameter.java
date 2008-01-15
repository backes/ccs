package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


/**
 * Represents a Parameter of a recursion variable.
 *
 * @author Clemens Hammacher
 */
public class Parameter {

    // TODO when instantiating a parameter, it has to be copied (i think)

    private static enum Type {
        UNKNOWN, CHANNEL, VALUE
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
     * If the names match, the types are compared.
     * If this Parameter has UNKNOWN type, then the type is set according to the
     * value.
     * If the type is different from UNKNOWN and the type of the value does
     * not suit, the type is left unchanged, and a ParseException is thrown.
     *
     * @param value the Value to match this Parameter with
     * @throws ParseException if this parameter cannot be replaced by the given value
     */
    public void match(Value value) throws ParseException {
        // TODO
        switch (type) {
        case UNKNOWN:
            if (value instanceof ParameterRefValue) {
                final ParameterRefValue paramValue = (ParameterRefValue) value;
                switch (paramValue.getParam().type) {
                case CHANNEL:
                    setType(Type.CHANNEL);
                    break;
                case VALUE:
                    setType(Type.VALUE);
                    break;
                case UNKNOWN:
                    addConnectedParameter(paramValue.getParam());
                    paramValue.getParam().addConnectedParameter(this);
                    break;
                default:
                    assert false;
                    break;
                }
            } else if (value instanceof Channel) {
                setType(Type.CHANNEL);
            } else {
                setType(Type.VALUE);
            }
            break;

        case CHANNEL:
            if (value instanceof Channel)
                break;
            throw new ParseException("This parameter represents a channel, no value");

        case VALUE:
            if (value instanceof Channel)
                throw new ParseException("This parameter represents a value, no channel");
            break;

        default:
            assert false;
            break;
        }
    }

    private void setType(Type newType) throws ParseException {
        if (type == newType)
            return;

        switch (type) {
        case UNKNOWN:
            break;
        case CHANNEL:
        case VALUE:
            throw new ParseException("This parameter already has type " + type);
        default:
            assert false;
        }
        type = newType;
        if (connectedParameters != null)
            for (final Parameter otherParam: connectedParameters)
                otherParam.setType(newType);
    }

    private void addConnectedParameter(Parameter param) {
        if (connectedParameters == null)
            connectedParameters = new ArrayList<Parameter>(2);
        connectedParameters.add(param);
    }

    // hashCode and equals are not overridden (only identical Parameters are equal)

}
