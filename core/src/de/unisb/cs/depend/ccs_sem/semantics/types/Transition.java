package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;


public class Transition {

    private final Action action;
    private final Expression target;

    private static Map<Action, Map<Expression, Transition>> repository
        = new HashMap<Action, Map<Expression,Transition>>();

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

    public static Transition getTransition(Action action, Expression target) {
        Map<Expression, Transition> map = repository.get(action);
        if (map == null) {
            map = new HashMap<Expression, Transition>();
            repository.put(action, map);
        }

        Transition trans = map.get(target);
        if (trans == null) {
            trans = new Transition(action, target);
            map.put(target, trans);
        }

        return trans;
    }

    @Override
    public String toString() {
        return "--" + action + "-> " + target;
    }

    /**
     * Called on input transitions to check whether they match to the given
     * otherTrans, that has to be an output action (getAction().isOutputAction()
     * has to yield true).
     *
     * @param otherTrans the Transition to check against
     * @return the new Expression that is reached by this Transition if one matches
     *         with the otherTrans (may be just the target of this trans, but
     *         doesn't have to); or <code>null</code> if the Transitions can't synchronize
     */
    public Expression synchronizeWith(Action otherAction) {
        // this method should only be called on input actions
        assert action.isInputAction();

        return action.synchronizeWith(otherAction, getTarget());
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((action == null) ? 0 : action.hashCode());
        result = PRIME * result + ((target == null) ? 0 : target.hashCode());
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
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

}
