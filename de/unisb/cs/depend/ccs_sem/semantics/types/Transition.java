package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class Transition {
    
    private Action action;
    private Expression target;
    
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
