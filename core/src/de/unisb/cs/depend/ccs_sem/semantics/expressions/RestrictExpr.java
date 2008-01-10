package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class RestrictExpr extends Expression {

    private Expression innerExpr;
    private final Set<Action> restricted;

    public RestrictExpr(Expression innerExpr, Set<Action> restricted) {
        super();
        this.innerExpr = innerExpr;
        this.restricted = restricted;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(innerExpr);
    }

    @Override
    protected List<Transition> evaluate0() {
        final List<Transition> oldTransitions = innerExpr.getTransitions();
        final List<Transition> newTransitions = new ArrayList<Transition>(oldTransitions.size());

        for (final Transition trans: oldTransitions)
            if (!restricted.contains(trans.getAction())) {
                Expression newExpr = new RestrictExpr(trans.getTarget(), restricted);
                // search if this expression is already known
                newExpr = Expression.getExpression(newExpr);
                // search if this transition is already known (otherwise create it)
                final Transition newTrans = Transition.getTransition(trans.getAction(), newExpr);
                newTransitions.add(newTrans);
            }

        return newTransitions;
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        innerExpr = innerExpr.replaceRecursion(declarations);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(innerExpr).append(" \\ {");
        boolean first = true;
        for (final Action restr: restricted) {
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
    public Expression instantiate(List<Value> parameters) {
        final Expression newExpr = innerExpr.instantiate(parameters);

        if (newExpr.equals(innerExpr))
            return this;

        return Expression.getExpression(new RestrictExpr(newExpr, restricted));
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        final Expression newExpr = innerExpr.insertParameters(parameters);

        if (newExpr.equals(innerExpr))
            return this;

        return Expression.getExpression(new RestrictExpr(newExpr, restricted));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((innerExpr == null) ? 0 : innerExpr.hashCode());
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
        if (innerExpr == null) {
            if (other.innerExpr != null)
                return false;
        } else if (!innerExpr.equals(other.innerExpr))
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
        final RestrictExpr cloned = (RestrictExpr) super.clone();
        cloned.innerExpr = innerExpr.clone();

        // field restricted doesn't have to be cloned

        return cloned;
    }

}
