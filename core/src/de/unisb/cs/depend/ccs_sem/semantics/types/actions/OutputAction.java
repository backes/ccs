package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class OutputAction extends Action {

    private final Channel channel;
    private final Value value;

    public OutputAction(Channel channel, Value value) {
        super();
        this.channel = channel;
        this.value = value;
    }

    @Override
    public String getLabel() {
        final String valueString = value == null ? null : value.getStringValue();
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
        sb.append(channelString).append('!');
        if (valueString != null)
            sb.append(valueString);
        return sb.toString();
    }

    @Override
    public String toString() {
        final String valueString = value == null ? null : value.toString();
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

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // this method should not be called on an output action
        assert false;

        return null;
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        final Channel newChannel = channel.instantiate(parameters);

        if (value == null) {
            if (channel.equals(newChannel))
                return this;
            return new OutputAction(newChannel, null);
        }

        final Value newValue = value.instantiate(parameters);

        if (channel.equals(newChannel) && value.equals(newValue))
            return this;

        return new OutputAction(newChannel, newValue);
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + channel.hashCode(parameterOccurences);
        result = PRIME * result + (value == null ? 0 : value.hashCode(parameterOccurences));
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
        final OutputAction other = (OutputAction) obj;
        if (!channel.equals(other.channel, parameterOccurences))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value, parameterOccurences))
            return false;
        return true;
    }

}
