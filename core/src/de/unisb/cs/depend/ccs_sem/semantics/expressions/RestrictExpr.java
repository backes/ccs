package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.evalutators.Evaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class RestrictExpr extends Expression {
    
    private Expression expr;
    private Set<Action> restricted;

    public RestrictExpr(Expression expr, Set<Action> restricted) {
        super();
        this.expr = expr;
        this.restricted = restricted;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(expr);
    }

    @Override
    protected void evaluateChildren(Evaluator eval) {
        if (!expr.isEvaluated()) {
            eval.evaluate(expr);
        }
    }
    
    @Override
    protected List<Transition> evaluateThis() {
        List<Transition> oldTransitions = expr.evaluateThis();
        List<Transition> newTransitions = new ArrayList<Transition>(oldTransitions.size());
        
        for (Transition trans: oldTransitions)
            if (!restricted.contains(trans.getAction())) {
                Expression newExpr = new RestrictExpr(trans.getTarget(), restricted);
                // search if this expression is already known
                newExpr = Expression.getExpression(newExpr);
                // search if this transition is already known (otherwise create it)
                Transition newTrans = Transition.getTransition(trans.getAction(), newExpr);
                newTransitions.add(newTrans);
            }

        return newTransitions;
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        expr = expr.replaceRecursion(declarations);
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expr).append(" \\ {");
        boolean first = true;
        for (Action restr: restricted) {
            if (first)
                first = false;
            else
                sb.append(',');
            sb.append(restr);
        }
        sb.append('}');

        return sb.toString();
    }

    @Override
    public Expression replaceParameters(List<Value> parameters) {
        expr = expr.replaceParameters(parameters);

        return this;
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        expr = expr.insertParameters(parameters);

        return this;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((expr == null) ? 0 : expr.hashCode());
        result = PRIME * result + ((restricted == null) ? 0 : restricted.hashCode());
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
        final RestrictExpr other = (RestrictExpr) obj;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
            return false;
        if (restricted == null) {
            if (other.restricted != null)
                return false;
        } else if (!restricted.equals(other.restricted))
            return false;
        return true;
    }
    
    @Override
    public Expression clone() {
        RestrictExpr cloned = (RestrictExpr) super.clone();
        cloned.expr = expr.clone();
        
        // field restricted doesn't have to be cloned

        return cloned;
    }

}
