package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression.RecursiveExpressionAlphabetWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class StopExpression extends Expression {

    private static StopExpression instance;

    private StopExpression() {
        super();
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.emptySet();
    }

    @Override
    protected List<Transition> evaluate0() {
        return Collections.emptyList();
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) {
        return this;
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    protected boolean isError0() {
        return false;
    }

    @Override
    public Set<Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "0";
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 7;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return obj instanceof StopExpression;
    }

    public static StopExpression get() {
        if (instance == null)
            instance = (StopExpression) ExpressionRepository.getExpression(new StopExpression());
        return instance;
    }

}
