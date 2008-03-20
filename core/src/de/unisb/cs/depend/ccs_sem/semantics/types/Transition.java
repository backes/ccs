package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.FullRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class Transition {

    private final Action action;
    private final Expression target;

    public Transition(Action action, Expression target) {
        super();
        this.action = action;
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public Expression getTarget() {
        return target;
    }

    /**
     * Replaces multiple consecutive "tau"-Transitions by just one.
     * @return either the Transition itself or a successor Transition that
     *         this one can be replaces by
     */
    public Transition minimize() {
        if (action instanceof TauAction) {
            final List<Transition> targetTransitions = target.getTransitions();
            if (targetTransitions.size() == 1) {
                final Transition nextTransition = targetTransitions.get(0);
                if (nextTransition.getAction() instanceof TauAction)
                    return nextTransition;
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "--" + action + "-> " + target;
    }

    /**
     * Called on input transitions to check whether they can synchronize with
     * the given Action, that has to be an output action (getAction().isOutputAction()
     * has to yield true).
     *
     * @param otherAction the Action to check against
     * @return the new Expression that is reached by this Transition if one matches
     *         with the otherAction (may be just the target of this trans, but
     *         doesn't have to); or <code>null</code> if no synchronization is possible
     */
    public Expression synchronizeWith(Action otherAction) {
        // this method should only be called on input actions
        assert action.isInputAction();

        return action.synchronizeWith(otherAction, target);
    }

    /**
     * Build a Transition that is this one, restricted by a specific action.
     *
     * @param restrictionAction the action that is forbidden
     * @return either <code>this</code> (if the Transition is not affected by
     *         the restriction), <code>null</code> (if the Transition is totally
     *         forbidden), or a new (modified) Transition
     */
    public Transition restrictBy(Action restrictionAction) {
        // at least the actions must have the same class and the same channel
        if (!action.getClass().equals(restrictionAction.getClass())
                || !action.getChannel().equals(restrictionAction.getChannel()))
            return this;

        // now we know that both actions have the same class, so we only check one
        if (action instanceof SimpleAction) {
            // we already know that the channels match, so it is restricted
            return null;
        }

        if (action instanceof OutputAction) {
            final Value restrictionValue = restrictionAction.getValue();
            if (restrictionValue == null
                    || restrictionValue.equals(action.getValue()))
                return null;
            // else:
            return this;
        }

        if (action instanceof InputAction) {
            final InputAction myInputAction = (InputAction)action;
            final InputAction restrictionInputAction = (InputAction)restrictionAction;
            final Value myValue = myInputAction.getValue();
            final Parameter myParam = myInputAction.getParameter();
            assert restrictionInputAction.getValue() == null
                || restrictionInputAction.getValue() instanceof ConstantValue;
            final ConstantValue restrictionValue = (ConstantValue)restrictionInputAction.getValue();

            if (restrictionValue != null) {
                // if both have a value and they match, return null
                if (myValue != null)
                    return restrictionValue.equals(myValue) ? null : this;

                // a bit more complex: my action has a parameter
                if (myParam != null) {
                    Range newRange = myParam.getRange();
                    if (newRange == null)
                        newRange = new FullRange();
                    newRange = newRange.subtract(new SetRange(Collections.singleton((Value)restrictionValue)));
                    final Parameter newParam = new Parameter(myParam.getName(), newRange);
                    final Action newAction = new InputAction(myInputAction.getChannel(), newParam);
                    // in the target, the old parameter has to be substituted with the new one
                    final Map<Parameter, Value> map = Collections.singletonMap(
                        myParam, (Value)new ParameterReference(newParam));
                    final Expression newTarget = target.instantiate(map);
                    return new Transition(newAction, newTarget);
                }
                // if my action has no value and no parameter, but the other one
                // has, there is no restriction
                return this;
            }

            if (restrictionInputAction.getParameter() != null) {
                final Parameter restrictionParameter = restrictionInputAction.getParameter();

                // restriction with a parameter means restricting everything
                // that is within the range of the parameter

                if (myValue != null) {
                    if (restrictionParameter.getRange() == null
                            || restrictionParameter.getRange().contains(myValue))
                        return null;
                    else
                        return this;
                }

                // a bit more complex: my action has a parameter
                if (myParam != null) {
                    if (restrictionParameter.getRange() == null)
                        return null;
                    Range newRange = myParam.getRange();
                    if (newRange == null)
                        newRange = new FullRange();
                    newRange = newRange.subtract(restrictionParameter.getRange());
                    final Parameter newParam = new Parameter(myParam.getName(), newRange);
                    final Action newAction = new InputAction(myInputAction.getChannel(), newParam);
                    // in the target, the old parameter has to be substituted with the new one
                    final Map<Parameter, Value> map = Collections.singletonMap(
                        myParam, (Value)new ParameterReference(newParam));
                    final Expression newTarget = target.instantiate(map);
                    return new Transition(newAction, newTarget);
                }

                // if my action has no value and no parameter, there is no restriction
                return this;
            }

            // otherwise, the restrictionValue always restricts us
            return null;
        }

        // no restriction found (e.g. TauAction)
        return this;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + action.hashCode();
        result = PRIME * result + target.hashCode();
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
        final Transition other = (Transition) obj;
        if (!action.equals(other.action))
            return false;
        if (!target.equals(other.target))
            return false;
        return true;
    }

}
