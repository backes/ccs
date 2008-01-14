package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
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
        final String channelValue = channel.getStringValue();
        final String paramValue = param != null ? param.getName()
                        : value != null ? value.getStringValue() : null;

        final StringBuilder sb =
                new StringBuilder(channelValue.length() + 1 + paramValue.length());
        sb.append(channelValue).append('?').append(paramValue);
        return sb.toString();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        // TODO is this right? is this method ever called?
        return value;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        if (!(otherAction instanceof OutputAction))
            return null;

        if (!channel.equals(otherAction.getChannel()))
            return null;

        if (param == null) {
            if (value == null ? otherAction.getMessage() != null : value.equals(otherAction.getMessage()))
                return target;

            return null;
        }

        final Map<Parameter, Value> map =
            Collections.singletonMap(param, otherAction.getMessage());
        return target.instantiate(map);
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        final Channel newChannel = channel.instantiate(parameters);
        if (channel.equals(newChannel))
            return this;
        return Action.getAction(new InputAction(newChannel, param));
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        if (actionToCheck instanceof InputAction) {
            final InputAction inputActionToCheck = (InputAction) actionToCheck;
            return (channel.equals(inputActionToCheck.channel)
                    && (param == null) == (inputActionToCheck.param == null));
        }

        return false;
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) throws ParseException {
        final Channel newChannel = channel.insertParameters(parameters);

        if (channel.equals(newChannel))
            return this;

        return Action.getAction(new InputAction(newChannel, param));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + channel.hashCode();
        result = PRIME * result + ((param == null) ? 0 : param.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final InputAction other = (InputAction) obj;
        if (!channel.equals(other.channel))
            return false;
        if (param == null) {
            if (other.param != null)
                return false;
        } else if (!param.equals(other.param))
            return false;
        return true;
    }

}
