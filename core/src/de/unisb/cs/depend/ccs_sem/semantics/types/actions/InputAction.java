package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.InputValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public class InputAction extends Action {

    private final String channel;
    private final Value message;

    public InputAction(String channel, Value message) {
        super();
        this.channel = channel;
        this.message = message;
    }

    @Override
    public String getLabel() {
        final String value = message == null ? "" : message.getStringValue();
        final StringBuilder sb =
                new StringBuilder(channel.length() + 1 + value.length());
        sb.append(channel).append('?').append(value);
        return sb.toString();
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        return message;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        if (!(otherAction instanceof OutputAction))
            return null;

        final OutputAction outAct = (OutputAction) otherAction;

        if (message == null && outAct.getMessage() == null)
            return target;

        if (message instanceof InputValue) {
            final InputValue inputMessage = (InputValue)message;
            if (inputMessage.canBeInstantiated(otherAction.getMessage())) {
                return target.instantiateInputValue(otherAction.getMessage());
            }
        }

        return null;
    }

    @Override
    public Action instantiate(List<Value> parameters) {
        final Value newMessage = message.instantiate(parameters);
        if (message.equals(newMessage))
            return this;
        return Action.getAction(new InputAction(channel, newMessage));
    }

    @Override
    public Action instantiateInputValue(Value value) {
        final Value newMessage = message.instantiateInputValue(value);
        if (newMessage.equals(message))
            return this;
        return Action.getAction(new InputAction(channel, newMessage));
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        if (actionToCheck instanceof InputAction) {
            final InputAction inputActionToCheck = (InputAction) actionToCheck;
            if (channel.equals(inputActionToCheck.channel)
                    && (message == null) == (inputActionToCheck.message == null))
                return true;
        }

        return false;
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) {
        final Value newMessage = message.insertParameters(parameters);
        if (message.equals(newMessage))
            return this;

        return Action.getAction(new InputAction(channel, newMessage));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((channel == null) ? 0 : channel.hashCode());
        result = PRIME * result + ((message == null) ? 0 : message.hashCode());
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
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

}
