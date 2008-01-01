package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public class InputAction extends Action {

    private String channel;
    private Value message;

    public InputAction(String channel, Value message) {
        super();
        this.channel = channel;
        this.message = message;
    }

    @Override
    public String getLabel() {
        String value = message.getValue();
        StringBuilder sb = new StringBuilder(channel.length() + value.length() + 1);
        sb.append(channel).append('?').append(value);
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
        if (!(action instanceof OutputAction))
            return false;
        
        OutputAction outAct = (OutputAction) action;
        
        return outAct.getChannel().equals(channel) && outAct.getMessage().equals(message);
    }

    @Override
    public Action replaceParameters(List<Value> parameters) {
        message = message.replaceParameters(parameters);

        return this;
    }

    @Override
    public Action insertParameters(List<Value> parameters) {
        message = message.insertParameters(parameters);
        
        return this;
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
    
    @Override
    public Action clone() {
        InputAction cloned = (InputAction) super.clone();
        cloned.message = message.clone();
        
        return cloned;
    }

}
