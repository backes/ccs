package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression.RecursiveExpressionAlphabetWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstBooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class ConditionalExpression extends Expression {

    private final Value condition;
    private final Expression consequence;

    private ConditionalExpression(Value condition, Expression consequence) {
        super();
        this.condition = condition;
        this.consequence = consequence;
    }

    public static Expression create(Value condition, Expression consequence) {
        if (condition instanceof ConstBooleanValue)
            return ((ConstBooleanValue)condition).getValue() ? consequence : StopExpression.get();
        return ExpressionRepository.getExpression(new ConditionalExpression(condition, consequence));
    }

    @Override
    protected List<Transition> evaluate0() {
        assert condition instanceof ConstBooleanValue;

        if (((ConstBooleanValue)condition).getValue())
            return consequence.getTransitions();

        return Collections.emptyList();
    }

    @Override
    public Collection<Expression> getChildren() {
        assert condition instanceof ConstBooleanValue;

        if (((ConstBooleanValue)condition).getValue())
            return Collections.singleton(consequence);

        return Collections.emptySet();
    }

    @Override
    public Collection<Expression> getSubTerms() {
        return Collections.singleton(consequence);
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        Value newCondition;
        try {
            newCondition = condition.instantiate(parameters);
        } catch (final ArithmeticError e) {
            return ErrorExpression.get();
        }
        final Expression newConsequence = consequence.instantiate(parameters);
        if (condition.equals(newCondition) && consequence.equals(newConsequence))
            return this;
        return create(newCondition, newConsequence);
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables)
            throws ParseException {
        final Expression newConsequence = consequence.replaceRecursion(processVariables);
        if (consequence.equals(newConsequence))
            return this;
        return create(condition, newConsequence);
    }

    @Override
    protected boolean isError0() {
        // if the condition is not evaluatable so far, we cannot detect an error
        return false;
    }

    @Override
    public Set<Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        // the alphabet is the consequence's alphabet, even if we don't know
        // whether it will ever occur
        return consequence.getAlphabet(alreadyIncluded);
    }

    @Override
    public String toString() {
        final String conditionString = condition.toString();
        final String consequenceString = consequence.toString();
        final StringBuilder sb = new StringBuilder(conditionString.length() + consequenceString.length() + 6);
        sb.append("when ").append(conditionString).append(' ').append(consequenceString);
        return sb.toString();
    }

    @Override
    public int hashCode(
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final boolean empty = parameterOccurences.isEmpty();
        if (empty && hash != 0)
            return hash;
        final int prime = 31;
        int result = 1;
        result = prime * result + condition.hashCode(parameterOccurences);
        result = prime * result + consequence.hashCode(parameterOccurences);
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
        final ConditionalExpression other = (ConditionalExpression) obj;
        if (!condition.equals(other.condition, parameterOccurences))
            return false;
        if (!consequence.equals(other.consequence, parameterOccurences))
            return false;
        return true;
    }

}
