package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Collections;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class InputAction extends Action {

    private final Channel channel;

    // at most one of these must be initialized
    private final Parameter param;
    private final Value value;

    public InputAction(Channel channel, Parameter param) {
        super();
        this.channel = channel;
        this.param = param;
        this.value = null;
    }

    public InputAction(Channel channel, Value value) {
        super();
        this.channel = channel;
        this.param = null;
        this.value = value;
    }

    @Override
    public String getLabel() {
        final String valueString = param != null ? param.toString()
            : value == null ? null : value.getStringValue();
        return makeLabel(channel.getStringValue(), valueString);
    }

    private String makeLabel(String channelString, String valueString) {
        if (value != null) {
            final boolean noParenthesis = (value instanceof ConstantValue
                    && !(value instanceof ConstIntegerValue
                            && ((ConstIntegerValue)value).getValue() < 0))
                || value instanceof ParameterReference;
            if (!noParenthesis)
                valueString = '(' + valueString + ')';
        }

        final int size = channelString.length() + 1 +
            (valueString == null ? 0 : valueString.length());
        final StringBuilder sb = new StringBuilder(size);
        sb.append(channelString).append('?');
        if (valueString != null)
            sb.append(valueString);
        return sb.toString();
    }

    @Override
    public String toString() {
        final String valueString = param != null ? param.toString()
            : value == null ? null : value.toString();
        return makeLabel(channel.toString(), valueString);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getValue() {
        return value;
    }

    public Parameter getParameter() {
        return param;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // can only synchronize with an output action
        if (!(otherAction instanceof OutputAction))
            return null;

        // the channel have to match
        if (!channel.sameChannel(otherAction.getChannel()))
            return null;

        final Value otherValue = otherAction.getValue();
        if (param == null) {
            if (value == null && otherValue == null)
                return target;
            if  (value != null) {
                if (value.equals(otherValue))
                    return target;
                if (value instanceof ConstString) {
                    final ConstString value2 = new ConstString(value.getStringValue(),
                        !((ConstString)value).isQuoted());
                    if (value2.equals(otherValue))
                        return target;
                }
            }

            return null;
        }

        if (otherValue == null)
            return null;

        // check the parameter range
        final Range range = param.getRange();
        if (range != null && !range.contains(otherValue))
            return null;

        final Map<Parameter, Value> map =
            Collections.singletonMap(param, otherValue);
        return target.instantiate(map);
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) throws ArithmeticError {
        final Channel newChannel = channel.instantiate(parameters);

        if (value == null) {
            if (param == null) {
                if (channel.equals(newChannel))
                    return this;
                return new InputAction(newChannel, param);
            }
            final Parameter newParam = param.instantiate(parameters);
            if (channel.equals(newChannel) && newParam.equals(param))
                return this;
            return new InputAction(newChannel, newParam);
        }

        final Value newValue = value.instantiate(parameters);

        if (channel.equals(newChannel) && value.equals(newValue))
            return this;

        return new InputAction(newChannel, newValue);
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int prime = 31;
        int result = 1;
        result = prime * result + channel.hashCode(parameterOccurences);
        result = prime * result + (param == null ? 0 : param.hashCode(parameterOccurences));
        result = prime * result + (value == null ? 0 : value.hashCode(parameterOccurences));
        return result;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final InputAction other = (InputAction) obj;
        if (!channel.equals(other.channel, parameterOccurences))
            return false;
        if (param == null) {
            if (other.param != null)
                return false;
        } else if (!param.equals(other.param, parameterOccurences))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value, parameterOccurences))
            return false;
        return true;
    }

}
