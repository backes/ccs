package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.Transition;


public abstract class AbstractExpression implements Expression {
    
    private List<Transition> transitions = null;

    public List<Transition> evaluate() {
        if (transitions == null)
            transitions = evaluate0();
        
        return transitions;
    }

    protected abstract List<Transition> evaluate0();

}
