package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public abstract class Action implements Comparable<Action> {

    public abstract String getLabel();

    public abstract Channel getChannel();

    public abstract Value getMessage();

    /**
     * Overwritten by all Actions that can act as input action.
     * @return
     */
    public boolean isInputAction() {
        return false;
    }

    /**
     * Overwritten by all Actions that can act as output action.
     * @return
     */
    public boolean isOutputAction() {
        return false;
    }

    public abstract Action instantiate(Map<Parameter, Value> parameters);

    /**
     * @param actionToCheck
     * @return true iff <b>this</b> Action restrict the Action actionToCheck.
     */
    public abstract boolean restricts(Action actionToCheck);

    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * See {@link Transition#synchronizeWith(Action)}
     * @param otherAction the Action that we want to synchronize with
     * @param our target Expression before synchronizing
     * @return <code>null</code> if we can't synchronize, otherwise either the
     *         Expression target or a new one that's instantiated using otherAction
     */
    public abstract Expression synchronizeWith(Action otherAction, Expression target);

    public int compareTo(Action o) {
        return getLabel().compareTo(o.getLabel());
    }
}
