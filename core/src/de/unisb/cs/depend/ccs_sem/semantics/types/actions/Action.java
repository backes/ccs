package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public abstract class Action {

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

    public abstract Action insertParameters(List<Parameter> parameters) throws ParseException;

    /**
     * @param actionToCheck
     * @return true iff <b>this</b> Action restrict the Action actionToCheck.
     */
    public abstract boolean restricts(Action actionToCheck);

    /**
     * If this Action is used as Prefix for a PrefixExpression, it may want
     * to manipulate the target of the PrefixExpression.

     * @param target the original target Expression
     * @return either <code>target</code> again, or a new Expression
     * @throws ParseException
     */
    public abstract Expression manipulateTarget(Expression target) throws ParseException;

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

}
