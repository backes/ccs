package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.IntervalRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.BooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConditionalValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


/**
 * Represents a Parameter of a recursion variable.
 *
 * @author Clemens Hammacher
 */
public class Parameter {

    public static enum Type {
        UNKNOWN("Unknown / unused parameter"),
        CHANNEL("Channel parameter"),
        VALUE("Value parameter"),
        STRINGVALUE("String value parameter"),
        BOOLEANVALUE("Boolean value parameter"),
        INTEGERVALUE("Integer value parameter"),
        STRING("String (value/channel) parameter");

        private String desc;

        private Type(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    // the type is determined while parsing, Value.insertParameters() and
    // UnknownString.replaceRecursion
    private Type type = Type.UNKNOWN;
    private final String name;
    private List<Parameter> connectedParameters = null;
    private final Range range;

    public Parameter(String name) {
        this(name, null);
    }

    public Parameter(String name, Range range) {
        this.name = name;
        this.range = range;
    }

    @Override
    public String toString() {
        if (range == null)
            return name;

        if (range instanceof IntervalRange || range instanceof SetRange)
            return name + ":" + range;

        return name + ":(" + range + ")";
    }

    public String getName() {
        return name;
    }

    public Range getRange() {
        return range;
    }

    /**
     * Tries to match this parameter with the given Value.
     * If this Parameter has UNKNOWN type, then the type is set according to the
     * value.
     * If the type is different from UNKNOWN and the type of the value does
     * not suit, the type is left unchanged, and a ParseException is thrown.
     *
     * @param value the Value to match this Parameter with
     * @throws ParseException if this parameter cannot be instantiated with the given value
     */
    public void match(Value value) throws ParseException {
        if (value instanceof ParameterReference) {
            final ParameterReference paramValue = (ParameterReference) value;
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
            case STRING:
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
        } else if (value instanceof ConstString) {
            setType(Type.STRING);
        } else if (value instanceof ConditionalValue) {
            final ConditionalValue cond = (ConditionalValue) value;
            match(cond.getThenValue());
            match(cond.getElseValue());
        } else {
            // we should never get to here
            assert false;
        }
    }

    /**
     * Tries to set a new type, which has suit the old one.
     * Otherwise a ParseException is thrown.
     * @param newType the new type to set this parameter to
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
            if (newType == Type.STRING)
                // do not change
                return;
            throw new ParseException("Parameter already has type \"" + type + "\", cannot be instantiated with \"" + newType + "\"");
        case VALUE:
            switch (newType) {
            case STRING:
                newType = Type.STRINGVALUE;
                break;
            case BOOLEANVALUE:
            case INTEGERVALUE:
            case STRINGVALUE:
                // accept
                break;

            default:
                throw new ParseException("Parameter already has type \"" + type + "\", cannot be instantiated with \"" + newType + "\"");
            }

        case STRINGVALUE:
            if (newType == Type.STRING)
                return;
            // fall through here!
        case BOOLEANVALUE:
        case INTEGERVALUE:
            if (newType != Type.VALUE)
                throw new ParseException("Parameter already has type \"" + type + "\", cannot be instantiated with \"" + newType + "\"");
        case STRING:
            if (newType == Type.CHANNEL || newType == Type.STRINGVALUE)
                // accept
                break;
            throw new ParseException("Parameter already has type \"" + type + "\", cannot be instantiated with \"" + newType + "\"");
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
            connectedParameters = parametersToSetType;
        }
    }

    private void addConnectedParameter(Parameter param) {
        if (connectedParameters == null)
            connectedParameters = new ArrayList<Parameter>(2);
        connectedParameters.add(param);
    }

    public Parameter instantiate(Map<Parameter, Value> parameters) {
        if (range == null)
            return this;

        final Range newRange = range.instantiate(parameters);
        if (range.equals(newRange))
            return this;
        return new Parameter(name, newRange);
    }

    public Type getType() {
        return type;
    }

    // hashCode and equals are not overridden (only identical Parameters are equal)

}
