package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class OutputAction extends Action {

    private final Channel channel;
    private final Value message;

    public OutputAction(Channel channel, Value message) {
        super();
        this.channel = channel;
        this.message = message;
    }

    @Override
    public String getLabel() {
        final String channelValue = channel.getStringValue();
        final String messageValue = message == null ? null : message.getStringValue();

        final int size = channelValue.length() + 1 +
            (messageValue == null ? 0 : messageValue.length());
        final StringBuilder sb = new StringBuilder(size);
        sb.append(channelValue).append('!');
        if (messageValue != null)
            sb.append(messageValue);
        return sb.toString();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        return message;
    }

    @Override
    public boolean isOutputAction() {
        return true;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // this method should not be called on an output action
        assert false;

        return null;
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        if (actionToCheck instanceof OutputAction) {
            final OutputAction outputActionToCheck = (OutputAction) actionToCheck;
            if (channel.equals(outputActionToCheck.channel)) {
                // TODO distinguish parameters / values
                if (message == null || message.equals(outputActionToCheck.message))
                    return true;
            }
        }

        return false;
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        final Channel newChannel = channel.instantiate(parameters);

        if (message == null) {
            if (channel.equals(newChannel))
                return this;
            return Action.getAction(new OutputAction(newChannel, null));
        }

        final Value newMessage = message.instantiate(parameters);

        if (channel.equals(newChannel) && message.equals(newMessage))
            return this;

        return Action.getAction(new OutputAction(newChannel, newMessage));
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) throws ParseException {
        final Value newMessage = message.insertParameters(parameters);
        if (message.equals(newMessage))
            return this;
        return Action.getAction(new OutputAction(channel, newMessage));
    }

    @Override
    public Expression manipulateTarget(Expression target) {
        return target;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + channel.hashCode();
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
        final OutputAction other = (OutputAction) obj;
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
