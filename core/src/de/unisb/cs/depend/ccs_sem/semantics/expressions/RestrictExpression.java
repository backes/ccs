package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RestrictExpression extends Expression {

    private final Expression innerExpr;
    private final Set<Channel> restricted;

    public RestrictExpression(Expression innerExpr, Set<Channel> restricted) {
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

        for (final Transition trans: oldTransitions) {
            final Channel channel = trans.getAction().getChannel();
            if (restricted.contains(channel))
                continue;

            Expression newExpr = new RestrictExpression(trans.getTarget(), restricted);
            // search if this expression is already known
            newExpr = ExpressionRepository.getExpression(newExpr);
            // create the new Transition
            final Transition newTrans = new Transition(trans.getAction(), newExpr);
            newTransitions.add(newTrans);
        }

        return newTransitions;
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        final Expression newInnerExpr = innerExpr.replaceRecursion(processVariables);
        if (innerExpr.equals(newInnerExpr))
            return this;
        return new RestrictExpression(newInnerExpr, restricted);
    }

    @Override
    protected boolean isError0() {
        return innerExpr.isError();
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Expression newExpr = innerExpr.instantiate(parameters);
        Set<Channel> newRestricted = null;
        for (final Channel rest: restricted) {
            final Channel newRest = rest.instantiate(parameters);
            if (newRestricted == null) {
                if (!rest.equals(newRest)) {
                    newRestricted = new TreeSet<Channel>();
                    for (final Channel ch: restricted) {
                        if (ch == rest)
                            break;
                        newRestricted.add(ch);
                    }
                    newRestricted.add(newRest);
                }
            } else
                newRestricted.add(newRest);
        }

        if (newRestricted == null && innerExpr.equals(newExpr)) // this means no changes
            return this;
        return ExpressionRepository.getExpression(new RestrictExpression(
            newExpr, newRestricted == null ? restricted : newRestricted));
    }

    @Override
    public Set<Action> getAlphabet(Set<ProcessVariable> alreadyIncluded) {
        // filter out the restricted actions
        final Set<Action> innerAlphabet = innerExpr.getAlphabet(alreadyIncluded);
        final Iterator<Action> it = innerAlphabet.iterator();
        while (it.hasNext())
            if (restricted.contains(it.next().getChannel()))
                it.remove();

        return innerAlphabet;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(innerExpr).append(" \\ {");
        boolean first = true;
        for (final Channel restr: restricted) {
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
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 1;
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
