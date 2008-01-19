package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Collections;
import java.util.Map;

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

        final int size = channelValue.length() + 1 +
            (paramValue == null ? 0 : paramValue.length());
        final StringBuilder sb = new StringBuilder(size);
        sb.append(channelValue).append('?');
        if (paramValue != null)
            sb.append(paramValue);
        return sb.toString();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        // TODO is this right? is this method ever called?
        assert false;
        return value;
    }

    public Parameter getParameter() {
        return param;
    }

    @Override
    public boolean isInputAction() {
        return true;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        if (!(otherAction instanceof OutputAction))
            return null;

        if (!channel.equals(otherAction.getChannel()))
            return null;

        if (param == null) {
            if (value == null ? otherAction.getMessage() == null : value.equals(otherAction.getMessage()))
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

        if (value == null) {
            if (channel.equals(newChannel))
                return this;
            return new InputAction(newChannel, value);
        }

        final Value newValue = value.instantiate(parameters);

        if (channel.equals(newChannel) && value.equals(newValue))
            return this;

        return new InputAction(newChannel, newValue);
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        if (actionToCheck instanceof InputAction) {
            final InputAction inputActionToCheck = (InputAction) actionToCheck;
            if (channel.equals(inputActionToCheck.channel)) {
                if (param != null)
                    return true;
                if (value == null || value.equals(inputActionToCheck.value))
                    return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 3;
        result = prime * result + channel.hashCode();
        result = prime * result + ((param == null) ? 0 : param.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
