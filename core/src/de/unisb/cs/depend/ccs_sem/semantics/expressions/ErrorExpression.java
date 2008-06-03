package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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


public class ErrorExpression extends Expression {

    private static ErrorExpression instance;

    private ErrorExpression() {
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
        return true;
    }

    @Override
    public Set<Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        // no Collections.emptySet() here because it could be modified by the caller
        return new HashSet<Action>(0);
    }

    @Override
    public String toString() {
        return "ERROR";
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 1;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return obj instanceof ErrorExpression;
    }

    public static ErrorExpression get() {
        if (instance == null)
            instance = (ErrorExpression) ExpressionRepository.getExpression(new ErrorExpression());
        return instance;
    }

}
