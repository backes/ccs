package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression.RecursiveExpressionAlphabetWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ChannelSet;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RestrictExpression extends Expression {

    private final Expression innerExpr;
    private final ChannelSet restricted;

    public RestrictExpression(Expression innerExpr, ChannelSet restricted) {
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
        ChannelSet newRestricted = null;
        for (final Channel rest: restricted) {
            final Channel newRest = rest.instantiate(parameters);
            if (newRestricted == null) {
                if (!rest.equals(newRest)) {
                    newRestricted = new ChannelSet();
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
    public Map<Action, Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        Map<Action, Action> innerAlphabet = innerExpr.getAlphabet(alreadyIncluded);
        if (innerAlphabet.isEmpty())
            return innerAlphabet;
        if (!(innerAlphabet instanceof HashMap))
            innerAlphabet = new HashMap<Action, Action>(innerAlphabet);
        final Iterator<Entry<Action, Action>> it = innerAlphabet.entrySet().iterator();
        while (it.hasNext())
            if (restricted.contains(it.next().getKey().getChannel()))
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
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final boolean empty = parameterOccurences.isEmpty();
        if (empty && hash != 0)
            return hash;
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + innerExpr.hashCode(parameterOccurences);
        result = PRIME * result + restricted.hashCode(parameterOccurences);
        if (empty) {
            assert hash == 0 || hash == result;
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RestrictExpression other = (RestrictExpression) obj;
        if (!innerExpr.equals(other.innerExpr, parameterOccurences))
            return false;
        if (!restricted.equals(other.restricted, parameterOccurences))
            return false;
        return true;
    }

}
