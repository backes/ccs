package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


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

        // decide if we take the naive way or a more complex one
        final boolean useComplexWay = restricted.size() > 3;

        if (useComplexWay) {
            restrictComplex(oldTransitions, newTransitions);
        } else {
            restrictNaive(oldTransitions, newTransitions);
        }

        return newTransitions;
    }

    private void restrictNaive(final List<Transition> oldTransitions,
            final List<Transition> newTransitions) {
        for (final Transition trans: oldTransitions) {
            for (final Action restrictedAction: restricted) {
                if (!restrictedAction.restricts(trans.getAction()))
                    newTransitions.add(trans);
            }
        }
    }

    private void restrictComplex(final List<Transition> oldTransitions,
            final List<Transition> newTransitions) {
        // build a mapping from channel (String) to Action(s) to hide on this channel
        final Map<String, List<Action>> restrictionMap = new HashMap<String, List<Action>>();
        for (final Action a: restricted) {
            List<Action> list = restrictionMap.get(a.getChannel());
            if (list == null)
                list = restrictionMap.put(a.getChannel(), list = new ArrayList<Action>(2));
            list.add(a);
        }

        for (final Transition trans: oldTransitions) {
            final List<Action> restrList = restrictionMap.get(trans.getAction().getChannel());
            boolean isRestricted = false;
            if (restrList != null) {
                for (final Action restrictedAction: restrList) {
                    if (restrictedAction.restricts(trans.getAction()))
                        isRestricted = true;
                }
            }
            if (!isRestricted) {
                Expression newExpr = new RestrictExpr(trans.getTarget(), restricted);
                // search if this expression is already known
                newExpr = Expression.getExpression(newExpr);
                // search if this transition is already known (otherwise create it)
                final Transition newTrans = Transition.getTransition(trans.getAction(), newExpr);
                newTransitions.add(newTrans);
            }
        }
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
    public Expression insertParameters(List<Parameter> parameters) {
        final Expression newExpr = innerExpr.insertParameters(parameters);
        if (newExpr.equals(innerExpr))
            return this;
        return Expression.getExpression(new RestrictExpr(newExpr, restricted));
    }

    @Override
    public Expression instantiateInputValue(Value value) {
        final Expression newExpr = innerExpr.instantiateInputValue(value);
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

}
