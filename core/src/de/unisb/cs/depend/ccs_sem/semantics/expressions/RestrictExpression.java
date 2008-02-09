package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RestrictExpression extends Expression {

    private final Expression innerExpr;
    private final Set<Action> restricted;

    public RestrictExpression(Expression innerExpr, Set<Action> restricted) {
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

        boolean useComplexWay = restricted.size() > 5;

        // in debug mode switch between the two modes
        assert (useComplexWay = new Random().nextBoolean()) || !useComplexWay;

        if (useComplexWay) {
            restrictComplex(oldTransitions, newTransitions);
        } else {
            restrictNaive(oldTransitions, newTransitions);
        }

        return newTransitions;
    }

    private void restrictNaive(final List<Transition> oldTransitions,
            final List<Transition> newTransitions) {
        outer:
        for (Transition trans: oldTransitions) {
            for (final Action restrictedAction: restricted) {
                trans = trans.restrictBy(restrictedAction);
                if (trans == null)
                    continue outer;
            }

            Expression newExpr = new RestrictExpression(trans.getTarget(), restricted);
            // search if this expression is already known
            newExpr = ExpressionRepository.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            final Transition newTrans = new Transition(trans.getAction(), newExpr);
            newTransitions.add(newTrans);
        }
    }

    private void restrictComplex(final List<Transition> oldTransitions,
            final List<Transition> newTransitions) {
        // build a mapping from channel (String) to Action(s) to hide on this channel
        final Map<Channel, List<Action>> restrictionMap = new HashMap<Channel, List<Action>>();
        for (final Action a: restricted) {
            List<Action> list = restrictionMap.get(a.getChannel());
            if (list == null)
                restrictionMap.put(a.getChannel(), list = new ArrayList<Action>(2));
            list.add(a);
        }

        outer:
        for (Transition trans: oldTransitions) {
            final List<Action> restrList = restrictionMap.get(trans.getAction().getChannel());
            if (restrList != null)
                for (final Action restrictedAction: restrList) {
                    trans = trans.restrictBy(restrictedAction);
                    if (trans == null)
                        continue outer;
                }

            Expression newExpr = new RestrictExpression(trans.getTarget(), restricted);
            // search if this expression is already known
            newExpr = ExpressionRepository.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            final Transition newTrans = new Transition(trans.getAction(), newExpr);
            newTransitions.add(newTrans);
        }
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        final Expression newInnerExpr = innerExpr.replaceRecursion(declarations);
        if (innerExpr.equals(newInnerExpr))
            return this;
        return new RestrictExpression(newInnerExpr, restricted);
    }

    @Override
    public boolean isError() {
        return innerExpr.isError();
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
                sb.append(", ");
            sb.append(restr);
        }
        sb.append('}');

        return sb.toString();
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Expression newExpr = innerExpr.instantiate(parameters);
        final Set<Action> newRestricted = new TreeSet<Action>();
        boolean restChanged = false;
        for (final Action rest: restricted) {
            final Action newRest = rest.instantiate(parameters);
            if (!restChanged && !rest.equals(newRest))
                restChanged = true;
            newRestricted.add(newRest);
        }

        if (!restChanged && newExpr.equals(innerExpr))
            return this;
        return ExpressionRepository.getExpression(new RestrictExpression(newExpr, newRestricted));
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 6;
        result = PRIME * result + innerExpr.hashCode();
        result = PRIME * result + restricted.hashCode();
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
        final RestrictExpression other = (RestrictExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (!innerExpr.equals(other.innerExpr))
            return false;
        if (!restricted.equals(other.restricted))
            return false;
        return true;
    }

}