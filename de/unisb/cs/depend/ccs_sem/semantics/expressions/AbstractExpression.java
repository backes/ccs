package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InteralSystemException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public abstract class AbstractExpression implements Expression {
    
    private List<Transition> transitions = null;

    public List<Transition> evaluate() {
        if (transitions == null)
            transitions = evaluate0();
        
        return transitions;
    }

    protected abstract List<Transition> evaluate0();

    public boolean isRegular() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public Expression clone() {
        AbstractExpression newExpr;
        try {
            newExpr = (AbstractExpression) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InteralSystemException("Expression cannot be cloned", e);
        }
        
        // the transitions don't have to be cloned!

        return newExpr;
    }

}
