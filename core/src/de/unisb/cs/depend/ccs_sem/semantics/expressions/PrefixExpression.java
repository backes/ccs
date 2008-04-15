package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class PrefixExpression extends Expression {

    private final Action prefix;
    private final Expression target;

    public PrefixExpression(Action prefix, Expression target) {
        super();
        this.prefix = prefix;
        this.target = target;
    }

    @Override
    public Collection<Expression> getChildren() {
        // nothing has to be evaluated before we can evaluate, so: empty set
        return Collections.emptySet();
    }

    @Override
    public Collection<Expression> getSubTerms() {
        return Collections.singleton(target);
    }

    @Override
    protected List<Transition> evaluate0() {
        return Collections.singletonList(new Transition(prefix, target));
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        final Expression newTarget = target.replaceRecursion(processVariables);
        if (newTarget.equals(target))
            return this;
        return ExpressionRepository.getExpression(new PrefixExpression(prefix, newTarget));
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Action newPrefix = prefix.instantiate(parameters);
        // if the prefix is an input action and its parameter changed, we have
        // to substitute it in the target
        final Expression newTarget;
        if (prefix instanceof InputAction) {
            assert newPrefix instanceof InputAction;
            final InputAction oldIA = (InputAction) prefix;
            final InputAction newIA = (InputAction) newPrefix;
            if (oldIA.getParameter() != null && !oldIA.getParameter().equals(newIA.getParameter())) {
                assert newIA.getParameter() != null;
                final Map<Parameter, Value> newParameters = new HashMap<Parameter, Value>(parameters);
                newParameters.put(oldIA.getParameter(), new ParameterReference(newIA.getParameter()));
                newTarget = target.instantiate(newParameters);
            } else
                newTarget = target.instantiate(parameters);
        } else
            newTarget = target.instantiate(parameters);
        if (newPrefix.equals(prefix) && newTarget.equals(target))
            return this;
        return ExpressionRepository.getExpression(new PrefixExpression(newPrefix, newTarget));
    }

    @Override
    protected boolean isError0() {
        return false;
    }

    @Override
    public Set<Action> getAlphabet(Set<ProcessVariable> alreadyIncluded) {
        final Set<Action> successorAlphabet = target.getAlphabet(alreadyIncluded);
        if (!(prefix instanceof TauAction))
            successorAlphabet.add(prefix);
        return successorAlphabet;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(prefix).append('.');
        if (target instanceof ChoiceExpression || target instanceof ParallelExpression
                || target instanceof RestrictExpression)
            sb.append('(').append(target).append(')');
        else
            sb.append(target);

        return sb.toString();
    }

    @Override
    public int hashCode(
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final boolean empty = parameterOccurences.isEmpty();
        if (empty && hash != 0)
            return hash;
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + prefix.hashCode(parameterOccurences);
        result = PRIME * result + target.hashCode(parameterOccurences);
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
        final PrefixExpression other = (PrefixExpression) obj;
        if (!prefix.equals(other.prefix, parameterOccurences))
            return false;
        if (!target.equals(other.target, parameterOccurences))
            return false;
        return true;
    }

}
