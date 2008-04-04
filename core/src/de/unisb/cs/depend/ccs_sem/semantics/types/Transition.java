package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;


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
        assert action instanceof InputAction;

        return action.synchronizeWith(otherAction, target);
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
