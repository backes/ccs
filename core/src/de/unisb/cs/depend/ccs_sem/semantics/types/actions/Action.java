package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public abstract class Action implements Comparable<Action> {

    public abstract String getLabel();

    public abstract Channel getChannel();

    public abstract Value getValue();

    public abstract Action instantiate(Map<Parameter, Value> parameters);

    // toString() has to be overwritten in subclasses!
    @Override
    public abstract String toString();

    /**
     * See {@link Transition#synchronizeWith(Action)}
     * @param otherAction the Action that we want to synchronize with
     * @param target Expression before synchronizing
     * @return <code>null</code> if we can't synchronize, otherwise either the
     *         Expression target or a new one that's instantiated using otherAction
     */
    public abstract Expression synchronizeWith(Action otherAction, Expression target);

    public int compareTo(Action o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public final boolean equals(Object obj) {
        return equals(obj, new HashMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public abstract boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences);

    @Override
    public final int hashCode() {
        return hashCode(new HashMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public abstract int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences);

}
