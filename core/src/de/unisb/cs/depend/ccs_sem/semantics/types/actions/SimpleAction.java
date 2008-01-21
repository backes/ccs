package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class SimpleAction extends Action {

    private final Channel channel;

    public SimpleAction(Channel channel) {
        super();
        this.channel = channel;
    }

    @Override
    public String getLabel() {
        return channel.getStringValue();
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        final Channel newChannel = channel.instantiate(parameters);
        if (channel.equals(newChannel))
            return this;

        return new SimpleAction(newChannel);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        return null;
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        if (!(actionToCheck instanceof SimpleAction))
            return false;

        final SimpleAction other = (SimpleAction)actionToCheck;
        return channel.equals(other.channel);
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // this action cannot synchronize
        return null;
    }

    @Override
    public int hashCode() {
        return channel.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimpleAction other = (SimpleAction) obj;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        return true;
    }

}
