package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public class OutputAction extends Action {

    private final String channel;
    private Value message;
    private Action counterAction = null;

    public OutputAction(String channel, Value message) {
        super();
        this.channel = channel;
        this.message = message;
    }

    @Override
    public String getLabel() {
        final String value = message.getValue();
        final StringBuilder sb = new StringBuilder(channel.length() + value.length() + 1);
        sb.append(channel).append('!').append(value);
        return sb.toString();
    }

    public String getChannel() {
        return channel;
    }

    public Value getMessage() {
        return message;
    }

    @Override
    public boolean isCounterTransition(Action action) {
        if (!(action instanceof InputAction))
            return false;

        final InputAction inAct = (InputAction) action;

        return inAct.getChannel().equals(channel) && inAct.getMessage().equals(message);
    }

    @Override
    public Action getCounterAction() {
        if (counterAction  == null)
            counterAction =
                    Action.getAction(new OutputAction(channel, message));

        return counterAction;
    }

    @Override
    public Action instantiate(List<Value> parameters) {
        final Value newMessage = message.instantiate(parameters);
        if (message.equals(newMessage))
            return this;

        return Action.getAction(new OutputAction(channel, newMessage));
    }

    @Override
    public Action insertParameters(List<Value> parameters) {
        final Value newMessage = message.insertParameters(parameters);
        if (message.equals(newMessage))
            return this;

        return Action.getAction(new OutputAction(channel, newMessage));
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

    @Override
    public Action clone() {
        final OutputAction cloned = (OutputAction) super.clone();
        cloned.message = message.clone();

        return cloned;
    }

}
