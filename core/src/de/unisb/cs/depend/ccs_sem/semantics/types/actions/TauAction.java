package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class TauAction extends Action {

    private static TauAction instance = new TauAction();

    private final Channel channel;

    private TauAction() {
        channel = TauChannel.get();
    }

    @Override
    public String getLabel() {
        return "i";
    }

    public static TauAction get() {
        return instance;
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        return null;
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 11;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return obj == this;
    }

    @Override
    public String toString() {
        return "i";
    }

}
